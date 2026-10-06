import json
import time
from datetime import datetime
from zoneinfo import ZoneInfo

from vt import (Empresa, RESULTADOS, adicionar_item, agora_brt, atendimentos, br, checar, futuro, garantir_vidro,
                gw_para, historico_orcamento, medicao, mover, mysql, novo_cliente, novo_orcamento, novo_telefone,
                orcamento, orcamento_enviado, propor_medicao)

ESPERA_AVISO_SEGUNDOS = 45


def iso_brt(minutos):
    return agora_brt(minutos).replace(" ", "T")


def configurar_agenda(e, **campos):
    atual = e.admin.get("/api/empresa/agenda").json()
    atual.update(campos)
    return e.admin.put("/api/empresa/agenda", atual)


def erro(r):
    try:
        return r.json()
    except ValueError:
        return {}


def historico_tipos(sql_where):
    return [t for (t,) in mysql(f"select tipo from historico where {sql_where} order by id")]


ek = Empresa()
tel = novo_telefone()
cli = novo_cliente(ek, tel)
orc = novo_orcamento(ek, cli)

data_k = iso_brt(-30)
r = propor_medicao(ek, orc, data_k)
checar("K1 empresa em Brasília não propõe data que já passou (400 com o motivo)",
       r.status_code == 400 and "futuro" in erro(r).get("mensagem", ""), f"HTTP {r.status_code} {erro(r)}")

r = configurar_agenda(ek, fusoHorario="Marte/Olympus")
checar("K2 fuso inválido é recusado", r.status_code == 400, f"HTTP {r.status_code} {r.text[:150]}")

r = configurar_agenda(ek, fusoHorario="America/Rio_Branco")
checar("K3 fuso da empresa gravado (Acre)", r.ok and r.json().get("fusoHorario") == "America/Rio_Branco", r.text[:150])

r = propor_medicao(ek, orc, data_k)
msg = gw_para(tel)[-1] if gw_para(tel) else ""
checar("K4 empresa no Acre propõe o horário de lá (servidor em Brasília)",
       r.ok and medicao(ek, orc)["status"] == "PROPOSTA_ENVIADA", f"HTTP {r.status_code} {r.text[:200]}")
checar("K5 a mensagem ao cliente traz a hora como a empresa digitou", br(data_k) in msg, msg)

ek.cliente_diz(tel, "1")
checar("K6 cliente confirma: a proposta não é tratada como vencida pelo relógio de Brasília",
       medicao(ek, orc)["status"] == "AGENDADA", medicao(ek, orc)["status"])

ef = Empresa()
garantir_vidro(ef)
tel = novo_telefone()
cli = novo_cliente(ef, tel)
orc = novo_orcamento(ef, cli)

r = propor_medicao(ef, orc, iso_brt(30))
checar("F1 proposta para daqui a 30 min é recusada (antecedência mínima padrão de 60 min)",
       r.status_code == 400 and "antecedência" in erro(r).get("mensagem", ""), f"HTTP {r.status_code} {erro(r)}")

configurar_agenda(ef, antecedenciaMinimaMinutos=0)
r = propor_medicao(ef, orc, iso_brt(30))
checar("F2 com antecedência 0 configurada, a mesma proposta sai", r.ok, f"HTTP {r.status_code} {r.text[:150]}")
configurar_agenda(ef, antecedenciaMinimaMinutos=60)

r = configurar_agenda(ef, antecedenciaMinimaMinutos=-5)
checar("F3 antecedência negativa é recusada", r.status_code == 400, f"HTTP {r.status_code}")

tel = novo_telefone()
cli = novo_cliente(ef, tel)
orc = orcamento_enviado(ef, cli)
ef.cliente_diz(tel, "sim")
os_id = next(o["id"] for o in ef.admin.get("/api/ordens-servico").json() if o.get("orcamentoId") == orc)
ef.admin.put(f"/api/ordens-servico/{os_id}/necessita-producao?valor=false")

r = ef.admin.post(f"/api/ordens-servico/{os_id}/instalacao", {"dataAgendada": futuro(11, 9)})
checar("E1 instalação sem equipe é recusada (400, campo equipeResponsavel)",
       r.status_code == 400 and "equipeResponsavel" in (erro(r).get("erros") or {}), f"HTTP {r.status_code} {erro(r)}")
r = ef.admin.post(f"/api/ordens-servico/{os_id}/instalacao", {"dataAgendada": futuro(11, 9), "equipeResponsavel": "   "})
checar("E2 equipe em branco também", r.status_code == 400, f"HTTP {r.status_code}")
r = ef.admin.post(f"/api/ordens-servico/{os_id}/instalacao", {"dataAgendada": futuro(11, 9), "equipeResponsavel": "Equipe Azul"})
checar("E3 com a equipe, a proposta sai", r.ok and r.json().get("equipeResponsavel") == "Equipe Azul", r.text[:150])

tel = novo_telefone()
cli = novo_cliente(ef, tel)
orc = novo_orcamento(ef, cli)
assert propor_medicao(ef, orc).ok
adicionar_item(ef, orc)
ef.admin.post(f"/api/orcamentos/{orc}/aceitar-sugestoes")
mover(ef, orc, "ORCAMENTO_FINAL")
r = ef.admin.post(f"/api/orcamentos/{orc}/enviar", {"medicaoPendente": "MANTER"})
checar("A1 'manter a visita e enviar': orçamento enviado e a proposta de medição continua",
       r.ok and orcamento(ef, orc)["status"] == "ENVIADO" and medicao(ef, orc)["status"] == "PROPOSTA_ENVIADA",
       f"HTTP {r.status_code} orçamento={orcamento(ef, orc)['status']} medição={medicao(ef, orc)['status']}")
checar("A2 a decisão fica no histórico do orçamento",
       any("mantendo a visita" in h for h in historico_orcamento(orc)), historico_orcamento(orc)[-3:])
r = ef.admin.post(f"/api/orcamentos/{orc}/enviar", {"medicaoPendente": "TALVEZ"})
checar("A3 decisão inválida é recusada", r.status_code == 400, f"HTTP {r.status_code}")

tel = novo_telefone()
cli = novo_cliente(ef, tel)
orc = orcamento_enviado(ef, cli)
r = ef.admin.post(f"/api/orcamentos/{orc}/perder", {"motivoPerda": "PRECO_ALTO"})
checar("B1 orçamento enviado marcado como perdido", r.ok and orcamento(ef, orc)["status"] == "PERDIDO", r.text[:150])

r = ef.admin.post(f"/api/orcamentos/{orc}/reabrir", {"motivo": "cliente voltou a negociar"})
o = orcamento(ef, orc)
checar("B2 'reabrir' volta para revisão (ORCAMENTO_FINAL) e limpa o motivo da perda",
       r.ok and o["status"] == "ORCAMENTO_FINAL" and not o.get("motivoPerda"), f"HTTP {r.status_code} {o['status']} {o.get('motivoPerda')}")
checar("B3 reabertura registrada no histórico com o motivo",
       "ORCAMENTO_REABERTO" in historico_tipos(f"orcamento_id={orc}")
       and any("cliente voltou a negociar" in h for h in historico_orcamento(orc)), historico_orcamento(orc)[-2:])

ef.cliente_diz(tel, "sim")
checar("B4 o 'sim' à versão antiga não aprova o orçamento reaberto",
       orcamento(ef, orc)["status"] == "ORCAMENTO_FINAL", orcamento(ef, orc)["status"])

r = ef.admin.post(f"/api/orcamentos/{orc}/reabrir")
checar("B5 só orçamento perdido pode ser reaberto", r.status_code in (400, 409), f"HTTP {r.status_code}")

r = ef.admin.post(f"/api/orcamentos/{orc}/enviar")
ef.cliente_diz(tel, "sim")
checar("B6 reenviado, o cliente aprova normalmente", r.ok and orcamento(ef, orc)["status"] == "APROVADO",
       f"HTTP {r.status_code} {orcamento(ef, orc)['status']}")

tel = novo_telefone()
cli = novo_cliente(ef, tel)
orc = novo_orcamento(ef, cli)
mover(ef, orc, "PRE_ORCAMENTO")
r = ef.admin.post(f"/api/orcamentos/{orc}/mover-pipeline", {"status": "PERDIDO", "motivoPerda": "DESISTIU_DO_SERVICO"})
ef.admin.post(f"/api/orcamentos/{orc}/reabrir")
checar("B7 perdido em pré-orçamento volta para pré-orçamento", orcamento(ef, orc)["status"] == "PRE_ORCAMENTO",
       orcamento(ef, orc)["status"])

ej = Empresa()
tel = novo_telefone()
cli = novo_cliente(ej, tel)
orc = novo_orcamento(ej, cli)
data_j = futuro(6, 10)
assert propor_medicao(ej, orc, data_j).ok
ej.cliente_diz(tel, "1")
assert medicao(ej, orc)["status"] == "AGENDADA"

ej.cliente_diz(tel, "preciso cancelar a visita")
m = medicao(ej, orc)
checar("J1 pedido de cancelamento não cancela sozinho: a medição continua AGENDADA",
       m["status"] == "AGENDADA", m["status"])
checar("J2 o pedido fica registrado na medição (quando e o texto do cliente)",
       m.get("cancelamentoSolicitadoEm") and m.get("cancelamentoSolicitadoTexto") == "preciso cancelar a visita",
       f"{m.get('cancelamentoSolicitadoEm')} {m.get('cancelamentoSolicitadoTexto')}")
checar("J3 próxima ação da empresa: confirmar com o cliente e cancelar ou manter",
       m.get("responsavelProximaAcao") == "EMPRESA" and "pediu para cancelar" in (m.get("proximaAcao") or ""),
       m.get("proximaAcao"))
checar("J4 o cliente foi para a fila de atendimento", atendimentos(tel)[-1][1] == "AGUARDANDO_ATENDENTE", atendimentos(tel)[-1])

dia = data_j[:10]
agenda = ej.admin.get(f"/api/dashboard/agenda?de={dia}&ate={dia}").json()
item = next((a for a in agenda if a["tipo"] == "MEDICAO" and a["orcamentoId"] == orc), {})
checar("J5 a agenda marca o pedido de cancelamento", item.get("cancelamentoSolicitado") is True, item)
acoes = ej.admin.get("/api/dashboard/proximas-acoes").json()
acao = next((a for a in acoes if a.get("tipo") == "MEDICAO" and a.get("orcamentoId") == orc), {})
checar("J6 próximas ações da dashboard trazem o pedido como urgente",
       acao.get("urgente") is True and "cancelar" in (acao.get("descricao") or acao.get("acao") or json.dumps(acao)),
       acao)

antes = len(gw_para(tel))
r = ej.admin.post(f"/api/orcamentos/{orc}/medicao/manter-data")
m = medicao(ej, orc)
novas = gw_para(tel)[antes:]
checar("J7 'manter a data' limpa o pedido e mantém a medição AGENDADA",
       r.ok and m["status"] == "AGENDADA" and not m.get("cancelamentoSolicitadoEm"), f"HTTP {r.status_code} {m['status']}")
checar("J8 o cliente é avisado de que a data continua", any("continua marcada" in (x or "") for x in novas), novas)
checar("J9 histórico registra pedido e decisão",
       {"MEDICAO_CANCELAMENTO_SOLICITADO", "MEDICAO_DATA_MANTIDA"} <= set(historico_tipos(f"orcamento_id={orc}")),
       historico_tipos(f"orcamento_id={orc}")[-4:])
r = ej.admin.post(f"/api/orcamentos/{orc}/medicao/manter-data")
checar("J10 'manter a data' sem pedido aberto é recusado", r.status_code in (400, 409), f"HTTP {r.status_code}")

ej.cliente_diz(tel, "quero cancelar")
r = ej.admin.post(f"/api/orcamentos/{orc}/medicao/cancelar", {"motivo": "cliente pediu"})
m = medicao(ej, orc)
checar("J11 cancelar depois do pedido: CANCELADA e o pedido não fica pendurado",
       r.ok and m["status"] == "CANCELADA" and not m.get("cancelamentoSolicitadoEm"), f"{m['status']} {m.get('cancelamentoSolicitadoEm')}")

ei = Empresa()
configurar_agenda(ei, horaInicioMensagens=0, horaFimMensagens=24, prazoRespostaAtendenteMinutos=5)
domingo = datetime.now(ZoneInfo("America/Sao_Paulo")).weekday() == 6

tel = novo_telefone()
novo_cliente(ei, tel, nome="Joana Lima")
ei.cliente_diz(tel, "oi")
ei.cliente_diz(tel, "9")
at_id = int(atendimentos(tel)[-1][0])
assert atendimentos(tel)[-1][1] == "AGUARDANDO_ATENDENTE"

mysql(f"update atendimento_whatsapp set cliente_aguardando_desde='{agora_brt(-12)}', "
      f"ultima_mensagem_cliente_em='{agora_brt(-12)}', ultima_mensagem_empresa_em='{agora_brt(-20)}' where id={at_id}")

fila = ei.admin.get("/api/atendimentos-whatsapp/pendentes").json()
item = next((a for a in fila if a["id"] == at_id), {})
checar("I1 a fila mostra a espera calculada no servidor e o atraso (prazo de 5 min)",
       item.get("respostaAtrasada") is True and item.get("minutosEsperando", 0) >= 12
       and "esperando há" in (item.get("proximaAcao") or ""), {k: item.get(k) for k in ("minutosEsperando", "respostaAtrasada", "proximaAcao")})
resumo = ei.admin.get("/api/dashboard/resumo").json()
checar("I2 a dashboard conta as conversas atrasadas", resumo.get("atendimentosAtrasadosWhatsapp") == 1,
       resumo.get("atendimentosAtrasadosWhatsapp"))

if domingo:
    print("[INFO] I3/I4 pulados: domingo fica fora da janela de mensagens automáticas")
else:
    fim = time.time() + ESPERA_AVISO_SEGUNDOS
    avisos = []
    while time.time() < fim:
        avisos = [x for x in gw_para(tel) if "responda 0" in (x or "")]
        if avisos:
            break
        time.sleep(2)
    checar("I3 o cliente recebe o aviso de demora com a saída '0'", len(avisos) == 1, gw_para(tel)[-2:])

    ei.cliente_diz(tel, "alô? alguém?")
    time.sleep(25)
    avisos = [x for x in gw_para(tel) if "responda 0" in (x or "")]
    item = next((a for a in ei.admin.get("/api/atendimentos-whatsapp/pendentes").json() if a["id"] == at_id), {})
    checar("I4 insistir não gera outro aviso nem zera o tempo de espera",
           len(avisos) == 1 and item.get("minutosEsperando", 0) >= 12, f"{len(avisos)} avisos; espera={item.get('minutosEsperando')}")

ei.cliente_diz(tel, "0")
at = atendimentos(tel)[-1]
checar("I5 '0' na fila devolve o cliente ao atendimento automático",
       int(at[0]) == at_id and at[1] == "EM_FLUXO_BOT" and "voltou ao atendimento automático" in (gw_para(tel)[-1] or ""),
       f"{at} {gw_para(tel)[-1]}")
checar("I6 a saída da fila fica no histórico da conversa",
       "ATENDIMENTO_DEVOLVIDO_BOT" in historico_tipos(f"atendimento_id={at_id}"),
       historico_tipos(f"atendimento_id={at_id}")[-3:])

ei.cliente_diz(tel, "9")
ei.admin.post(f"/api/atendimentos-whatsapp/{at_id}/assumir")
antes = len(gw_para(tel))
ei.cliente_diz(tel, "0")
checar("I7 '0' com atendente na conversa não devolve ao bot",
       atendimentos(tel)[-1][1] == "EM_ATENDIMENTO_HUMANO" and len(gw_para(tel)) == antes,
       f"{atendimentos(tel)[-1]} novas={gw_para(tel)[antes:]}")

ei.admin.post(f"/api/atendimentos-whatsapp/{at_id}/responder", {"mensagem": "Qualquer coisa estamos à disposição."})
velho = agora_brt(-80 * 60)
mysql(f"update atendimento_whatsapp set atualizado_em='{velho}', ultima_mensagem_cliente_em='{agora_brt(-81 * 60)}', "
      f"ultima_mensagem_empresa_em='{velho}' where id={at_id}")
ei.cliente_diz(tel, "oi, voltei")
convs = atendimentos(tel)
motivo = mysql(f"select motivo_encerramento from atendimento_whatsapp where id={at_id}")[0][0]
checar("I8 conversa com atendente parada há 80 h (empresa falou por último) é encerrada e o cliente volta ao bot",
       convs[-2][1] == "ENCERRADO" and motivo == "INATIVIDADE" and convs[-1][1] == "EM_FLUXO_BOT"
       and int(convs[-1][0]) != at_id, f"{convs} motivo={motivo}")

tel2 = novo_telefone()
novo_cliente(ei, tel2, nome="Paulo Dias")
ei.cliente_diz(tel2, "oi")
ei.cliente_diz(tel2, "9")
at2 = int(atendimentos(tel2)[-1][0])
ei.admin.post(f"/api/atendimentos-whatsapp/{at2}/assumir")
ei.cliente_diz(tel2, "e o meu orçamento?")
mysql(f"update atendimento_whatsapp set atualizado_em='{velho}', cliente_aguardando_desde='{velho}', "
      f"ultima_mensagem_cliente_em='{velho}', ultima_mensagem_empresa_em='{agora_brt(-81 * 60)}' where id={at2}")
ei.cliente_diz(tel2, "alguém?")
checar("I9 conversa com o cliente esperando resposta não é encerrada por inatividade",
       len(atendimentos(tel2)) == 1 and atendimentos(tel2)[-1][1] == "EM_ATENDIMENTO_HUMANO", atendimentos(tel2))

ok = sum(1 for _, passou, _ in RESULTADOS if passou)
print(f"\nRESUMO: {ok} passaram / {len(RESULTADOS) - ok} falharam de {len(RESULTADOS)} verificações")
with open("resultado_regras.json", "w") as f:
    json.dump([{"teste": n, "passou": p, "detalhe": str(d)} for n, p, d in RESULTADOS], f, ensure_ascii=False, indent=2)
