import json
import time
from datetime import datetime, timedelta

from vt import (Empresa, adicionar_item, agora_brt, checar, futuro, garantir_vidro, gw_para,
                gw_reset, medicao, mover, mysql, novo_cliente, novo_orcamento, novo_telefone,
                orcamento, orcamento_enviado, propor_medicao, RESULTADOS, atendimentos)

gw_reset()
e = Empresa()
garantir_vidro(e)

DIAS = {0: "segunda", 1: "terca", 2: "quarta", 3: "quinta", 4: "sexta", 5: "sabado", 6: "domingo"}


def status_medicao(orc):
    return medicao(e, orc)["status"]


def cenario_medicao(data=None):
    tel = novo_telefone()
    cli = novo_cliente(e, tel)
    orc = novo_orcamento(e, cli)
    r = propor_medicao(e, orc, data)
    assert r.ok, r.text
    return tel, orc


tel, orc = cenario_medicao()
e.cliente_diz(tel, "Sem problema, mas nesse dia não vou estar em casa")
checar("Q1 'sem problema, mas nesse dia não vou estar em casa' NÃO confirma a medição",
       status_medicao(orc) != "AGENDADA", status_medicao(orc))

for n, texto in (("Q2", "Sem dúvida não vou fechar com esse preço"),
                 ("Q3", "Sem problema, vou pensar e te falo"),
                 ("Q4", "Sem problema eu pagar metade na entrega?")):
    tel = novo_telefone()
    cli = novo_cliente(e, tel)
    orc = orcamento_enviado(e, cli)
    e.cliente_diz(tel, texto)
    st = orcamento(e, orc)["status"]
    checar(f"{n} '{texto}' NÃO aprova o orçamento", st != "APROVADO", st)

hoje = datetime.now()
proxima_quarta = hoje + timedelta(days=((2 - hoje.weekday()) % 7) or 7)
tel, orc = cenario_medicao()
e.cliente_diz(tel, "2")
e.cliente_diz(tel, "não posso segunda, só quarta às 10h")
m = medicao(e, orc)
data_lida = m.get("contrapropostaData")
checar("Q5 'não posso segunda, só quarta às 10h' é lida como quarta 10h (ou não é lida)",
       data_lida is None or data_lida.startswith(proxima_quarta.strftime("%Y-%m-%d") + "T10:00"),
       f"lida={data_lida}; esperado quarta {proxima_quarta:%d/%m} 10h")
checar("Q5b o bot não diz ao cliente que entendeu segunda-feira",
       not any("entendi: segunda" in x for x in gw_para(tel)), [x for x in gw_para(tel) if "entendi" in x][-1:])

tel, orc = cenario_medicao()
e.cliente_diz(tel, "2")
alvo = hoje + timedelta(days=12)
nao = hoje + timedelta(days=11)
e.cliente_diz(tel, f"dia {nao.day} não dá, dia {alvo.day} às 15h")
data_lida = medicao(e, orc).get("contrapropostaData")
checar(f"Q6 'dia {nao.day} não dá, dia {alvo.day} às 15h' é lida como dia {alvo.day} (ou não é lida)",
       data_lida is None or data_lida[8:10] == f"{alvo.day:02d}", f"lida={data_lida}")

tel, orc = cenario_medicao()
e.cliente_diz(tel, "2")
e.cliente_diz(tel, "amanhã às 14h não consigo, só às 17h")
data_lida = medicao(e, orc).get("contrapropostaData")
checar("Q7 'amanhã às 14h não consigo, só às 17h' é lida como 17h (ou não é lida)",
       data_lida is None or "T17:00" in data_lida, f"lida={data_lida}")

tel, orc = cenario_medicao()
agora = int(time.time())
e.cliente_diz(tel, "obrigado", timestamp=agora + 7)
e.cliente_diz(tel, "1", timestamp=agora + 2)
checar("Q8 '1' entregue depois de um 'obrigado' escrito depois continua confirmando",
       status_medicao(orc) == "AGENDADA", status_medicao(orc))

tel = novo_telefone()
cli = novo_cliente(e, tel)
orc = orcamento_enviado(e, cli)
e.cliente_diz(tel, "1")
os_id = [o for o in e.admin.get("/api/ordens-servico").json() if o["orcamentoId"] == orc][0]["id"]
e.admin.put(f"/api/ordens-servico/{os_id}/necessita-producao?valor=false")
r = e.admin.post(f"/api/ordens-servico/{os_id}/instalacao", {"dataAgendada": futuro(), "equipeResponsavel": "Equipe Q9"})
assert r.ok, r.text
e.cliente_diz(tel, "1")
e.cliente_diz(tel, "quero falar com um atendente sobre a garantia")
antes = atendimentos(tel)[-1]
mysql(f"update instalacao set data_agendada = '{agora_brt(-60)}' where ordem_servico_id={os_id}")
r = e.admin.post(f"/api/ordens-servico/{os_id}/instalacao/realizar", {"checklist": "ok"})
depois = mysql(f"select status from atendimento_whatsapp where id={antes[0]}")[0][0]
checar("Q9 concluir a instalação não encerra a conversa de um cliente que está esperando atendente",
       not (antes[1] == "AGUARDANDO_ATENDENTE" and depois == "ENCERRADO"), f"antes={antes[1]} depois={depois}")

tel = novo_telefone()
cli = novo_cliente(e, tel)
orc = novo_orcamento(e, cli)
assert propor_medicao(e, orc).ok
adicionar_item(e, orc)
e.admin.post(f"/api/orcamentos/{orc}/aceitar-sugestoes")
mover(e, orc, "ORCAMENTO_FINAL")
antes = len(gw_para(tel))
env = e.admin.post(f"/api/orcamentos/{orc}/enviar")
corpo = env.json() if env.headers.get("content-type", "").startswith("application/json") else {}
checar("Q10a envio com medição em negociação para e pergunta (409 MEDICAO_PENDENTE), sem mandar nada",
       env.status_code == 409 and (corpo.get("erros") or {}).get("codigo") == "MEDICAO_PENDENTE"
       and orcamento(e, orc)["status"] == "ORCAMENTO_FINAL" and len(gw_para(tel)) == antes,
       f"HTTP {env.status_code} {corpo} status={orcamento(e, orc)['status']} msgs={gw_para(tel)[antes:]}")
env = e.admin.post(f"/api/orcamentos/{orc}/enviar", {"medicaoPendente": "CANCELAR"})
time.sleep(1)
novas = gw_para(tel)[antes:]
checar("Q10b 'cancelar a visita e enviar': medição cancelada, cliente avisado e orçamento enviado",
       env.status_code == 200 and orcamento(e, orc)["status"] == "ENVIADO" and status_medicao(orc) == "CANCELADA"
       and any("cancel" in (m or "").lower() for m in novas),
       f"HTTP {env.status_code} status={orcamento(e, orc)['status']} medição={status_medicao(orc)} msgs={novas}")

tel, orc = cenario_medicao()
e.cliente_diz(tel, "2")
e.cliente_diz(tel, "vou falar com meu marido")
m = medicao(e, orc)
ultima = gw_para(tel)[-1]
checar("Q11 'vou falar com meu marido' não vira sugestão de data",
       m["status"] != "CONTRAPROPOSTA_CLIENTE", f"status={m['status']} última msg={ultima[:90]!r}")

tel = novo_telefone()
cli = novo_cliente(e, tel)
orc = novo_orcamento(e, cli)
adicionar_item(e, orc)
adicionar_item(e, orc, cor="BRONZE")
r = e.admin.post(f"/api/orcamentos/{orc}/enviar-estimativa")
enviado = [x for x in gw_para(tel) if "estimativa" in x.lower()]
checar("Q12 estimativa não é enviada com item sem preço (valor subestimado)",
       not r.ok and not enviado, f"http={r.status_code} msg={enviado[-1][:100] if enviado else None}")

d = datetime.now() + timedelta(days=5)
data = d.replace(hour=14, minute=0, second=0, microsecond=0).strftime("%Y-%m-%dT%H:%M:%S")
tel, orc = cenario_medicao(data)
e.cliente_diz(tel, f"Sim, {DIAS[d.weekday()]} às 14h está ótimo")
checar("Q13 'Sim, <dia proposto> às 14h está ótimo' confirma a data proposta",
       status_medicao(orc) == "AGENDADA", status_medicao(orc))

tel, orc = cenario_medicao()
e.conectar("DESCONECTADO")
r = e.admin.post(f"/api/orcamentos/{orc}/medicao/confirmar-manualmente")
mysql(f"update medicao set data_agendada = '{agora_brt(-60*24)}' where orcamento_id={orc}")
mysql(f"update mensagem_saida set criado_em = '{agora_brt(-60*72)}' where telefone='{tel}' and status='PENDENTE'")
antes = len(gw_para(tel))
e.conectar("CONECTADO")
time.sleep(2)
novas = gw_para(tel)[antes:]
checar("Q14 confirmação parada 3 dias na fila não é enviada depois que a data passou",
       not any("Confirmado" in x for x in novas), [x[:80] for x in novas])

for texto, esperado in (("agora não", "ENVIADO"), ("não por enquanto", "ENVIADO"), ("não", "PERDIDO")):
    tel = novo_telefone()
    cli = novo_cliente(e, tel)
    orc = orcamento_enviado(e, cli)
    e.cliente_diz(tel, texto)
    st = orcamento(e, orc)["status"]
    checar(f"Q15 '{texto}' no orçamento deixa {esperado} (adiar não é perder — perdido não tem volta)",
           st == esperado, st)

d = datetime.now() + timedelta(days=6)
data = d.replace(hour=14, minute=0, second=0, microsecond=0).strftime("%Y-%m-%dT%H:%M:%S")
tel, orc = cenario_medicao(data)
e.cliente_diz(tel, f"não, {DIAS[d.weekday()]} às 14h não dá")
m = medicao(e, orc)
checar("Q16 'não, <dia proposto> às 14h não dá' é recusa, não sugestão da própria data recusada",
       m["status"] == "RECUSADA_CLIENTE" and not any("entendi" in x for x in gw_para(tel)),
       f"status={m['status']} última={gw_para(tel)[-1][:80]!r}")


import threading
from vt import gw_id, gw_modo


def ultimo_atendimento(tel):
    return atendimentos(tel)[-1]


tel = novo_telefone()
cli = novo_cliente(e, tel)
orc = novo_orcamento(e, cli)
adicionar_item(e, orc)
assert e.admin.post(f"/api/orcamentos/{orc}/enviar-estimativa").ok
assert propor_medicao(e, orc).ok
id_estimativa = gw_id(tel, "estimativa")
e.cliente_diz(tel, "👍", reacao_a=id_estimativa)
checar("Q17 👍 reagindo à estimativa NÃO confirma a medição proposta depois",
       status_medicao(orc) == "PROPOSTA_ENVIADA", status_medicao(orc))
e.cliente_diz(tel, "👍", reacao_a="3EB0MENSAGEMQUENAOEXISTE")
checar("Q17b 👍 reagindo a uma mensagem desconhecida também não confirma",
       status_medicao(orc) == "PROPOSTA_ENVIADA", status_medicao(orc))

e.cliente_diz(tel, "👍", reacao_a=gw_id(tel, "Podemos agendar"))
checar("Q18 👍 reagindo à própria proposta confirma", status_medicao(orc) == "AGENDADA", status_medicao(orc))

tel = novo_telefone()
cli = novo_cliente(e, tel)
orc_m = novo_orcamento(e, cli)
assert propor_medicao(e, orc_m).ok
orc_o = orcamento_enviado(e, cli)
e.cliente_diz(tel, "👍", reacao_a=gw_id(tel, "Segue o seu orçamento"))
checar("Q19 com medição e orçamento pendentes, 👍 no orçamento aprova só o orçamento",
       orcamento(e, orc_o)["status"] == "APROVADO" and status_medicao(orc_m) == "PROPOSTA_ENVIADA",
       f"orçamento={orcamento(e, orc_o)['status']} medição={status_medicao(orc_m)}")

tel, orc = cenario_medicao()
e.cliente_diz(tel, "1", mensagem_id="WAQ20ORIGINAL")
antes = status_medicao(orc)
e.cliente_diz(tel, "2", mensagem_id="WAQ20EDICAO", edita="WAQ20ORIGINAL")
at = ultimo_atendimento(tel)
checar("Q20 editar o '1' para '2' depois de confirmado não muda a visita sozinho e chama um atendente",
       antes == "AGENDADA" and status_medicao(orc) == "AGENDADA" and at[1] == "AGUARDANDO_ATENDENTE"
       and "editou" in gw_para(tel)[-1],
       f"antes={antes} depois={status_medicao(orc)} conversa={at[1]} bot={gw_para(tel)[-1][:60]!r}")

tel, orc = cenario_medicao()
e.cliente_diz(tel, "1", mensagem_id="WAQ21ORIGINAL")
e.cliente_diz(tel, "", mensagem_id="WAQ21REVOKE", apaga="WAQ21ORIGINAL")
at = ultimo_atendimento(tel)
checar("Q21 apagar o '1' que confirmou a visita chama um atendente",
       status_medicao(orc) == "AGENDADA" and at[1] == "AGUARDANDO_ATENDENTE",
       f"medição={status_medicao(orc)} conversa={at[1]}")

tel, orc = cenario_medicao()
e.cliente_diz(tel, "oi", mensagem_id="WAQ22ORIGINAL")
e.cliente_diz(tel, "1", mensagem_id="WAQ22EDICAO", edita="WAQ22ORIGINAL")
checar("Q22 'oi' editado para '1' vale como a resposta nova",
       status_medicao(orc) == "AGENDADA", status_medicao(orc))

tel, orc = cenario_medicao()
e.cliente_diz(tel, "sim", mensagem_id="WAQ23ORIGINAL")
e.cliente_diz(tel, "Sim!", mensagem_id="WAQ23EDICAO", edita="WAQ23ORIGINAL")
checar("Q23 edição que só corrige a escrita não chama atendente",
       status_medicao(orc) == "AGENDADA" and ultimo_atendimento(tel)[1] == "EM_FLUXO_BOT",
       f"medição={status_medicao(orc)} conversa={ultimo_atendimento(tel)[1]}")

tel = novo_telefone()
cli = novo_cliente(e, tel)
orc = novo_orcamento(e, cli)
gw_modo(atrasarProximaSegundos=4)
propondo = threading.Thread(target=lambda: propor_medicao(e, orc))
propondo.start()
time.sleep(1.5)
e.cliente_diz(tel, "oi")
propondo.join()
time.sleep(2)
msgs = gw_para(tel)
pos_proposta = next((n for n, m in enumerate(msgs) if "Podemos agendar" in m), None)
pos_resposta = next((n for n, m in enumerate(msgs) if "Podemos agendar" not in m), None)
checar("Q24 a resposta do bot não chega antes da proposta gerada antes dela",
       pos_proposta is not None and pos_resposta is not None and pos_proposta < pos_resposta,
       [m[:40] for m in msgs])

tel = novo_telefone()
cli = novo_cliente(e, tel)
orc_m = novo_orcamento(e, cli)
assert propor_medicao(e, orc_m).ok
e.cliente_diz(tel, "2")
e.cliente_diz(tel, "sexta às 10h")
orcs = [orcamento_enviado(e, cli) for _ in range(3)]
time.sleep(1.2)
e.cliente_diz(tel, "👍", reacao_a=gw_id(tel, f"orçamento nº {orcs[2]}"))
lista = gw_para(tel)[-1]
e.cliente_diz(tel, "1")
depois = gw_para(tel)[-1]
checar("Q25 na lista 'Você tem N assuntos', o '1' escolhe o item 1 mostrado (não a medição escondida)",
       f"1 - Aprovar o orçamento nº {orcs[0]}" in lista and f"orçamento nº {orcs[0]}" in depois
       and "Qual dia e horário" not in depois,
       f"lista={lista[:120]!r} depois={depois[:80]!r}")
e.cliente_diz(tel, "1")
checar("Q25b e o '1' seguinte aprova esse orçamento",
       [orcamento(e, o)["status"] for o in orcs] == ["APROVADO", "ENVIADO", "APROVADO"],
       [orcamento(e, o)["status"] for o in orcs])

tel = novo_telefone()
cli = novo_cliente(e, tel)
orc_a = orcamento_enviado(e, cli)
orc_b = orcamento_enviado(e, cli)
time.sleep(1.2)
e.cliente_diz(tel, "0")
e.cliente_diz(tel, "2")
consulta = gw_para(tel)[-1]
checar("Q26 'acompanhar meu orçamento' mostra os dois aguardando aprovação",
       f"nº {orc_a}" in consulta and f"nº {orc_b}" in consulta and "assuntos em aberto" in consulta,
       consulta[:160])
item_a = next((l.split(" - ")[0] for l in consulta.splitlines() if f"orçamento nº {orc_a}" in l and " - " in l), None)
e.cliente_diz(tel, item_a or "?")
e.cliente_diz(tel, "1")
checar("Q26b escolher o 2º da lista e aprovar aprova o orçamento certo",
       orcamento(e, orc_a)["status"] == "APROVADO" and orcamento(e, orc_b)["status"] == "ENVIADO",
       f"A={orcamento(e, orc_a)['status']} B={orcamento(e, orc_b)['status']}")

tel = novo_telefone()
cli = novo_cliente(e, tel)
orc_a = orcamento_enviado(e, cli)
orc_b = orcamento_enviado(e, cli)
time.sleep(1.2)
e.cliente_diz(tel, f"aprovo o orçamento nº {orc_a}")
checar("Q27 'aprovo o orçamento nº X' com dois orçamentos abertos aprova o X e só ele",
       orcamento(e, orc_a)["status"] == "APROVADO" and orcamento(e, orc_b)["status"] == "ENVIADO",
       f"A={orcamento(e, orc_a)['status']} B={orcamento(e, orc_b)['status']} bot={gw_para(tel)[-1][:70]!r}")
e.cliente_diz(tel, f"aprovo o orçamento nº {orc_a + 1000}")
checar("Q27b número de um orçamento que não existe não aprova o outro",
       orcamento(e, orc_b)["status"] == "ENVIADO", orcamento(e, orc_b)["status"])

ok = sum(1 for _, c, _ in RESULTADOS if c)
print(f"\nRESUMO: {ok} passaram / {len(RESULTADOS) - ok} falharam de {len(RESULTADOS)} verificações")
json.dump([{"nome": n, "passou": c, "detalhe": str(d)} for n, c, d in RESULTADOS],
          open("resultado_quebra.json", "w"), ensure_ascii=False, indent=1)
