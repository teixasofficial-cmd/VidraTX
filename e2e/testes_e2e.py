import threading
import os
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


def dashboard():
    return e.admin.get("/api/dashboard/resumo").json()


def conversa_bot_ate_solicitacao(tel, nome="Paulo Lima"):
    for msg in ["oi", nome, "1", "box de banheiro", "1,20 x 1,90, Rua das Flores 10", "sim"]:
        r = e.cliente_diz(tel, msg)
        assert r.ok, (msg, r.status_code, r.text)


def instalacao(os_id):
    return e.admin.get(f"/api/ordens-servico/{os_id}/instalacao").json()


def cenario_a():
    tel = novo_telefone()
    conversa_bot_ate_solicitacao(tel)
    at = atendimentos(tel)[-1]
    checar("A1 solicitação criada e conversa na fila do atendente", at[1] == "AGUARDANDO_ATENDENTE", at)
    sol = e.admin.get("/api/solicitacoes-orcamento").json()
    sol_id = next(s["id"] for s in sol if s.get("clienteId") == int(at[3]) or s.get("cliente", {}).get("id") == int(at[3]))
    orc = e.admin.post(f"/api/solicitacoes-orcamento/{sol_id}/criar-orcamento").json()["id"]
    orc_dup = e.admin.post(f"/api/solicitacoes-orcamento/{sol_id}/criar-orcamento").json()["id"]
    checar("A2 criar orçamento da solicitação é idempotente", orc == orc_dup)
    checar("A3 orçamento nasce em etapa inicial do pipeline", orcamento(e, orc)["status"] == "NOVO_CONTATO", orcamento(e, orc)["status"])
    garantir_vidro(e)
    adicionar_item(e, orc)
    r = e.admin.post(f"/api/orcamentos/{orc}/enviar-estimativa")
    checar("A4 existe envio de ORÇAMENTO PRÉVIO (estimativa) ao cliente antes da medição",
           r.ok and "estimativa" in (ultima_msg_bot(tel) or "").lower() and orcamento(e, orc)["status"] == "PRE_ORCAMENTO",
           f"HTTP {r.status_code}: {r.text[:120]} / {ultima_msg_bot(tel)!r}")
    data = futuro(2, 9)
    propor_medicao(e, orc, data)
    e.cliente_diz(tel, "sim")
    checar("A5 medição confirmada pelo cliente", medicao(e, orc)["status"] == "AGENDADA", medicao(e, orc)["status"])
    checar("A6 pipeline reflete a visita confirmada", orcamento(e, orc)["status"] == "VISITA_AGENDADA", orcamento(e, orc)["status"])
    mysql(f"update medicao set data_agendada = '{agora_brt(-60)}' where orcamento_id={orc}")
    e.admin.post(f"/api/orcamentos/{orc}/medicao/realizar", {"observacoes": "vão 1200x1900 conferido"})
    checar("A7 medição realizada move o pipeline para MEDIDO", orcamento(e, orc)["status"] == "MEDIDO", orcamento(e, orc)["status"])
    e.admin.post(f"/api/orcamentos/{orc}/aceitar-sugestoes")
    mover(e, orc, "ORCAMENTO_FINAL")
    r = e.admin.post(f"/api/orcamentos/{orc}/enviar")
    checar("A8 orçamento final enviado", r.ok and orcamento(e, orc)["status"] == "ENVIADO", r.status_code)
    msg = ultima_msg_bot(tel)
    checar("A9 mensagem do orçamento traz itens/validade (não só o total)", "válid" in msg.lower() or "box" in msg.lower(), msg)
    e.cliente_diz(tel, "sim")
    checar("A10 cliente aprova pelo WhatsApp", orcamento(e, orc)["status"] == "APROVADO", orcamento(e, orc)["status"])
    os_list = e.admin.get("/api/ordens-servico").json()
    checar("A11 aprovação cria automaticamente o próximo passo (OS / 'aguardando agendamento da instalação')",
           any(o.get("orcamentoId") == orc for o in os_list), "nenhuma OS/instalação pendente criada; depende de alguém lembrar")
    os_id = next(o["id"] for o in os_list if o.get("orcamentoId") == orc)
    r = e.admin.put(f"/api/ordens-servico/{os_id}/necessita-producao?valor=false")
    checar("A11b gerente dispensa a produção da OS criada na aprovação", r.ok and not r.json().get("necessitaProducao"), r.text[:120])
    di = futuro(10, 8)
    r = e.admin.post(f"/api/ordens-servico/{os_id}/instalacao", {"dataAgendada": di, "endereco": "Rua das Flores 10", "equipeResponsavel": "Equipe 1"})
    checar("A12 instalação proposta (não agendada)", r.ok and instalacao(os_id)["status"] == "PROPOSTA_ENVIADA", instalacao(os_id)["status"])
    e.cliente_diz(tel, "sim")
    checar("A13 instalação confirmada pelo cliente", instalacao(os_id)["status"] == "AGENDADA", instalacao(os_id)["status"])
    agenda = e.admin.get(f"/api/dashboard/agenda?de={di[:10]}&ate={di[:10]}").json()
    checar("A17 instalação aparece na agenda da empresa", any(a.get("tipo") == "INSTALACAO" and a.get("ordemServicoId") == os_id
                                                               for a in agenda), agenda)
    mysql(f"update instalacao set data_agendada = '{agora_brt(-60)}' where ordem_servico_id={os_id}")
    e.admin.post(f"/api/ordens-servico/{os_id}/instalacao/realizar", {"checklist": "ok"})
    checar("A14 instalação concluída", instalacao(os_id)["status"] == "REALIZADA", instalacao(os_id)["status"])
    at = atendimentos(tel)
    checar("A15 conversa encerrada ao concluir o processo (ou regra explícita)", at[-1][1] == "ENCERRADO",
           f"conversa {at[-1][0]} continua {at[-1][1]}")
    checar("A16 cliente recebe mensagem de conclusão/pós-venda", any("conclu" in m.lower() or "obrigad" in m.lower() for m in gw_para(tel)[-2:]),
           ultima_msg_bot(tel))


def cenario_b():
    tel = novo_telefone()
    cli = novo_cliente(e, tel)
    orc = novo_orcamento(e, cli)
    propor_medicao(e, orc, futuro(3, 14))
    e.cliente_diz(tel, "não")
    e.cliente_diz(tel, "pode ser dia 20 às 10h")
    nova = futuro(5, 10)
    e.admin.put(f"/api/orcamentos/{orc}/medicao/aceitar-contraproposta", {"dataAgendada": nova})
    m = medicao(e, orc)
    checar("B1 medição confirmada na data aceita", m["status"] == "AGENDADA" and m["dataAgendada"] == nova, m)
    hist = historico_orcamento(orc)
    checar("B2 histórico mostra a sequência completa", len([h for h in hist if "medição" in h.lower() or "data" in h.lower()]) >= 4, hist)


def cenario_c():
    tel = novo_telefone()
    cli = novo_cliente(e, tel)
    orc = novo_orcamento(e, cli)
    d1, d3 = futuro(3, 14), futuro(6, 15)
    propor_medicao(e, orc, d1)
    e.cliente_diz(tel, "não")
    e.cliente_diz(tel, "dia 12 às 10h")
    e.admin.put(f"/api/orcamentos/{orc}/medicao/reagendar", {"dataAgendada": d3})
    e.cliente_diz(tel, "sim")
    m = medicao(e, orc)
    checar("C1 termina AGENDADA na 3ª data", m["status"] == "AGENDADA" and m["dataAgendada"] == d3, m)
    hist = historico_orcamento(orc)
    for i, h in enumerate(hist):
        print("     ", i, h)
    checar("C2 histórico cita a contraproposta recusada pela empresa", any("12" in h and "recus" in h.lower() for h in hist),
           "a recusa da empresa só aparece como 'Nova data proposta: de <data da EMPRESA> para ...'")


def cenario_d():
    tel = novo_telefone()
    cli = novo_cliente(e, tel)
    orc = orcamento_enviado(e, cli)
    e.cliente_diz(tel, "sim")
    checar("D1 orçamento aprovado", orcamento(e, orc)["status"] == "APROVADO")
    r = e.admin.post("/api/ordens-servico", {"orcamentoId": orc})
    os_id = r.json()["id"]
    r = e.admin.post(f"/api/ordens-servico/{os_id}/instalacao", {"dataAgendada": futuro(9, 9), "equipeResponsavel": "Equipe 2"})
    checar("D2 com produção obrigatória, agendar antes da produção é bloqueado com mensagem clara", r.status_code >= 400, r.text[:120])
    for acao in ("iniciar-producao", "concluir-producao", "conferir-producao"):
        e.admin.post(f"/api/ordens-servico/{os_id}/{acao}")
    r = e.admin.post(f"/api/ordens-servico/{os_id}/instalacao", {"dataAgendada": futuro(9, 9), "equipeResponsavel": "Equipe 2"})
    checar("D3 instalação proposta", r.ok and instalacao(os_id)["status"] == "PROPOSTA_ENVIADA", r.text[:100])
    e.cliente_diz(tel, "sim")
    checar("D4 instalação AGENDADA após o 'sim'", instalacao(os_id)["status"] == "AGENDADA")
    mysql(f"update instalacao set data_agendada = '{agora_brt(-60)}' where ordem_servico_id={os_id}")
    r = e.admin.post(f"/api/ordens-servico/{os_id}/instalacao/realizar", {})
    checar("D5 instalação concluída", instalacao(os_id)["status"] == "REALIZADA")
    r = e.admin.post(f"/api/ordens-servico/{os_id}/instalacao", {"dataAgendada": futuro(12, 9)})
    checar("D6 não cria segunda instalação para a mesma OS", r.status_code >= 400, r.status_code)
    tel2 = novo_telefone()
    cli2 = novo_cliente(e, tel2)
    orc2 = orcamento_enviado(e, cli2)
    e.admin.post(f"/api/orcamentos/{orc2}/aprovar")
    codigos = []
    ths = [threading.Thread(target=lambda: codigos.append(e.admin.post("/api/ordens-servico", {"orcamentoId": orc2}).status_code)) for _ in range(3)]
    [t.start() for t in ths]; [t.join() for t in ths]
    n = mysql(f"select count(*) from ordem_servico where orcamento_id={orc2}")[0][0]
    checar("D7 clique triplo em 'abrir OS' cria 1 OS", n == "1", f"{n} OS criadas; HTTP {codigos}")


def cenario_e():
    tel = novo_telefone()
    cli = novo_cliente(e, tel)
    orc = orcamento_enviado(e, cli)
    e.cliente_diz(tel, "sim")
    os_id = e.admin.post("/api/ordens-servico", {"orcamentoId": orc, "necessitaProducao": False}).json()["id"]
    e.admin.put(f"/api/ordens-servico/{os_id}/necessita-producao?valor=false")
    gw_falhar(True)
    e.admin.post(f"/api/ordens-servico/{os_id}/instalacao", {"dataAgendada": futuro(9, 9), "equipeResponsavel": "Equipe 3"})
    inst = instalacao(os_id)
    checar("E1 instalação marcada como 'proposta NÃO entregue' quando o envio falha",
           inst.get("envioStatus") not in (None, "ENVIADA", "ENTREGUE", "LIDA") and inst.get("responsavelProximaAcao") != "CLIENTE",
           f"status={inst['status']} envio={inst.get('envioStatus')} próxima ação={inst.get('proximaAcao')}")
    gw_falhar(False)
    e.conectar("CONECTADO")
    entregues = [m for m in gw_log() if m["telefone"] == tel and "agendar a instalação para" in (m["mensagem"] or "")
                 and not m["falhou"]]
    checar("E2 há reenvio/recuperação automática depois", len(entregues) == 1 and instalacao(os_id).get("envioStatus") == "ENVIADA",
           f"{len(entregues)} entregas; envio={instalacao(os_id).get('envioStatus')}")


def bot_fluxo():
    tel = novo_telefone()
    e.cliente_diz(tel, "oi")
    e.cliente_diz(tel, "Ana")
    for msg in ["oi", "não sei", "?"]:
        e.cliente_diz(tel, msg)
    at = atendimentos(tel)[-1]
    checar("BOT 3 mensagens não reconhecidas no menu escalam (não prende o cliente)", at[1] == "AGUARDANDO_ATENDENTE", at)
    tel = novo_telefone()
    for msg in ["oi", "Rafael", "1", "box", "não sei as medidas", "sim, mas quero mudar o endereço"]:
        e.cliente_diz(tel, msg)
    checar("BOT 'sim, mas...' na confirmação é tratado (não loop de 'Responda sim')", "Responda 'sim'" not in (ultima_msg_bot(tel) or ""),
           ultima_msg_bot(tel))
    tel = novo_telefone()
    for msg in ["oi", "Rafael", "1", "box", "1x2 m", "não"]:
        e.cliente_diz(tel, msg)
    checar("BOT 'não' na confirmação permite corrigir um campo sem perder tudo", "recomeçar" not in (ultima_msg_bot(tel) or ""),
           ultima_msg_bot(tel).splitlines()[0])
    tel = novo_telefone()
    for msg in ["oi", "Rafael", "1"]:
        e.cliente_diz(tel, msg)
    r = requests.post(f"{API}/api/whatsapp/webhook/midia", files={"arquivo": ("f.jpg", b"\xff\xd8\xff\xe0" + b"0" * 100, "image/jpeg")},
                      data={"telefone": tel}, headers={"Authorization": f"Bearer {e.webhook}"})
    checar("BOT foto no meio do fluxo recebe alguma resposta/orientação", len(gw_para(tel)) > 3,
           f"HTTP {r.status_code}; última resposta do bot ainda é: {ultima_msg_bot(tel)[:60]!r}")


def conversa_concorrente():
    tel = novo_telefone()
    ths = [threading.Thread(target=lambda t=t: e.cliente_diz(tel, t)) for t in ("oi", "bom dia")]
    [t.start() for t in ths]; [t.join() for t in ths]
    at = atendimentos(tel)
    abertas = [a for a in at if a[1] != "ENCERRADO"]
    checar("CONC mensagens simultâneas não criam 2 conversas abertas para o mesmo número", len(abertas) == 1,
           f"{len(abertas)} conversas abertas; saudações enviadas: {sum('Bem-vindo' in m for m in gw_para(tel))}")


def dashboard_proxima_acao():
    d = dashboard()
    chaves = sorted(d.keys())
    for chave in ["medicoesAguardandoCliente", "contrapropostasPendentes", "instalacoesAguardandoAgendamento",
                  "instalacoesHoje", "orcamentosAprovadosSemOs"]:
        checar(f"DASH indicador '{chave}'", chave in d, f"disponível: {chaves}")


def fuso_horario():
    from zoneinfo import ZoneInfo
    agora_brt = datetime.now(ZoneInfo("America/Sao_Paulo")).replace(tzinfo=None)
    alvo = (agora_brt + timedelta(minutes=90)).replace(second=0, microsecond=0)
    tel = novo_telefone()
    cli = novo_cliente(e, tel)
    orc = novo_orcamento(e, cli)
    r = propor_medicao(e, orc, alvo.strftime("%Y-%m-%dT%H:%M:%S"))
    checar("TZ medição para daqui a 90 min (horário de Brasília) é aceita", r.ok,
           f"agora BRT={agora_brt:%H:%M}, proposta {alvo:%H:%M} -> HTTP {r.status_code} {r.text[:90]}")
    orc2 = novo_orcamento(e, cli)
    d = futuro(3, 14)
    propor_medicao(e, orc2, d)
    guardado = mysql(f"select data_agendada from medicao where orcamento_id={orc2}")[0][0]
    checar("TZ banco guarda a mesma hora que o usuário escolheu", guardado == d.replace("T", " "),
           f"usuário escolheu {d}, MySQL guardou {guardado}")


for fn in [cenario_a, cenario_b, cenario_c, cenario_d, cenario_e, bot_fluxo, conversa_concorrente, dashboard_proxima_acao, fuso_horario]:
    rodar(fn)

ok = sum(1 for _, p, _ in RESULTADOS if p)
print(f"\nRESUMO: {ok} passaram / {len(RESULTADOS) - ok} falharam de {len(RESULTADOS)} verificações")
import json
json.dump(RESULTADOS, open(os.path.join(os.path.dirname(__file__) or ".", "resultado_e2e.json"), "w"),
          ensure_ascii=False, indent=1)
