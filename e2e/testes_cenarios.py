import json
import threading
import time
import uuid
from datetime import datetime, timedelta
from zoneinfo import ZoneInfo

import requests

import vt
from vt import checar, gw_para, mysql


def esperar_bot(tel, antes, segundos=4.0):
    fim = time.time() + segundos
    novas = []
    while time.time() < fim:
        novas = gw_para(tel)[antes:]
        if novas:
            time.sleep(0.3)
            return gw_para(tel)[antes:]
        time.sleep(0.1)
    return novas


def diz(e, tel, texto, **kw):
    antes = len(gw_para(tel))
    r = e.cliente_diz(tel, texto, **kw)
    assert r.ok, r.text
    return esperar_bot(tel, antes)


def conversa(e, tel, textos):
    return [(t, diz(e, tel, t)) for t in textos]


def texto(respostas):
    return " | ".join(" ".join(r) for _, r in respostas) if respostas and isinstance(respostas[0], tuple) \
        else " ".join(respostas or [])


def brl(valor):
    return f"R$\xa0{valor:,.2f}".replace(",", "X").replace(".", ",").replace("X", ".")


vt.gw_reset()

A = vt.Empresa()
B = vt.Empresa()
t8, box_instalado = 260.0, 900.0
kit = round(box_instalado - 2.28 * t8, 2)
ids = {}
for it in [
    {"categoria": "VIDRO", "descricao": "Temperado 8mm incolor", "tipoVidro": "TEMPERADO", "espessuraMm": 8,
     "cor": "INCOLOR", "unidade": "M2", "precoVenda": t8, "origem": "MANUAL", "ativo": True},
    {"categoria": "VIDRO", "descricao": "Espelho 4mm", "tipoVidro": "ESPELHO", "espessuraMm": 4,
     "cor": "INCOLOR", "unidade": "M2", "precoVenda": 190, "origem": "MANUAL", "ativo": True},
    {"categoria": "KIT", "descricao": "Kit + mão de obra (box frontal, calibrado)", "unidade": "KIT",
     "precoVenda": kit, "origem": "DERIVADO", "ativo": True},
]:
    r = A.admin.post("/api/tabela-precos", it)
    assert r.status_code < 300, r.text
    ids[it["descricao"]] = r.json()["id"]
param = A.admin.get("/api/parametros-calculo").json()
param.update({"modoPrecificacao": "VENDA", "percentualImpostos": 8, "percentualMargemDesejada": 30,
              "percentualMargemMinima": 20, "arredondamentoComercial": "TERMINAR_90",
              "taxaCartaoParcelas": {"1": 3}, "regimeTributario": "SIMPLES_NACIONAL"})
r = A.admin.put("/api/parametros-calculo", param)
assert r.ok, r.text
box_id = vt.tipologia(A, "Box frontal")
espelho_id = vt.tipologia(A, "Espelho")
KIT_BOX = {"tipo": "KIT", "descricao": "Kit + mão de obra", "quantidade": 1,
           "tabelaPrecoId": ids["Kit + mão de obra (box frontal, calibrado)"]}


def item_box(orc, qtd=1, componentes=True, **extra):
    corpo = {"tipologiaId": box_id, "ambiente": "Banheiro", "larguraVaoMm": 1200, "alturaVaoMm": 1900,
             "tipoVidro": "TEMPERADO", "espessuraMm": 8, "cor": "INCOLOR", "quantidade": qtd,
             "componentes": [KIT_BOX] if componentes else []}
    corpo.update(extra)
    return A.admin.post(f"/api/orcamentos/{orc}/itens", corpo)


def enviar(orc):
    A.admin.post(f"/api/orcamentos/{orc}/aceitar-sugestoes")
    vt.mover(A, orc, "ORCAMENTO_FINAL")
    return A.admin.post(f"/api/orcamentos/{orc}/enviar")


F1 = vt.novo_telefone()
c1 = conversa(A, F1, ["Oi, quero orçamento de box"])
etapa_depois_da_1a = vt.atendimentos(F1)
c1 += conversa(A, F1, ["Carlos Souza"])
etapa_depois_do_nome = vt.atendimentos(F1)[-1][2]
checar("1c a primeira mensagem ('quero orçamento de box') é aproveitada: depois do nome o bot já pergunta "
       "as medidas, sem voltar ao menu nem perguntar o serviço de novo",
       etapa_depois_do_nome == "COLETA_DESCRICAO", f"etapa depois do nome: {etapa_depois_do_nome}; bot: {texto(c1)}")
if etapa_depois_do_nome != "COLETA_DESCRICAO":
    if etapa_depois_do_nome == "MENU":
        c1 += conversa(A, F1, ["1"])
    c1 += conversa(A, F1, ["box frontal de banheiro"])
c1 += conversa(A, F1, ["1,20 x 1,90, vidro incolor 8 mm, ferragem preta. Bairro Vila Mariana, São Paulo"])
confirmacao = " ".join(c1[-1][1])
checar("2c o bot confirma a medida que entendeu antes de enviar o pedido",
       "1,20 m" in confirmacao and "1,90 m" in confirmacao, confirmacao[:300])
c1 += conversa(A, F1, ["sim"])
json.dump([{"cliente": t, "bot": r} for t, r in c1], open("resultado_cenarios_conversa1.json", "w"),
          ensure_ascii=False, indent=1)
checar("1a nenhum valor vai para o consumidor durante o pedido", "R$" not in texto(c1), texto(c1)[:300])
cliente1 = int(vt.atendimentos(F1)[-1][3])
orcs1 = [o for o in A.admin.get("/api/orcamentos").json() if o.get("clienteId") == cliente1]
tot1 = A.admin.get(f"/api/orcamentos/{orcs1[0]['id']}/totais").json() if orcs1 else {}
checar("1b o pedido chega à dashboard como orçamento em rascunho com o valor sugerido calculado",
       bool(orcs1) and (tot1.get("precoSugerido") or 0) > 0,
       f"{len(orcs1)} orçamento(s); sugerido {tot1.get('precoSugerido')}")
nome1 = mysql(f"select nome from cliente where id={cliente1}")[0][0]
checar("1c' o nome do cliente é o que ele informou", nome1 == "Carlos Souza", nome1)

orc1 = orcs1[0]["id"] if orcs1 else vt.novo_orcamento(A, cliente1)
itens1 = A.admin.get(f"/api/orcamentos/{orc1}/itens").json()
if not itens1:
    item_box(orc1)
elif not any(l.get("tipo") == "KIT" for i in itens1 for l in i.get("linhas", [])):
    i0 = itens1[0]
    A.admin.put(f"/api/orcamentos/{orc1}/itens/{i0['id']}", {
        "tipologiaId": i0["tipologiaId"], "ambiente": i0.get("ambiente"), "larguraVaoMm": i0["larguraVaoMm"],
        "alturaVaoMm": i0["alturaVaoMm"], "tipoVidro": i0["tipoVidro"], "espessuraMm": i0["espessuraMm"],
        "cor": i0["cor"], "quantidade": i0["quantidade"], "componentes": [KIT_BOX]})
tot = A.admin.get(f"/api/orcamentos/{orc1}/totais").json()
checar("1d preço sugerido do box que a vidraçaria vende a R$ 900 (preço de venda direto) fica perto de R$ 900",
       abs(tot["precoSugerido"] - box_instalado) < 100, f"sugerido R$ {tot['precoSugerido']}")

agenda_a = A.admin.get("/api/empresa/agenda").json()
A.admin.put("/api/empresa/agenda", dict(agenda_a, condicoesPagamento="PIX, dinheiro ou cartão em até 10x"))
param2 = dict(param, percentualMargemDesejada=20, percentualMargemMinima=10)
A.admin.put("/api/parametros-calculo", param2)
tela = A.admin.get(f"/api/orcamentos/{orc1}/totais").json()["valorFinal"]
antes = len(gw_para(F1))
r = enviar(orc1)
msg = esperar_bot(F1, antes)
checar("1e o valor que o consumidor recebe é o valor da tela no momento do envio",
       r.ok and any(brl(tela) in m for m in msg), f"HTTP {r.status_code}; tela {brl(tela)}; mensagem {msg}")
m1 = " ".join(msg)
checar("1f a mensagem do orçamento traz itens, validade e formas de pagamento",
       "×" in m1 and "Válido até" in m1 and "pagamento" in m1.lower(), m1[:400])

orc_med = vt.novo_orcamento(A, cliente1)
medidas = {}
for t in ["1,20 x 1,90", "120x190", "1.20 por 1.90", "120 x 1,90", "1m20 x 1m90", "1200 x 1900",
          "10 x 10", "0 x 0", "abc", "900 x 900"]:
    r = A.admin.post(f"/api/orcamentos/{orc_med}/itens", {
        "tipologiaId": box_id, "medidaTexto": t, "tipoVidro": "TEMPERADO", "espessuraMm": 8, "cor": "INCOLOR",
        "quantidade": 1})
    medidas[t] = f"{r.json().get('larguraVaoMm')} x {r.json().get('alturaVaoMm')}" if r.ok else f"HTTP {r.status_code}"
print(json.dumps(medidas, ensure_ascii=False))
checar("2 os formatos brasileiros viram 1200 x 1900 mm",
       all(medidas[t] == "1200 x 1900" for t in ["1,20 x 1,90", "120x190", "1.20 por 1.90", "120 x 1,90"]),
       str(medidas))
checar("2b '1m20 x 1m90' e '1200 x 1900' (em mm) viram 1200 x 1900 mm",
       medidas["1m20 x 1m90"] == "1200 x 1900" and medidas["1200 x 1900"] == "1200 x 1900", str(medidas))
checar("14 '0 x 0', 'abc' e '900 x 900' (9 m) são recusados; '10 x 10' vira 100 x 100 mm",
       all(medidas[t].startswith("HTTP 4") for t in ["0 x 0", "abc", "900 x 900"])
       and medidas["10 x 10"] == "100 x 100", str(medidas))

orc_q = vt.novo_orcamento(A, cliente1)
linhas = item_box(orc_q, qtd=3).json().get("linhas", [])
kit_linha = next((l for l in linhas if l["tipo"] == "KIT"), {})
checar("extra quantidade 3: o kit é cobrado 3 vezes, como o vidro",
       abs((kit_linha.get("valorSugerido") or 0) - 3 * kit) < 0.05, f"kit = {kit_linha.get('valorSugerido')}")

F3 = vt.novo_telefone()
conversa(A, F3, ["oi", "Bruno Lima", "1"])
r3 = diz(A, F3, "", midia="AUDIO")
checar("3 áudio no meio do pedido: o bot avisa que não ouve áudio e pede por escrito",
       bool(r3) and "escrev" in " ".join(r3).lower(), str(r3))

F4 = vt.novo_telefone()
conversa(A, F4, ["oi", "Marta Lima", "1"])
antes = len(gw_para(F4))
r = requests.post(vt.API + "/api/whatsapp/webhook/midia", headers={"Authorization": "Bearer " + A.webhook},
                  files={"arquivo": ("banheiro.jpg", b"\xff\xd8\xff\xe0" + b"0" * 200, "image/jpeg")},
                  data={"telefone": F4, "legenda": "foto do banheiro", "mensagemId": "WA" + uuid.uuid4().hex[:16]})
r4 = esperar_bot(F4, antes)
checar("4 foto do banheiro no meio do pedido: guardada e o bot confirma e segue",
       r.ok and bool(r4) and "foto" in " ".join(r4).lower(), f"HTTP {r.status_code}; {r4}")

rf = A.admin.post("/api/perguntas-frequentes", {
    "pergunta": "Vocês fazem espelho sob medida?", "palavrasChave": "espelho sob medida, espelho",
    "resposta": "Fazemos sim! Espelhos sob medida de 3 e 4 mm, com lapidação e instalação.", "ativa": True})
A.admin.post("/api/perguntas-frequentes", {
    "pergunta": "Atendem no meu bairro?", "palavrasChave": "bairro, atendem, regiao, região",
    "resposta": "Atendemos toda a zona sul e o centro de São Paulo.", "ativa": True})
checar("5' a vidraçaria cadastra as dúvidas frequentes pela API/tela", rf.status_code in (200, 201),
       f"HTTP {rf.status_code}")
F5 = vt.novo_telefone()
d1 = diz(A, F5, "vocês fazem espelho sob medida?")
checar("5a primeira mensagem com uma dúvida cadastrada recebe a resposta cadastrada",
       "Fazemos sim" in " ".join(d1), str(d1))
diz(A, F5, "Ana Paula")
d2 = diz(A, F5, "atendem no meu bairro?")
checar("5b dúvida no menu recebe a resposta cadastrada", "zona sul" in " ".join(d2), str(d2))
d3 = diz(A, F5, "4")
d4 = diz(A, F5, "vocês parcelam no cartão?")
at5 = vt.atendimentos(F5)[-1]
checar("5c dúvida sem resposta cadastrada vai para um atendente (e não 'Não entendi')",
       at5[1] == "AGUARDANDO_ATENDENTE" and "Não entendi" not in " ".join(d3 + d4), f"{at5}; {d3} {d4}")

r6 = diz(A, F1, "e aquele orçamento?")
checar("6 consumidor volta: 'e aquele orçamento?' (orçamento Enviado) recebe a situação e o valor",
       bool(r6) and "Não entendi" not in " ".join(r6) and "R$" in " ".join(r6), str(r6))
r7 = diz(A, F1, "quero falar com uma pessoa")
at1 = vt.atendimentos(F1)[-1]
checar("7a 'quero falar com uma pessoa' com orçamento Enviado em aberto: transfere",
       at1[1] == "AGUARDANDO_ATENDENTE" and "Não entendi" not in " ".join(r7), f"{at1}; {r7}")
F7 = vt.novo_telefone()
conversa(A, F7, ["oi", "Paulo Reis"])
p7 = diz(A, F7, "quero falar com uma pessoa")
at7 = vt.atendimentos(F7)[-1]
A.admin.post(f"/api/atendimentos-whatsapp/{at7[0]}/assumir")
depois7 = diz(A, F7, "oi, alguém aí?")
checar("7b pede pessoa, vidraceiro assume e o bot fica quieto", bool(p7) and not depois7, f"{p7} / {depois7}")

F8 = vt.novo_telefone()
conversa(A, F8, ["oi", "Rita Alves", "1", "box", "1,20 x 1,90", "sim"])
r8 = diz(A, F8, "quero também um orçamento de espelho 1,00 x 0,80")
at8 = vt.atendimentos(F8)[-1]
msgs8 = mysql("select count(*) from mensagem_atendimento where atendimento_id=%s and conteudo like '%%espelho 1,00%%'"
              % at8[0])[0][0]
checar("8 segundo pedido (espelho) com o primeiro na fila: fica na conversa do atendente, que continua pendente",
       at8[1] == "AGUARDANDO_ATENDENTE" and msgs8 == "1", f"{at8}; registrada={msgs8}; bot={r8}")

orc_antigo = vt.novo_orcamento(A, cliente1)
item_box(orc_antigo, componentes=False)
v_antigo = A.admin.get(f"/api/orcamentos/{orc_antigo}/totais").json()["valorFinal"]
enviado_antes = vt.orcamento(A, orc1)["valorTotal"]
A.admin.put(f"/api/tabela-precos/{ids['Temperado 8mm incolor']}",
            {"categoria": "VIDRO", "descricao": "Temperado 8mm incolor", "tipoVidro": "TEMPERADO", "espessuraMm": 8,
             "cor": "INCOLOR", "unidade": "M2", "precoVenda": 300, "origem": "MANUAL", "ativo": True})
v_antigo2 = A.admin.get(f"/api/orcamentos/{orc_antigo}/totais").json()["valorFinal"]
enviado_depois = vt.orcamento(A, orc1)["valorTotal"]
orc_novo = vt.novo_orcamento(A, cliente1)
item_box(orc_novo, componentes=False)
v_novo = A.admin.get(f"/api/orcamentos/{orc_novo}/totais").json()["valorFinal"]
checar("9 preço do 8 mm muda: enviado e em elaboração não mudam; novo usa o preço novo",
       enviado_antes == enviado_depois and v_antigo == v_antigo2 and v_novo > v_antigo,
       f"enviado {enviado_antes}->{enviado_depois}; elaboração {v_antigo}->{v_antigo2}; novo {v_novo}")

F10 = vt.novo_telefone()
o1 = vt.novo_orcamento(A, vt.novo_cliente(A, F10, "Cliente Dez"))
o2 = vt.novo_orcamento(A, vt.novo_cliente(A, vt.novo_telefone(), "Outro"))
quando = vt.futuro(dias=30, hora=10)
r1 = vt.propor_medicao(A, o1, quando)
time.sleep(1)
diz(A, F10, "1")
status1 = vt.medicao(A, o1).get("status")
r2 = vt.propor_medicao(A, o2, quando)
checar("10 medição num horário já confirmado: a segunda é recusada", r1.ok and status1 == "AGENDADA" and r2.status_code >= 400,
       f"1ª {r1.status_code} ({status1}); 2ª {r2.status_code} {r2.text[:120]}")

agenda = A.admin.get("/api/empresa/agenda").json()
agora = datetime.now(ZoneInfo("America/Sao_Paulo"))
fechado = dict(agenda, horaInicioMensagens=(agora.hour + 2) % 24 or 1, horaFimMensagens=(agora.hour + 3) % 24 or 24,
               mensagemForaHorario="Estamos fora do horário. Respondemos a partir das {abertura}.")
if fechado["horaFimMensagens"] <= fechado["horaInicioMensagens"]:
    fechado.update(horaInicioMensagens=1, horaFimMensagens=2)
ra = A.admin.put("/api/empresa/agenda", fechado)
F11 = vt.novo_telefone()
conversa(A, F11, ["oi", "Lucas Prado"])
r11 = diz(A, F11, "quero falar com uma pessoa")
checar("11 fora do horário: o cliente é avisado de quando alguém responde (horário configurável)",
       ra.ok and "fora do horário" in " ".join(r11).lower(), f"PUT agenda {ra.status_code}; bot: {r11}")
A.admin.put("/api/empresa/agenda", agenda)

F12 = vt.novo_telefone()
mid = "WA" + uuid.uuid4().hex[:18].upper()
A.cliente_diz(F12, "oi", mensagem_id=mid)
A.cliente_diz(F12, "oi", mensagem_id=mid)
time.sleep(1)
checar("12 a mesma mensagem entregue duas vezes é descartada",
       len(gw_para(F12)) == 1 and not mysql(f"select nome from cliente where whatsapp='{F12}'"),
       f"{len(gw_para(F12))} resposta(s)")
F12b = vt.novo_telefone()
ts = [threading.Thread(target=A.cliente_diz, args=(F12b, t)) for t in ["oi", "bom dia"]]
[t.start() for t in ts]
[t.join() for t in ts]
time.sleep(1)
checar("12b duas mensagens simultâneas do mesmo número abrem uma conversa só", len(vt.atendimentos(F12b)) == 1,
       str(vt.atendimentos(F12b)))

rb = diz(B, F1, "oi")
rb_get = B.admin.get(f"/api/orcamentos/{orc1}")
checar("13 duas vidraçarias: a Beta não vê o orçamento da Alfa; o mesmo telefone é outro cliente na Beta",
       rb_get.status_code in (403, 404) and "Carlos" not in " ".join(rb), f"HTTP {rb_get.status_code}; {rb}")

A.conectar("DESCONECTADO")
resumo = A.admin.get("/api/dashboard/resumo").json()
checar("15a WhatsApp desconecta: a tela inicial mostra a conexão",
       any("whatsapp" in k.lower() and ("conex" in k.lower() or "status" in k.lower()) for k in resumo),
       sorted(resumo.keys()))
A.conectar("CONECTADO")
vt.gw_falhar(True)
F15 = vt.novo_telefone()
c15 = vt.novo_cliente(A, F15, "Cliente Quinze")
o15 = vt.novo_orcamento(A, c15)
item_box(o15)
r = enviar(o15)
time.sleep(3)
o = vt.orcamento(A, o15)
vt.gw_falhar(False)
checar("15b envio com o WhatsApp fora: a tela mostra que a mensagem não foi entregue",
       o.get("envioStatus") not in ("ENVIADA", "ENTREGUE", "LIDA"), f"status {o['status']}, envio {o.get('envioStatus')}")

F16 = vt.novo_telefone()
c16 = vt.novo_cliente(A, F16, "Joana Dias")
o16 = vt.novo_orcamento(A, c16)
item_box(o16)
enviar(o16)
time.sleep(1)
diz(A, F16, "não aceito esse valor")
checar("extra 'não aceito esse valor' não aprova", vt.orcamento(A, o16)["status"] != "APROVADO",
       vt.orcamento(A, o16)["status"])
r = requests.get(vt.API + "/v3/api-docs")
checar("extra documentação da API fechada sem login (padrão)", r.status_code in (401, 403, 404), f"HTTP {r.status_code}")

rr = requests.post(vt.API + "/auth/renovar", headers=A.admin.h)
checar("extra a sessão do painel pode ser renovada sem digitar a senha de novo", rr.ok and "token" in rr.text,
       f"HTTP {rr.status_code}")

func = A.criar_usuario("Func", "FUNCIONARIO")
o_f = vt.novo_orcamento(A, c16)
item = item_box(o_f).json()
rf = func.put(f"/api/orcamentos/{o_f}/itens/{item['id']}/linhas/{item['linhas'][0]['id']}", {"valorFinal": 1})
checar("extra funcionário não mexe no preço (quem define é gerente/administrador)", rf.status_code == 403,
       f"HTTP {rf.status_code}")

p = A.admin.get("/api/parametros-calculo").json()
A.admin.put("/api/parametros-calculo", dict(p, regraDeslocamento="FIXO", valorDeslocamento=80))
o_d = vt.novo_orcamento(A, c16)
item_box(o_d, componentes=False)
linhas_d = [l for i in A.admin.get(f"/api/orcamentos/{o_d}/itens").json() for l in i.get("linhas", [])]
totais_d = A.admin.get(f"/api/orcamentos/{o_d}/totais").json()
checar("extra deslocamento fixo da tela de Parâmetros entra no orçamento",
       any(l["tipo"] == "DESLOCAMENTO" for l in linhas_d) or totais_d.get("deslocamento"),
       json.dumps(totais_d, ensure_ascii=False)[:200])
A.admin.put("/api/parametros-calculo", p)

rl = A.admin.post(f"/api/clientes/{c16}/anonimizar")
dados = A.admin.get(f"/api/clientes/{c16}").json() if rl.ok else {}
checar("extra LGPD: a vidraçaria apaga os dados pessoais de um cliente a pedido dele",
       rl.ok and "Joana" not in json.dumps(dados, ensure_ascii=False) and F16 not in json.dumps(dados),
       f"HTTP {rl.status_code}")

print("\nRESUMO:", sum(1 for _, ok, _ in vt.RESULTADOS if ok), "de", len(vt.RESULTADOS))
json.dump([{"verificacao": n, "passou": ok, "detalhe": d} for n, ok, d in vt.RESULTADOS],
          open("resultado_cenarios.json", "w"), ensure_ascii=False, indent=1)
