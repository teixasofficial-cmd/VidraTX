import os
import threading
import time
import traceback

from vt import *

e = Empresa()
gw_reset()


def rodar(fn):
    print(f"\n=== {fn.__doc__.strip().splitlines()[0]}")
    try:
        fn()
    except Exception as ex:
        checar(fn.__name__ + " (erro no harness)", False, repr(ex))
        traceback.print_exc()


def preparar_medicao(data=None):
    tel = novo_telefone()
    cli = novo_cliente(e, tel)
    orc = novo_orcamento(e, cli)
    r = propor_medicao(e, orc, data)
    assert r.ok, r.text
    return tel, cli, orc


def t01():
    data = futuro(4, 14)
    tel, cli, orc = preparar_medicao(data)
    checar("T1 proposta enviada ao cliente com data/hora", any(br(data) in m for m in gw_para(tel)),
           gw_para(tel)[-1] if gw_para(tel) else "nenhuma mensagem")
    e.cliente_diz(tel, "sim")
    m = medicao(e, orc)
    checar("T1 'sim' confirma (AGENDADA)", m["status"] == "AGENDADA", m["status"])
    checar("T1 resposta de confirmação ao cliente", "confirmada" in (ultima_msg_bot(tel) or ""), ultima_msg_bot(tel))
    checar("T1 pipeline do orçamento avança para 'Visita agendada' ao confirmar",
           orcamento(e, orc)["status"] == "VISITA_AGENDADA", orcamento(e, orc)["status"])

    for texto in ["Sim!", "sim, pode ser", "pode ser", "ok", "Pode sim", "👍", "Sim.", "combinado", "fechado"]:
        tel2, _, orc2 = preparar_medicao()
        e.cliente_diz(tel2, texto)
        st = medicao(e, orc2)["status"]
        checar(f"T1 aceite '{texto}' reconhecido como confirmação", st == "AGENDADA",
               f"status={st}; texto salvo como contraproposta={medicao(e, orc2).get('contrapropostaTexto')!r}")


def t02():
    tel, cli, orc = preparar_medicao()
    e.cliente_diz(tel, "não")
    m = medicao(e, orc)
    checar("T2 'não' marca RECUSADA_CLIENTE", m["status"] == "RECUSADA_CLIENTE", m["status"])
    checar("T2 data recusada não aparece como confirmada", m["status"] != "AGENDADA")
    for texto in ["não posso", "Não.", "nao da", "não consigo nesse dia", "esse dia não"]:
        tel2, _, orc2 = preparar_medicao()
        e.cliente_diz(tel2, texto)
        m2 = medicao(e, orc2)
        checar(f"T2 recusa '{texto}' reconhecida como recusa", m2["status"] == "RECUSADA_CLIENTE",
               f"status={m2['status']} contraproposta={m2.get('contrapropostaTexto')!r}")


def t03():
    tel, cli, orc = preparar_medicao()
    e.cliente_diz(tel, "não")
    e.cliente_diz(tel, "pode ser dia 12 às 10h?")
    m = medicao(e, orc)
    checar("T3 contraproposta registrada (CONTRAPROPOSTA_CLIENTE)", m["status"] == "CONTRAPROPOSTA_CLIENTE", m["status"])
    checar("T3 texto da contraproposta preservado", m.get("contrapropostaTexto") == "pode ser dia 12 às 10h?",
           m.get("contrapropostaTexto"))
    checar("T3 contraproposta não vira agendamento", m["status"] != "AGENDADA")
    checar("T3 data/hora da contraproposta registrada de forma estruturada",
           any(k in m for k in ("contrapropostaData", "dataContraproposta", "contrapropostaEm")),
           f"campos disponíveis: {sorted(m.keys())}")


def t04():
    tel, cli, orc = preparar_medicao()
    e.cliente_diz(tel, "dia 12 às 10h fica melhor pra mim")
    nova = futuro(6, 10)
    r = e.admin.put(f"/api/orcamentos/{orc}/medicao/aceitar-contraproposta", {"dataAgendada": nova})
    m = medicao(e, orc)
    checar("T4 aceitar contraproposta confirma (AGENDADA)", r.ok and m["status"] == "AGENDADA", f"{r.status_code} {m['status']}")
    checar("T4 cliente notificado com a data final", any(br(nova) in x and "Combinado" in x for x in gw_para(tel)),
           ultima_msg_bot(tel))
    r2 = e.admin.put(f"/api/orcamentos/{orc}/medicao/aceitar-contraproposta", {"dataAgendada": nova})
    checar("T4 duplo clique em aceitar não duplica (2º rejeitado)", r2.status_code >= 400, r2.status_code)
    checar("T4 só 1 mensagem 'Combinado' enviada",
           sum("Combinado" in x for x in gw_para(tel)) == 1, sum("Combinado" in x for x in gw_para(tel)))


def t05():
    tel, cli, orc = preparar_medicao()
    e.cliente_diz(tel, "só posso sábado")
    nova = futuro(7, 9)
    r = e.admin.put(f"/api/orcamentos/{orc}/medicao/recusar-contraproposta",
                    {"dataAgendada": nova, "motivo": "aos sábados a equipe não atende"})
    checar("T5 existe ação explícita 'Recusar contraproposta'", r.ok, f"HTTP {r.status_code} {r.text[:200]}")
    hist = historico_orcamento(orc)
    checar("T5 histórico registra que a EMPRESA recusou a sugestão do cliente",
           any("recus" in h.lower() and "empresa" in h.lower() for h in hist), hist[-1] if hist else "")
    checar("T5 cliente é informado de que a data dele não foi aceita",
           any("não" in x.lower() and "sábado" in x.lower() for x in gw_para(tel)) or
           any("não conseguimos" in x.lower() for x in gw_para(tel)), ultima_msg_bot(tel))
    propostas = e.admin.get(f"/api/orcamentos/{orc}/medicao/propostas").json()
    checar("T5 histórico estruturado de propostas mostra a sugestão recusada e a nova proposta",
           any(p.get("origem") == "CLIENTE" and p.get("status") == "RECUSADA" for p in propostas)
           and any(p.get("origem") == "EMPRESA" and p.get("status") == "PENDENTE" for p in propostas),
           [(p.get("origem"), p.get("status")) for p in propostas])


def t06():
    tel, cli, orc = preparar_medicao()
    e.cliente_diz(tel, "pode ser dia 12 às 10h?")
    e.cliente_diz(tel, "ou dia 13 às 15h")
    propostas = e.admin.get(f"/api/orcamentos/{orc}/medicao/propostas").json()
    textos = [p.get("textoCliente") for p in propostas if p.get("origem") == "CLIENTE"]
    checar("T6 segunda sugestão não apaga a primeira (histórico estruturado)",
           textos == ["pode ser dia 12 às 10h?", "ou dia 13 às 15h"], textos)
    m = medicao(e, orc)
    checar("T6 sugestão vigente é a mais recente, com data entendida", m.get("contrapropostaTexto") == "ou dia 13 às 15h"
           and (m.get("contrapropostaData") or "").endswith("15:00:00"), (m.get("contrapropostaTexto"), m.get("contrapropostaData")))
    hist = historico_orcamento(orc)
    checar("T6 as duas sugestões aparecem no histórico", sum("dia 1" in h for h in hist) == 2, hist[-2:])


def t07():
    tel = novo_telefone()
    cli = novo_cliente(e, tel)
    orc_enviado = orcamento_enviado(e, cli)
    orc_med = novo_orcamento(e, cli)
    assert propor_medicao(e, orc_med).ok
    t = int(time.time()) + 1
    e.cliente_diz(tel, "sim", timestamp=t)
    e.cliente_diz(tel, "sim", timestamp=t)
    checar("T7 'sim' com duas pendências pergunta qual",
           "assuntos em aberto" in (ultima_msg_bot(tel) or ""), ultima_msg_bot(tel))
    st = orcamento(e, orc_enviado)["status"]
    checar("T7 2º 'sim' repetido NÃO aprova outro orçamento em silêncio", st == "ENVIADO",
           f"orçamento {orc_enviado} ficou {st} — aprovado sem o cliente ter visto que era sobre preço")

    e.cliente_diz(tel, "2")
    time.sleep(1.2)
    t = int(time.time())
    e.cliente_diz(tel, "sim", timestamp=t)
    e.cliente_diz(tel, "sim", timestamp=t)
    checar("T7 'sim' à medição escolhida confirma a medição", medicao(e, orc_med)["status"] == "AGENDADA",
           medicao(e, orc_med)["status"])
    st = orcamento(e, orc_enviado)["status"]
    checar("T7 'sim' duplicado depois de confirmar não aprova o orçamento", st == "ENVIADO", f"orçamento ficou {st}")


def t08():
    tel = novo_telefone()
    e.cliente_diz(tel, "oi")
    e.cliente_diz(tel, "João Pereira")
    e.cliente_diz(tel, "1")
    e.cliente_diz(tel, "box de banheiro", mensagem_id="WA-T8-BOX-" + tel)
    e.cliente_diz(tel, "box de banheiro", mensagem_id="WA-T8-BOX-" + tel)
    at = atendimentos(tel)[-1]
    checar("T8 reentrega não avança o fluxo do bot", at[2] == "COLETA_DESCRICAO",
           f"etapa={at[2]} — a mesma mensagem foi usada como 'descrição'")

    tel, cli, orc = preparar_medicao()
    codigos = []
    ths = [threading.Thread(target=lambda: codigos.append(
        e.cliente_diz(tel, "sim", mensagem_id="WA-T8-SIM-" + tel).status_code)) for _ in range(2)]
    [t.start() for t in ths]; [t.join() for t in ths]
    confirmacoes = [h for h in historico_orcamento(orc) if "confirmou" in h]
    checar("T8 'sim' duplicado concorrente gera 1 confirmação", len(confirmacoes) == 1, f"{len(confirmacoes)} confirmações; HTTP {codigos}")
    checar("T8 webhook duplicado não devolve erro 5xx (senão o gateway reentrega de novo)", all(c < 500 for c in codigos), codigos)
    checar("T8 cliente recebe só 1 resposta", len([x for x in gw_para(tel) if 'confirmada' in x or 'registrado' in x or 'transferir' in x]) == 1,
           [x[:60] for x in gw_para(tel)[1:]])
    recebidas = mysql(f"select count(*) from mensagem_recebida where whatsapp_mensagem_id='WA-T8-SIM-{tel}'")
    checar("T8 existe chave de idempotência (id da mensagem do WhatsApp) no contrato do webhook",
           recebidas and recebidas[0][0] == "1", f"linhas gravadas para o mesmo id: {recebidas}")


def t09():
    tel, cli, orc = preparar_medicao(futuro(1, 9))
    mysql(f"update medicao set data_agendada = now() - interval 2 day where orcamento_id={orc}")
    e.cliente_diz(tel, "sim")
    m = medicao(e, orc)
    checar("T9 'sim' para data já passada não confirma", m["status"] != "AGENDADA", m["status"])
    checar("T9 cliente avisado e escalado", "já passou" in (ultima_msg_bot(tel) or ""), ultima_msg_bot(tel))
    checar("T9 a medição sai de 'Aguardando confirmação do cliente' (fica claro que a empresa precisa agir)",
           m["status"] != "PROPOSTA_ENVIADA", m["status"])

    tel = novo_telefone()
    for msg in ["oi", "Carla Dias", "1", "espelho"]:
        e.cliente_diz(tel, msg)
    mysql(f"update atendimento_whatsapp set atualizado_em = now() - interval 90 day where telefone='{tel}'")
    e.cliente_diz(tel, "oi, tudo bem?")
    at = atendimentos(tel)
    checar("T9 conversa abandonada há 90 dias não reaproveita a etapa antiga",
           len(at) > 1 or at[-1][2] != "CONFIRMACAO",
           f"'oi, tudo bem?' virou a descrição do pedido de 90 dias atrás (etapa={at[-1][2]})")


def t10():
    tel, cli, orc = preparar_medicao()
    e.cliente_diz(tel, "qual o valor do box?")
    m = medicao(e, orc)
    checar("T10 pergunta fora de contexto NÃO vira contraproposta de data", m["status"] == "PROPOSTA_ENVIADA",
           f"status={m['status']} contraproposta={m.get('contrapropostaTexto')!r}")
    checar("T10 bot não responde 'vamos verificar essa data'", "verificar essa data" not in (ultima_msg_bot(tel) or ""),
           ultima_msg_bot(tel))


def t11():
    tel, cli, orc = preparar_medicao()
    e.cliente_diz(tel, "quero falar com alguém")
    checar("T11 escala durante proposta de medição", atendimentos(tel)[-1][1] == "AGUARDANDO_ATENDENTE", atendimentos(tel)[-1])

    tel = novo_telefone()
    cli = novo_cliente(e, tel)
    orc = orcamento_enviado(e, cli)
    e.cliente_diz(tel, "quero falar com alguém")
    checar("T11 escala durante orçamento aguardando aprovação", "transferir" in (ultima_msg_bot(tel) or ""),
           ultima_msg_bot(tel))
    for _ in range(4):
        e.cliente_diz(tel, "atendente por favor")
    checar("T11 cliente não fica preso em loop 'Responda SIM ou NÃO'",
           sum("Não entendi" in x for x in gw_para(tel)) < 3,
           f"{sum('Não entendi' in x for x in gw_para(tel))} respostas 'Não entendi' seguidas, sem escalar")

    for texto in ["não aceito", "não aprovo", "não, obrigado"]:
        tel = novo_telefone()
        cli = novo_cliente(e, tel)
        orc = orcamento_enviado(e, cli)
        e.cliente_diz(tel, texto)
        st = orcamento(e, orc)["status"]
        checar(f"T11b '{texto}' NÃO aprova o orçamento", st != "APROVADO", f"status={st}")
    tel, cli, orc = preparar_medicao()
    e.cliente_diz(tel, "não confirmo")
    checar("T11b 'não confirmo' NÃO confirma a medição", medicao(e, orc)["status"] != "AGENDADA",
           medicao(e, orc)["status"])


def t12():
    tel, cli, orc = preparar_medicao()
    e.cliente_diz(tel, "sim")
    e.cliente_diz(tel, "preciso mudar a data da medição, surgiu um imprevisto")
    acoes = e.admin.get("/api/dashboard/proximas-acoes").json()
    checar("T12 pedido de remarcação aparece para a empresa agir (fila de próximas ações)",
           any(a.get("tipo") == "MEDICAO" and a.get("orcamentoId") == orc and a.get("responsavel") == "EMPRESA"
               for a in acoes), f"resposta do bot: {ultima_msg_bot(tel)!r}")
    checar("T12 medição sinaliza 'reagendamento solicitado'", medicao(e, orc)["status"] != "AGENDADA",
           f"continua {medicao(e, orc)['status']} como se nada tivesse acontecido")


def t13():
    data1, data2 = futuro(3, 9), futuro(5, 16)
    tel, cli, orc = preparar_medicao(data1)
    time.sleep(1.2)
    escrito_em = int(time.time())
    time.sleep(1.2)
    e.admin.put(f"/api/orcamentos/{orc}/medicao/reagendar", {"dataAgendada": data2})
    e.cliente_diz(tel, "sim", timestamp=escrito_em)
    m = medicao(e, orc)
    checar("T13 'sim' fica vinculado à proposta que o cliente viu (ou é pedido de novo)",
           m["status"] != "AGENDADA",
           f"confirmou {m['dataAgendada']} — a data NOVA — com o 'sim' dado à data antiga {data1}")

    tel, cli, orc = preparar_medicao(data1)
    res = {}
    t1 = threading.Thread(target=lambda: res.update(u=e.admin.put(f"/api/orcamentos/{orc}/medicao/reagendar", {"dataAgendada": data2}).status_code))
    t2 = threading.Thread(target=lambda: res.update(c=e.cliente_diz(tel, "sim").status_code))
    t1.start(); t2.start(); t1.join(); t2.join()
    m = medicao(e, orc)
    checar("T13 corrida termina em estado coerente e sem 5xx", all(v < 500 for v in res.values()),
           f"HTTP {res} status final={m['status']} data={m['dataAgendada']}")


def t14():
    slot = futuro(8, 10)
    ts = []
    for _ in range(2):
        tel, cli, orc = preparar_medicao(slot)
        ts.append((tel, orc))
    for tel, _ in ts:
        e.cliente_diz(tel, "sim")
    ags = [medicao(e, o)["status"] for _, o in ts]
    checar("T14 sistema impede 2 medições confirmadas no mesmo horário (mesma equipe)",
           ags.count("AGENDADA") < 2, f"status={ags} — duas visitas às {slot}")


def t15():
    tel = novo_telefone()
    cli = novo_cliente(e, tel)
    orc_enviado = orcamento_enviado(e, cli)
    orc_med = novo_orcamento(e, cli)
    propor_medicao(e, orc_med)
    e.admin.post(f"/api/orcamentos/{orc_med}/medicao/cancelar")
    checar("T15 cliente é avisado do cancelamento", any("cancel" in x.lower() for x in gw_para(tel)),
           "nenhuma mensagem de cancelamento enviada")
    e.cliente_diz(tel, "sim")
    checar("T15 'sim' à medição cancelada não confirma nada",
           medicao(e, orc_med)["status"] == "CANCELADA", medicao(e, orc_med)["status"])
    st = orcamento(e, orc_enviado)["status"]
    checar("T15 'sim' à medição cancelada NÃO aprova o orçamento pendente", st == "ENVIADO", f"orçamento ficou {st}")


def t16():
    tel = novo_telefone()
    cli = novo_cliente(e, tel)
    orc = novo_orcamento(e, cli)
    gw_falhar(True)
    r = propor_medicao(e, orc)
    m = medicao(e, orc)
    checar("T16 falha de envio da proposta fica visível para o usuário",
           m.get("envioStatus") not in (None, "ENVIADA", "ENTREGUE", "LIDA") and m.get("responsavelProximaAcao") != "CLIENTE",
           f"HTTP {r.status_code}, envio={m.get('envioStatus')} próxima ação={m.get('responsavelProximaAcao')}: {m.get('proximaAcao')}")

    tel2 = novo_telefone()
    cli2 = novo_cliente(e, tel2)
    garantir_vidro(e)
    orc2 = novo_orcamento(e, cli2)
    adicionar_item(e, orc2)
    e.admin.post(f"/api/orcamentos/{orc2}/aceitar-sugestoes")
    mover(e, orc2, "ORCAMENTO_FINAL")
    r = e.admin.post(f"/api/orcamentos/{orc2}/enviar")
    o = orcamento(e, orc2)
    checar("T16 orçamento enviado com WhatsApp fora do ar não aparece como 'aguardando o cliente'",
           o.get("envioStatus") not in (None, "ENVIADA", "ENTREGUE", "LIDA") and o.get("responsavelProximaAcao") != "CLIENTE",
           f"HTTP {r.status_code}, status {o['status']}, envio={o.get('envioStatus')}, próxima ação={o.get('proximaAcao')}")

    gw_falhar(False)
    e.conectar("CONECTADO")
    checar("T16 ao reconectar, proposta e orçamento pendentes são entregues",
           any(not x["falhou"] for x in gw_log() if x["telefone"] == tel)
           and any(not x["falhou"] for x in gw_log() if x["telefone"] == tel2),
           [(x["telefone"], x["falhou"]) for x in gw_log() if x["telefone"] in (tel, tel2)])
    checar("T16 status de envio atualizado depois da entrega",
           medicao(e, orc).get("envioStatus") == "ENVIADA", medicao(e, orc).get("envioStatus"))


def t17():
    tel = novo_telefone()
    cli = novo_cliente(e, tel)
    orc = novo_orcamento(e, cli)
    e.conectar("DESCONECTADO")
    r = propor_medicao(e, orc)
    e.conectar("CONECTADO")
    checar("T17 agendar com WhatsApp desconectado avisa o usuário (como o 'enviar orçamento' já faz)",
           r.status_code >= 400 or "aviso" in r.text.lower(), f"HTTP {r.status_code}, medição {medicao(e, orc)['status']}")
    checar("T17 ao reconectar, a proposta pendente é reenviada", len(gw_para(tel)) > 0, "nenhuma mensagem após reconectar")


def t18():
    tel, cli, orc = preparar_medicao()
    t = int(time.time()) + 2
    e.cliente_diz(tel, "dia 12 às 10h fica melhor", timestamp=t + 5)
    e.cliente_diz(tel, "não", timestamp=t)
    m = medicao(e, orc)
    checar("T18 vale o que o cliente escreveu por último, qualquer que seja a ordem de chegada",
           m["status"] == "CONTRAPROPOSTA_CLIENTE" and m.get("contrapropostaTexto") == "dia 12 às 10h fica melhor",
           f"{m['status']} / {m.get('contrapropostaTexto')!r}")


def t19():
    tel = novo_telefone()
    cli = novo_cliente(e, tel)
    orc_a = orcamento_enviado(e, cli, preco_vidro=250)
    orc_b = orcamento_enviado(e, cli, preco_vidro=900)
    msgs = gw_para(tel)
    e.cliente_diz(tel, "sim")
    a, b = orcamento(e, orc_a)["status"], orcamento(e, orc_b)["status"]
    checar("T19 'sim' com 2 orçamentos pendentes pede para o cliente escolher", a == "ENVIADO" and b == "ENVIADO",
           f"A={a} B={b} (aprovou o último enviado sem perguntar)")
    checar("T19 resposta ao cliente identifica QUAL orçamento foi aprovado",
           any(str(orc_b) in x or "R$" in x for x in gw_para(tel)[len(msgs):]), ultima_msg_bot(tel))


def t20():
    tel, cli, orc = preparar_medicao()
    e.cliente_diz(tel, "quero falar com atendente")
    at_id = atendimentos(tel)[-1][0]
    e.admin.post(f"/api/atendimentos-whatsapp/{at_id}/assumir")
    r = e.admin.post(f"/api/atendimentos-whatsapp/{at_id}/encerrar")
    checar("T20 encerrar conversa com medição pendente exige confirmação/é bloqueado", r.status_code >= 400,
           f"HTTP {r.status_code} — encerrada com medição {medicao(e, orc)['status']}")
    r = e.admin.post(f"/api/atendimentos-whatsapp/{at_id}/encerrar", {"forcar": True})
    checar("T20 com confirmação a conversa é encerrada", r.ok, r.text[:200])
    e.cliente_diz(tel, "oi, voltei")
    at = atendimentos(tel)
    checar("T20 nova mensagem abre nova conversa e preserva a antiga", len(at) == 2, at)


def extra_bot_fala_por_cima_do_humano():
    tel = novo_telefone()
    cli = novo_cliente(e, tel)
    orcamento_enviado(e, cli)
    e.cliente_diz(tel, "oi")
    at = atendimentos(tel)[-1][0]
    empresa_id = mysql(f"select empresa_id from atendimento_whatsapp where id={at}")[0][0]
    admin_id = mysql(f"select id from usuario where email like 'admin%' and empresa_id={empresa_id} limit 1")[0][0]
    mysql(f"update atendimento_whatsapp set status='EM_ATENDIMENTO_HUMANO', atendente_id={admin_id} where id={at}")
    antes = len(gw_para(tel))
    e.cliente_diz(tel, "tá, vou ver com meu marido e te falo")
    novas = gw_para(tel)[antes:]
    checar("Extra A bot fica em silêncio enquanto um humano atende", not novas, novas)

    tel2, cli2, orc2 = preparar_medicao()
    e.cliente_diz(tel2, "quero falar com atendente")
    at2 = atendimentos(tel2)[-1][0]
    e.admin.post(f"/api/atendimentos-whatsapp/{at2}/assumir")
    antes = len(gw_para(tel2))
    e.cliente_diz(tel2, "tenho garagem sim, pode vir de carro")
    m = medicao(e, orc2)
    checar("Extra A conversa com humano não altera status da medição", m["status"] == "PROPOSTA_ENVIADA",
           f"status={m['status']} contraproposta={m.get('contrapropostaTexto')!r}; bot respondeu {gw_para(tel2)[antes:]}")


def extra_nome_invalido():
    tel = novo_telefone()
    e.cliente_diz(tel, "oi")
    e.cliente_diz(tel, "quero orçamento de box")
    nome = mysql(f"select nome from cliente where whatsapp='{tel}'")
    checar("Extra B 'quero orçamento de box' não é salvo como nome do cliente",
           not nome or nome[0][0] != "quero orçamento de box",
           f"cliente cadastrado com nome {nome}")


def extra_telefone_sem_ddi():
    tel_wpp = novo_telefone()
    tel_digitado = tel_wpp[2:]
    cli = novo_cliente(e, f"({tel_digitado[:2]}) {tel_digitado[2:7]}-{tel_digitado[7:]}")
    orc = novo_orcamento(e, cli)
    propor_medicao(e, orc)
    destinos = [m["telefone"] for m in gw_log() if "medição" in (m["mensagem"] or "")][-1:]
    checar("Extra C proposta vai para um número WhatsApp válido (com DDI 55)", destinos and destinos[0].startswith("55"),
           f"enviado para {destinos}")
    e.cliente_diz(tel_wpp, "sim")
    checar("Extra C resposta do cliente é reconhecida", medicao(e, orc)["status"] == "AGENDADA",
           f"status={medicao(e, orc)['status']}; clientes com o número: {mysql(f'select id,nome from cliente where whatsapp like {chr(39)}%{tel_digitado}{chr(39)}')}")


for fn in [t01, t02, t03, t04, t05, t06, t07, t08, t09, t10, t11, t12, t13, t14, t15, t16, t17, t18, t19, t20,
           extra_bot_fala_por_cima_do_humano, extra_nome_invalido, extra_telefone_sem_ddi]:
    rodar(fn)

ok = sum(1 for _, p, _ in RESULTADOS if p)
print(f"\nRESUMO: {ok} passaram / {len(RESULTADOS) - ok} falharam de {len(RESULTADOS)} verificações")
import json
json.dump(RESULTADOS, open(os.path.join(os.path.dirname(__file__) or ".", "resultado_whatsapp.json"), "w"),
          ensure_ascii=False, indent=1)
