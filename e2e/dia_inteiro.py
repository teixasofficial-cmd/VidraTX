import os
import random
import threading
import time
import uuid
from collections import Counter
from concurrent.futures import ThreadPoolExecutor

from vt import *

e = Empresa()
random.seed(7)
gw_reset()
ger = e.criar_usuario("Gerente", "GERENTE")
func = e.criar_usuario("Funcionario", "FUNCIONARIO")
garantir_vidro(e, preco=280)

erros_http = Counter()
lock = threading.Lock()


def diz(tel, texto, duplicar=False):
    mensagem_id = "WA" + uuid.uuid4().hex[:18].upper()
    for _ in range(2 if duplicar else 1):
        r = e.cliente_diz(tel, texto, mensagem_id=mensagem_id)
        if r.status_code >= 500:
            with lock:
                erros_http[f"webhook {r.status_code}"] += 1
        time.sleep(random.uniform(0.05, 0.3))


def captacao(tel, nome, duplicar=False):
    for msg in ["oi", nome, "1", random.choice(["box de banheiro", "espelho", "janela"]),
                "mais ou menos 1,20 x 1,90, rua X 123", "sim"]:
        diz(tel, msg, duplicar)


PERFIS = {
    "perfeito": ["sim"],
    "informal": [random.choice(["ok", "pode ser", "👍", "Sim!", "combinado"])],
    "negociador": ["não", "pode ser dia 20 às 10h?"],
    "silencioso": [],
    "fora_de_contexto": ["qual o valor do box? e parcela?"],
    "duplicado": ["sim"],
    "recusa_educada": ["não aceito esse horário, só depois das 18h"],
}

NOMES = ["Ana Souza", "Bruno Lima", "Carla Dias", "Diego Rocha", "Elisa Prado", "Fabio Nunes", "Gisele Alves",
         "Hugo Freitas", "Iara Moura", "Joao Pedro Castro", "Karen Lopes", "Luiz Teixeira", "Marina Costa", "Nelson Ramos"]

clientes = []
for i, perfil in enumerate(list(PERFIS) * 2):
    clientes.append({"tel": novo_telefone(), "nome": NOMES[i], "perfil": perfil})


def jornada_cliente(c):
    captacao(c["tel"], c["nome"], duplicar=c["perfil"] == "duplicado")


with ThreadPoolExecutor(max_workers=8) as pool:
    list(pool.map(jornada_cliente, clientes))

sols = e.admin.get("/api/solicitacoes-orcamento").json()
usuarios = [e.admin, ger, func]
slot_comum = futuro(2, 9)


def preparar(c_idx):
    c = clientes[c_idx]
    cli_ids = [s for s in sols if s.get("clienteNome") == c["nome"]]
    sessao = usuarios[c_idx % 3]
    if not cli_ids:
        c["erro"] = "sem solicitação (nome salvo diferente)"
        return
    r = sessao.post(f"/api/solicitacoes-orcamento/{cli_ids[0]['id']}/criar-orcamento")
    c["orc"] = r.json()["id"]
    data = slot_comum if c_idx % 2 == 0 else futuro(2 + c_idx % 4, 8 + c_idx % 9)
    r = sessao.post(f"/api/orcamentos/{c['orc']}/medicao", {"dataAgendada": data, "endereco": "Rua X 123"})
    with lock:
        if r.status_code >= 500:
            erros_http[f"medicao {r.status_code}"] += 1


falhas = set(random.sample(range(len(clientes)), k=len(clientes) // 3))
for idx in range(len(clientes)):
    gw_falhar(idx in falhas)
    preparar(idx)
gw_falhar(False)
e.conectar("CONECTADO")

def responder(idx):
    c = clientes[idx]
    if idx in falhas or "orc" not in c:
        return
    for msg in PERFIS[c["perfil"]]:
        diz(c["tel"], msg, duplicar=c["perfil"] == "duplicado")


with ThreadPoolExecutor(max_workers=8) as pool:
    list(pool.map(responder, range(len(clientes))))

p = e.admin.get("/api/parametros-calculo").json()
p["percentualMargemDesejada"] = float(p["percentualMargemDesejada"]) + 12
ger.put("/api/parametros-calculo", p)

mostrado, enviado = {}, {}
for idx, c in enumerate(clientes):
    if "orc" not in c:
        continue
    m = medicao(e, c["orc"])
    if m["status"] == "CONTRAPROPOSTA_CLIENTE" and c["perfil"] == "negociador":
        e.admin.put(f"/api/orcamentos/{c['orc']}/medicao/aceitar-contraproposta", {"dataAgendada": futuro(6, 10)})
        m = medicao(e, c["orc"])
    if m["status"] == "AGENDADA":
        mysql(f"update medicao set data_agendada = '{agora_brt(-60)}' where orcamento_id={c['orc']}")
        e.admin.post(f"/api/orcamentos/{c['orc']}/medicao/realizar", {})
        adicionar_item(e, c["orc"])
        e.admin.post(f"/api/orcamentos/{c['orc']}/aceitar-sugestoes")
        if idx % 3 == 0:
            p["percentualMargemDesejada"] = float(p["percentualMargemDesejada"]) - 7
            ger.put("/api/parametros-calculo", p)
        mostrado[c["orc"]] = e.admin.get(f"/api/orcamentos/{c['orc']}/totais").json()["valorFinal"]
        mover(e, c["orc"], "ORCAMENTO_FINAL")
        e.admin.post(f"/api/orcamentos/{c['orc']}/enviar")
        enviado[c["orc"]] = orcamento(e, c["orc"])["valorTotal"]

for c in clientes:
    if c.get("orc") in enviado:
        diz(c["tel"], random.choice(["sim", "Sim, aprovado!", "não aceito desconto menor?"]))
perdidos = [c for c in clientes if "orc" in c and medicao(e, c["orc"])["status"] in ("PROPOSTA_ENVIADA", "RECUSADA_CLIENTE")][:2]
for c in perdidos:
    e.admin.post(f"/api/orcamentos/{c['orc']}/mover-pipeline", {"status": "PERDIDO", "motivoPerda": "SEM_RETORNO_DO_CLIENTE"})

print("\n================ FIM DO DIA ================")
emp = mysql(f"select id from empresa where slug='{e.slug}'")[0][0]
d = e.admin.get("/api/dashboard/resumo").json()
print("Dashboard mostra:", d)


def q(sql):
    return mysql(sql.replace("{emp}", emp))


problemas = []
nomes_ruins = [n for n in q("select nome from cliente where empresa_id={emp}") if n[0] not in NOMES]
problemas.append(("Clientes com nome errado (texto da conversa salvo como nome)", len(nomes_ruins), nomes_ruins[:3]))
dup = q("select telefone, count(*) from atendimento_whatsapp where empresa_id={emp} and status<>'ENCERRADO' group by telefone having count(*)>1")
problemas.append(("Telefones com 2+ conversas abertas ao mesmo tempo", len(dup), dup[:3]))
sem_solic = [c["nome"] for c in clientes if "orc" not in c]
problemas.append(("Clientes que pediram orçamento e não viraram orçamento", len(sem_solic), sem_solic[:3]))
contra_lixo = q("select m.contraproposta_texto from medicao m join orcamento o on o.id=m.orcamento_id where o.empresa_id={emp} and m.status='CONTRAPROPOSTA_CLIENTE' and m.contraproposta_texto not regexp '[0-9]'")
problemas.append(("'Contrapropostas' que não são data (ok/👍/pergunta)", len(contra_lixo), contra_lixo[:4]))
conflito = q("select m.data_agendada, count(*) from medicao m join orcamento o on o.id=m.orcamento_id where o.empresa_id={emp} and m.status='AGENDADA' group by m.data_agendada having count(*)>1")
problemas.append(("Horários com 2+ medições confirmadas", len(conflito), conflito[:3]))
nao_entregues = q("select m.id from medicao m join orcamento o on o.id=m.orcamento_id where o.empresa_id={emp} "
                  "and m.status='PROPOSTA_ENVIADA' and coalesce((select s.status from mensagem_saida s where "
                  "s.referencia_tipo='MEDICAO' and s.referencia_id=m.id and s.categoria='PROPOSTA_DATA' order by s.id desc limit 1), '') "
                  "not in ('ENVIADA','ENTREGUE','LIDA')")
nao_entregues_visiveis = [a for a in e.admin.get("/api/dashboard/proximas-acoes").json()
                          if a["tipo"] == "MEDICAO" and a["status"] == "PROPOSTA_ENVIADA" and a["responsavel"] != "CLIENTE"]
problemas.append(("Medições 'Aguardando confirmação do cliente' cuja proposta NUNCA chegou (sem aviso)",
                  max(0, len(nao_entregues) - len(nao_entregues_visiveis)), nao_entregues[:3]))
perdido_com_medicao = q("select o.id, m.status from orcamento o join medicao m on m.orcamento_id=o.id where o.empresa_id={emp} and o.status in ('PERDIDO','EXPIRADO') and m.status in ('PROPOSTA_ENVIADA','RECUSADA_CLIENTE','CONTRAPROPOSTA_CLIENTE','AGENDADA')")
problemas.append(("Orçamentos perdidos com medição ainda em aberto", len(perdido_com_medicao), perdido_com_medicao))
aprov_sem_os = q("select o.id from orcamento o left join ordem_servico os on os.orcamento_id=o.id where o.empresa_id={emp} and o.status='APROVADO' and os.id is null")
problemas.append(("Orçamentos aprovados sem próxima etapa criada (OS/instalação)", len(aprov_sem_os), aprov_sem_os))
diverg = {k: (mostrado[k], enviado[k]) for k in enviado if float(mostrado[k]) != float(enviado[k])}
problemas.append(("Orçamentos enviados com valor diferente do que a tela mostrava", len(diverg), diverg))
pend_real = int(q("select count(*) from atendimento_whatsapp where empresa_id={emp} "
                  "and status in ('AGUARDANDO_ATENDENTE','EM_ATENDIMENTO_HUMANO') and ultima_mensagem_cliente_em is not null "
                  "and (ultima_mensagem_empresa_em is null or ultima_mensagem_cliente_em > ultima_mensagem_empresa_em)")[0][0])
problemas.append(("Conversas com cliente esperando resposta que a dashboard NÃO conta",
                  abs(pend_real - d["atendimentosPendentesWhatsapp"]), f"banco={pend_real} dashboard={d['atendimentosPendentesWhatsapp']}"))
acoes = e.admin.get("/api/dashboard/proximas-acoes").json()
precisa_empresa = q("select m.id from medicao m join orcamento o on o.id=m.orcamento_id where o.empresa_id={emp} "
                    "and m.status in ('RECUSADA_CLIENTE','CONTRAPROPOSTA_CLIENTE','REAGENDAMENTO_NECESSARIO')")
visiveis = {a["referenciaId"] for a in acoes if a["tipo"] == "MEDICAO" and a["responsavel"] == "EMPRESA"}
problemas.append(("Medições esperando decisão da EMPRESA que NÃO aparecem na dashboard",
                  len([m for m in precisa_empresa if int(m[0]) not in visiveis]), f"{len(precisa_empresa)} no banco"))
aguardando_cliente = q("select m.id from medicao m join orcamento o on o.id=m.orcamento_id where o.empresa_id={emp} and m.status='PROPOSTA_ENVIADA'")
visiveis = {a["referenciaId"] for a in acoes if a["tipo"] == "MEDICAO" and a["status"] == "PROPOSTA_ENVIADA"}
problemas.append(("Medições esperando o CLIENTE que NÃO aparecem na dashboard",
                  len([m for m in aguardando_cliente if int(m[0]) not in visiveis]), f"{len(aguardando_cliente)} no banco"))
problemas.append(("Respostas 5xx em webhooks/ações", sum(erros_http.values()), dict(erros_http)))

for nome, n, det in problemas:
    print(f"- {nome}: {n}   {det if n else ''}")
import json
json.dump([(a, b, str(c)) for a, b, c in problemas] + [("dashboard", 0, str(d))], open(os.path.join(os.path.dirname(__file__) or ".", "resultado_dia.json"), "w"), ensure_ascii=False, indent=1)
