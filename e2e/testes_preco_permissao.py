import os
import traceback

from vt import *

e = Empresa()
gw_reset()
func = e.criar_usuario("Funcionario", "FUNCIONARIO")
ger = e.criar_usuario("Gerente", "GERENTE")


def rodar(fn):
    print(f"\n=== {fn.__doc__.strip().splitlines()[0]}")
    try:
        fn()
    except Exception as ex:
        checar(fn.__name__ + " (erro no harness)", False, repr(ex))
        traceback.print_exc()


def totais(orc):
    return e.admin.get(f"/api/orcamentos/{orc}/totais").json()


def params():
    return e.admin.get("/api/parametros-calculo").json()


def set_param(**kw):
    p = params()
    p.update(kw)
    r = e.admin.put("/api/parametros-calculo", p)
    assert r.ok, r.text
    return r.json()


def p01():
    garantir_vidro(e, preco=300)
    tel = novo_telefone()
    cli = novo_cliente(e, tel)
    orc = novo_orcamento(e, cli)
    adicionar_item(e, orc)
    e.admin.post(f"/api/orcamentos/{orc}/aceitar-sugestoes")
    antes = totais(orc)["valorFinal"]
    margem_original = params()["percentualMargemDesejada"]
    set_param(percentualMargemDesejada=float(margem_original) + 15)
    tela = totais(orc)["valorFinal"]
    mover(e, orc, "ORCAMENTO_FINAL")
    e.admin.post(f"/api/orcamentos/{orc}/enviar")
    enviado = [m for m in gw_para(tel) if "Valor total" in m or "orçamento ficou" in m]
    set_param(percentualMargemDesejada=margem_original)
    checar("P1 valor que o usuário vê é o valor enviado ao cliente",
           enviado and f"{float(tela):,.2f}".replace(",", "X").replace(".", ",").replace("X", ".") in enviado[-1],
           f"antes={antes} tela={tela} — mensagem ao cliente: {enviado[-1] if enviado else None!r}")


def p02():
    garantir_vidro(e, preco=300)
    cli = novo_cliente(e, novo_telefone())
    orc = novo_orcamento(e, cli)
    item = adicionar_item(e, orc)
    linha_vidro = next(l for l in item["linhas"] if l["tipo"] == "VIDRO")
    e.admin.put(f"/api/orcamentos/{orc}/itens/{item['id']}/linhas/{linha_vidro['id']}", {"valorFinal": 100})
    antes = totais(orc)["valorFinal"]
    r = e.admin.put(f"/api/orcamentos/{orc}/itens/{item['id']}", {
        "tipologiaId": tipologia(e), "ambiente": "Banheiro suíte", "larguraVaoMm": 1200, "alturaVaoMm": 1900,
        "tipoVidro": "TEMPERADO", "espessuraMm": 8, "cor": "INCOLOR", "quantidade": 1, "componentes": []})
    depois = totais(orc)["valorFinal"]
    checar("P2 preço editado da linha sobrevive a uma edição do item", antes == depois,
           f"total antes={antes} depois de só renomear o ambiente={depois} (HTTP {r.status_code})")


def p03():
    cli = novo_cliente(e, novo_telefone())
    orc = novo_orcamento(e, cli)
    item = adicionar_item(e, orc, cor="BRONZE")
    tipos = [l["tipo"] for l in item["linhas"]]
    alertas = item.get("alertas") or []
    checar("P3 item sem preço de vidro gera alerta ou bloqueio", "VIDRO" in tipos or alertas,
           f"linhas={tipos} alertas={alertas} total={totais(orc)['valorFinal']}")


def p04():
    garantir_vidro(e, preco=300)
    cli = novo_cliente(e, novo_telefone())
    orc = novo_orcamento(e, cli)
    comp = [{"tipo": "KIT", "descricao": "Kit box", "quantidade": 1, "valorUnitario": 200}]
    item = adicionar_item(e, orc, qtd=3, componentes=comp)
    vidro = next(l for l in item["linhas"] if l["tipo"] == "VIDRO")
    kit = next(l for l in item["linhas"] if l["tipo"] == "KIT")
    checar("P4 quantidade 3 multiplica o kit também (ou a regra está explícita na tela)",
           float(kit["valorSugerido"]) == 600.0,
           f"vidro={vidro['valorSugerido']} (x3) kit={kit['valorSugerido']} (x1) — REGRA NÃO DEFINIDA")
    q, u, v = float(vidro["quantidade"]), float(vidro["valorUnitario"]), float(vidro["valorSugerido"])
    checar("P4 linha do vidro fecha a conta (qtd × unitário = total)", abs(q * u - v) < 0.05,
           f"{q} × {u} = {q*u:.2f} ≠ {v}")


def p05():
    garantir_vidro(e, preco=300)
    tel = novo_telefone()
    cli = novo_cliente(e, tel)
    orc = orcamento_enviado(e, cli, preco_vidro=300)
    antes = orcamento(e, orc)["valorTotal"]
    t_antes = totais(orc)
    set_param(percentualMargemDesejada=float(params()["percentualMargemDesejada"]) + 10)
    garantir_vidro(e, preco=999)
    depois = orcamento(e, orc)["valorTotal"]
    t_depois = totais(orc)
    checar("P5 valor do orçamento enviado congelado", antes == depois and t_antes["valorFinal"] == t_depois["valorFinal"],
           f"{antes} -> {depois}")
    checar("P5 custo/margem do orçamento enviado congelados", t_antes["margemReal"] == t_depois["margemReal"],
           f"{t_antes['margemReal']} -> {t_depois['margemReal']}")


def p06():
    garantir_vidro(e, preco=300)
    cli = novo_cliente(e, novo_telefone())
    orc = novo_orcamento(e, cli)
    adicionar_item(e, orc)
    e.admin.put(f"/api/orcamentos/{orc}/preco-final", {"valorFinal": 1000})
    set_param(percentualMargemDesejada=float(params()["percentualMargemDesejada"]) + 5)
    adicionar_item(e, orc, larg=500, alt=500)
    e.admin.delete(f"/api/orcamentos/{orc}/itens/{e.admin.get(f'/api/orcamentos/{orc}/itens').json()[-1]['id']}")
    checar("P6 preço final digitado (R$ 1.000) continua R$ 1.000 depois de mudar a margem",
           float(totais(orc)["valorFinal"]) == 1000.0, f"virou {totais(orc)['valorFinal']}")


def p07():
    garantir_vidro(e, preco=300)
    tel = novo_telefone()
    cli = novo_cliente(e, tel)
    orc = novo_orcamento(e, cli)
    adicionar_item(e, orc)
    nr = totais(orc)["valoresNaoRevisados"]
    mover(e, orc, "ORCAMENTO_FINAL")
    r = e.admin.post(f"/api/orcamentos/{orc}/enviar")
    checar("P7 backend bloqueia envio com valores não revisados", r.status_code >= 400,
           f"{nr} valores não revisados; HTTP {r.status_code}; status={orcamento(e, orc)['status']}")


def perm01():
    garantir_vidro(e, preco=300)
    cli = novo_cliente(e, novo_telefone())
    orc = novo_orcamento(e, cli)
    item = adicionar_item(e, orc)
    linha = item["linhas"][0]
    r = func.put(f"/api/orcamentos/{orc}/itens/{item['id']}/linhas/{linha['id']}", {"valorFinal": 1})
    checar("PERM FUNCIONARIO não edita valor de linha", r.status_code == 403, r.status_code)
    r = func.put(f"/api/orcamentos/{orc}/preco-final", {"valorFinal": 1})
    checar("PERM FUNCIONARIO não ajusta preço final", r.status_code == 403, r.status_code)
    r = func.put(f"/api/orcamentos/{orc}", {"clienteId": cli, "valorTotal": 1.00})
    checar("PERM FUNCIONARIO não define valorTotal pelo PUT do orçamento",
           r.status_code in (400, 403) and float(orcamento(e, orc)["valorTotal"] or 0) != 1.0,
           f"HTTP {r.status_code}; valorTotal agora={orcamento(e, orc)['valorTotal']}")
    r = func.post(f"/api/orcamentos/{orc}/itens", {
        "tipologiaId": tipologia(e), "ambiente": "Sala", "quantidade": 1,
        "componentes": [{"tipo": "ITEM_LIVRE", "descricao": "Serviço", "quantidade": 1, "valorUnitario": 0.01}]})
    checar("PERM FUNCIONARIO não digita preço livre em componente manual", r.status_code == 403, r.status_code)
    r = func.put("/api/parametros-calculo", params())
    checar("PERM FUNCIONARIO não altera margem/mão de obra", r.status_code == 403, r.status_code)
    r = func.post("/api/tabela-precos", {"categoria": "VIDRO", "descricao": "x", "unidade": "M2", "precoVenda": 1, "ativo": True, "origem": "MANUAL"})
    checar("PERM FUNCIONARIO não altera tabela de preços", r.status_code == 403, r.status_code)

    orc2 = novo_orcamento(e, cli)
    adicionar_item(e, orc2)
    e.admin.post(f"/api/orcamentos/{orc2}/aceitar-sugestoes")
    motor = totais(orc2)["valorFinal"]
    e.admin.put(f"/api/orcamentos/{orc2}", {"clienteId": cli, "valorTotal": 10})
    checar("PERM existe uma única fonte de verdade para o preço", float(orcamento(e, orc2)["valorTotal"]) == float(motor),
           f"calculadora={motor} x valorTotal que seria enviado={orcamento(e, orc2)['valorTotal']}")


def perm02():
    tel = novo_telefone()
    cli = novo_cliente(e, tel, sessao=func)
    orc = orcamento_enviado(e, cli)
    r = func.post(f"/api/orcamentos/{orc}/aprovar")
    checar("PERM FUNCIONARIO aprova orçamento", r.ok, r.status_code)
    r = func.get("/api/ordens-servico")
    checar("PERM FUNCIONARIO acessa ordens de serviço (necessário para agendar instalação)", r.ok, r.status_code)
    r = func.post("/api/ordens-servico", {"orcamentoId": orc, "necessitaProducao": False})
    checar("PERM FUNCIONARIO abre OS para agendar instalação", r.status_code in (200, 201), r.status_code)
    orc3 = novo_orcamento(e, cli)
    r = propor_medicao(e, orc3, sessao=func)
    checar("PERM FUNCIONARIO agenda medição", r.status_code in (200, 201), r.status_code)
    e.cliente_diz(tel, "quero falar com atendente")
    at = atendimentos(tel)[-1][0]
    r1 = func.post(f"/api/atendimentos-whatsapp/{at}/assumir")
    r2 = ger.post(f"/api/atendimentos-whatsapp/{at}/responder", {"mensagem": "Oi, sou o gerente"})
    checar("PERM outro usuário consegue assumir/responder se o atendente sumir", r2.ok,
           f"assumir={r1.status_code}; gerente responder={r2.status_code} {r2.text[:90]}")
    r3 = func.post(f"/api/atendimentos-whatsapp/{at}/encerrar")
    if r3.status_code == 409 and "PENDENCIAS_ABERTAS" in r3.text:
        r3 = func.post(f"/api/atendimentos-whatsapp/{at}/encerrar", {"forcar": True})
    checar("PERM encerrar conversa (TODOS)", r3.ok, r3.status_code)
    r4 = func.post(f"/api/atendimentos-whatsapp/{at}/reabrir")
    checar("PERM reabrir conversa (TODOS) existe", r4.status_code < 400, r4.status_code)


def integ01():
    tel = novo_telefone()
    cli = novo_cliente(e, tel)
    orc = novo_orcamento(e, cli)
    r = mover(e, orc, "MEDIDO")
    checar("INTEG orçamento não vai para 'Medido' sem medição realizada", r.status_code >= 400,
           f"HTTP {r.status_code} status={orcamento(e, orc)['status']}")
    orc2 = novo_orcamento(e, cli)
    propor_medicao(e, orc2)
    r = e.admin.post(f"/api/orcamentos/{orc2}/mover-pipeline", {"status": "PERDIDO", "motivoPerda": "DESISTIU_DO_SERVICO"})
    m = medicao(e, orc2)
    checar("INTEG perder o orçamento cancela/sinaliza a medição pendente", m["status"] != "PROPOSTA_ENVIADA",
           f"orçamento PERDIDO e medição {m['status']}")
    e.cliente_diz(tel, "sim")
    checar("INTEG cliente não confirma visita de orçamento perdido", medicao(e, orc2)["status"] != "AGENDADA",
           medicao(e, orc2)["status"])
    orc3 = novo_orcamento(e, cli)
    propor_medicao(e, orc3)
    r = e.admin.delete(f"/api/orcamentos/{orc3}")
    checar("INTEG excluir orçamento com medição devolve erro de negócio (4xx), não 500", r.status_code < 500,
           f"HTTP {r.status_code} {r.text[:80]}")
    r = e.admin.delete(f"/api/clientes/{cli}")
    checar("INTEG excluir cliente com orçamentos devolve erro de negócio (4xx), não 500", r.status_code < 500,
           f"HTTP {r.status_code} {r.text[:80]}")


for fn in [p01, p02, p03, p04, p05, p06, p07, perm01, perm02, integ01]:
    rodar(fn)

ok = sum(1 for _, p, _ in RESULTADOS if p)
print(f"\nRESUMO: {ok} passaram / {len(RESULTADOS) - ok} falharam de {len(RESULTADOS)} verificações")
import json
json.dump(RESULTADOS, open(os.path.join(os.path.dirname(__file__) or ".", "resultado_preco.json"), "w"),
          ensure_ascii=False, indent=1)
