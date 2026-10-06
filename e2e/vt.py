import os
import random
import subprocess
import time
import uuid
from datetime import datetime, timedelta
from zoneinfo import ZoneInfo

import requests

API = os.environ.get("VIDRATX_API", "http://localhost:8080")
GW = os.environ.get("VIDRATX_GATEWAY_FALSO", "http://localhost:3333")

DB_HOST = os.environ.get("DB_HOST", "127.0.0.1")
DB_USER = os.environ.get("DB_USERNAME", "root")
DB_PASSWORD = os.environ.get("DB_PASSWORD", "root")
DB_NAME = os.environ.get("DB_NAME", "vidratx")


def mysql(sql):
    out = subprocess.run(["mysql", f"-h{DB_HOST}", f"-u{DB_USER}", f"-p{DB_PASSWORD}", "-N", "-B", DB_NAME, "-e", sql],
                         capture_output=True, text=True)
    return [linha.split("\t") for linha in out.stdout.strip().splitlines() if linha]


class Sessao:
    def __init__(self, token, dados):
        self.h = {"Authorization": f"Bearer {token}"}
        self.dados = dados

    def get(self, p): return requests.get(API + p, headers=self.h)
    def post(self, p, body=None): return requests.post(API + p, json=body, headers=self.h)
    def put(self, p, body=None): return requests.put(API + p, json=body, headers=self.h)
    def delete(self, p): return requests.delete(API + p, headers=self.h)


class Empresa:
    def __init__(self, sufixo=None):
        sufixo = sufixo or uuid.uuid4().hex[:6]
        self.slug = f"vid-{sufixo}"
        r = requests.post(f"{API}/auth/cadastro", json={
            "razaoSocial": f"Vidracaria {sufixo} LTDA", "nomeFantasia": f"Vidracaria {sufixo}",
            "cnpj": gerar_cnpj(), "slug": self.slug, "emailEmpresa": f"c{sufixo}@x.com",
            "telefone": "11988887777",
            "administrador": {"nome": "Admin Teste", "email": f"admin{sufixo}@x.com",
                              "senha": "senha-forte-123"}})
        assert r.status_code in (200, 201), \
            f"HTTP {r.status_code} {r.text} (o backend precisa rodar com CADASTRO_PUBLICO_HABILITADO=true)"
        self.admin = self.login(f"admin{sufixo}@x.com", "senha-forte-123")
        self.admin.post("/api/whatsapp/instancia/conectar")
        self.webhook = mysql("select wi.webhook_token from whatsapp_instancia wi join empresa e "
                             f"on e.id = wi.empresa_id where e.slug = '{self.slug}'")[0][0]
        r = requests.post(f"{API}/api/whatsapp/webhook/conexao",
                          json={"status": "CONECTADO", "numero": "5511900000000"},
                          headers={"Authorization": f"Bearer {self.webhook}"})
        assert r.ok, r.text

    def login(self, email, senha):
        r = requests.post(f"{API}/auth/login",
                          json={"empresaSlug": self.slug, "email": email, "senha": senha})
        assert r.ok, r.text
        return Sessao(r.json()["token"], r.json())

    def criar_usuario(self, nome, perfil):
        email = f"{nome.lower()}{uuid.uuid4().hex[:4]}@x.com"
        r = self.admin.post("/api/usuarios", {"nome": nome, "email": email,
                                              "senha": "senha-forte-123", "perfil": perfil})
        assert r.status_code in (200, 201), r.text
        return self.login(email, "senha-forte-123")

    def cliente_diz(self, telefone, texto, mensagem_id=None, timestamp=None, midia=None,
                    reacao_a=None, edita=None, apaga=None):
        corpo = {"telefone": telefone, "mensagem": texto,
                 "mensagemId": mensagem_id or ("WA" + uuid.uuid4().hex[:18].upper()),
                 "timestamp": int(timestamp if timestamp is not None else time.time() + 1)}
        if midia:
            corpo["midiaNaoSuportada"] = midia
        if reacao_a:
            corpo["reacaoA"] = reacao_a
        if edita:
            corpo["editaMensagemId"] = edita
        if apaga:
            corpo["apagaMensagemId"] = apaga
        return requests.post(f"{API}/api/whatsapp/webhook/mensagens", json=corpo,
                             headers={"Authorization": f"Bearer {self.webhook}"})

    def conectar(self, status):
        return requests.post(f"{API}/api/whatsapp/webhook/conexao", json={"status": status},
                             headers={"Authorization": f"Bearer {self.webhook}"})


def gw_reset(): requests.post(f"{GW}/reset")
def gw_falhar(v): requests.post(f"{GW}/modo", json={"falhar": v})
def gw_modo(**kw): requests.post(f"{GW}/modo", json=kw)
def gw_log(): return requests.get(f"{GW}/log").json()
def gw_para(tel): return [m["mensagem"] for m in gw_log() if m["telefone"] == tel]


def gw_id(tel, trecho):
    ids = [m["mensagemId"] for m in gw_log() if m["telefone"] == tel and trecho in (m["mensagem"] or "")]
    return ids[-1] if ids else None


def novo_telefone():
    return "55119" + "".join(str(random.randint(0, 9)) for _ in range(8))


def gerar_cnpj():
    base = [random.randint(0, 9) for _ in range(8)] + [0, 0, 0, 1]

    def dv(nums, pesos):
        r = sum(n * p for n, p in zip(nums, pesos)) % 11
        return 0 if r < 2 else 11 - r

    base.append(dv(base, [5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2]))
    base.append(dv(base, [6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2]))
    return "".join(map(str, base))


_PROXIMO_HORARIO = [0]


def futuro(dias=None, hora=None, minutos=0):
    if dias is None and hora is None:
        n = _PROXIMO_HORARIO[0]
        _PROXIMO_HORARIO[0] += 1
        dias, hora = 20 + n // 10, 8 + n % 10
    dias = 3 if dias is None else dias
    hora = 14 if hora is None else hora
    d = (datetime.now() + timedelta(days=dias)).replace(hour=hora, minute=minutos, second=0, microsecond=0)
    return d.strftime("%Y-%m-%dT%H:%M:%S")


def agora_brt(minutos=0):
    d = datetime.now(ZoneInfo("America/Sao_Paulo")) + timedelta(minutes=minutos)
    return d.strftime("%Y-%m-%d %H:%M:%S")


def br(iso):
    d = datetime.strptime(iso, "%Y-%m-%dT%H:%M:%S")
    return d.strftime("%d/%m/%Y às %H:%M")


RESULTADOS = []


def checar(nome, condicao, detalhe=""):
    RESULTADOS.append((nome, bool(condicao), detalhe))
    marca = "PASSA" if condicao else "FALHA"
    print(f"[{marca}] {nome}" + (f" — {detalhe}" if detalhe else ""))
    return condicao


def novo_cliente(e, tel, nome="Maria Souza", sessao=None):
    s = sessao or e.admin
    r = s.post("/api/clientes", {"nome": nome, "telefone": tel, "whatsapp": tel})
    assert r.status_code in (200, 201), r.text
    return r.json()["id"]


def novo_orcamento(e, cliente_id, sessao=None):
    s = sessao or e.admin
    r = s.post("/api/orcamentos", {"clienteId": cliente_id})
    assert r.status_code in (200, 201), r.text
    return r.json()["id"]


def garantir_vidro(e, preco=250, custo=150, cor="INCOLOR"):
    r = e.admin.post("/api/tabela-precos", {
        "categoria": "VIDRO", "descricao": f"Temperado 8mm {cor}", "tipoVidro": "TEMPERADO",
        "espessuraMm": 8, "cor": cor, "unidade": "M2", "custo": custo, "precoVenda": preco,
        "ativo": True, "origem": "MANUAL"})
    assert r.status_code in (200, 201), r.text
    return r.json()["id"]


def tipologia(e, nome_parcial="Box frontal"):
    for t in e.admin.get("/api/tipologias").json():
        if nome_parcial.lower() in t["nome"].lower():
            return t["id"]
    raise AssertionError("tipologia não encontrada")


def adicionar_item(e, orc, larg=1200, alt=1900, cor="INCOLOR", qtd=1, componentes=None, sessao=None):
    s = sessao or e.admin
    r = s.post(f"/api/orcamentos/{orc}/itens", {
        "tipologiaId": tipologia(e), "ambiente": "Banheiro", "larguraVaoMm": larg, "alturaVaoMm": alt,
        "tipoVidro": "TEMPERADO", "espessuraMm": 8, "cor": cor, "quantidade": qtd,
        "componentes": componentes or []})
    assert r.status_code in (200, 201), r.text
    return r.json()


def mover(e, orc, status, sessao=None):
    s = sessao or e.admin
    return s.post(f"/api/orcamentos/{orc}/mover-pipeline", {"status": status})


def orcamento_enviado(e, cliente_id, preco_vidro=250):
    garantir_vidro(e, preco=preco_vidro)
    orc = novo_orcamento(e, cliente_id)
    adicionar_item(e, orc)
    e.admin.post(f"/api/orcamentos/{orc}/aceitar-sugestoes")
    assert mover(e, orc, "ORCAMENTO_FINAL").ok
    r = e.admin.post(f"/api/orcamentos/{orc}/enviar")
    assert r.ok, r.text
    return orc


def propor_medicao(e, orc, data=None, sessao=None):
    s = sessao or e.admin
    r = s.post(f"/api/orcamentos/{orc}/medicao", {"dataAgendada": data or futuro(), "endereco": "Rua A, 100"})
    return r


def medicao(e, orc):
    return e.admin.get(f"/api/orcamentos/{orc}/medicao").json()


def orcamento(e, orc):
    return e.admin.get(f"/api/orcamentos/{orc}").json()


def historico_orcamento(orc):
    return [r[0] for r in mysql(f"select descricao from historico where orcamento_id={orc} order by id")]


def atendimentos(tel):
    return mysql(f"select id,status,etapa_fluxo,cliente_id from atendimento_whatsapp where telefone='{tel}' order by id")


def ultima_msg_bot(tel):
    msgs = gw_para(tel)
    return msgs[-1] if msgs else None
