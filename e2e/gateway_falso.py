import json
import os
import threading
import time
import uuid
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

enviadas = []
por_chave = {}
estado = {"falhar": False, "erro500": False, "semWhatsapp": [], "atrasarProximaSegundos": 0}
lock = threading.Lock()


class Handler(BaseHTTPRequestHandler):

    def log_message(self, *args):
        pass

    def _json(self, codigo, corpo=None):
        dados = json.dumps(corpo if corpo is not None else {}).encode()
        self.send_response(codigo)
        self.send_header("Content-Type", "application/json")
        self.send_header("Content-Length", str(len(dados)))
        self.end_headers()
        self.wfile.write(dados)

    def _ler_corpo(self):
        if self.headers.get("Transfer-Encoding", "").lower() == "chunked":
            corpo = b""
            while True:
                tamanho = int(self.rfile.readline().strip() or b"0", 16)
                if tamanho == 0:
                    self.rfile.readline()
                    return corpo
                corpo += self.rfile.read(tamanho)
                self.rfile.readline()
        tamanho = int(self.headers.get("Content-Length", 0))
        return self.rfile.read(tamanho) if tamanho else b""

    def do_GET(self):
        if self.path == "/log":
            with lock:
                return self._json(200, enviadas)
        if self.path.startswith("/sessoes/") and self.path.endswith("/qr"):
            return self._json(200, {"qr": "data:image/png;base64,QRFALSO"})
        self._json(404)

    def do_POST(self):
        corpo = self._ler_corpo()

        if self.path == "/reset":
            with lock:
                enviadas.clear()
                por_chave.clear()
                estado.update({"falhar": False, "erro500": False, "semWhatsapp": [], "atrasarProximaSegundos": 0})
            return self._json(200)

        if self.path == "/modo":
            estado.update(json.loads(corpo or b"{}"))
            return self._json(200, estado)

        if self.path.startswith("/sessoes/"):
            return self._json(202)

        if self.path == "/mensagens/enviar":
            dados = json.loads(corpo or b"{}")
            chave = dados.get("idempotencyKey")
            telefone = dados.get("telefone")
            with lock:
                atraso = estado.get("atrasarProximaSegundos") or 0
                estado["atrasarProximaSegundos"] = 0
            if atraso:
                time.sleep(atraso)
            with lock:
                if chave and chave in por_chave:
                    return self._json(200, {"mensagemId": por_chave[chave]})
                sem_whatsapp = telefone in estado.get("semWhatsapp", [])
                falhou = estado["falhar"] or estado.get("erro500") or sem_whatsapp
                mensagem_id = None if falhou else "3EB0" + uuid.uuid4().hex[:16].upper()
                enviadas.append({
                    "telefone": telefone,
                    "mensagem": dados.get("mensagem"),
                    "falhou": falhou,
                    "mensagemId": mensagem_id,
                    "idempotencyKey": chave,
                })
                if mensagem_id and chave:
                    por_chave[chave] = mensagem_id
            if estado["falhar"]:
                return self._json(409, {"erro": "Nenhuma sessão ativa para esta instância"})
            if sem_whatsapp:
                return self._json(422, {"erro": "Este número não tem WhatsApp"})
            if estado.get("erro500"):
                return self._json(500, {"erro": "Falha temporária"})
            return self._json(200, {"mensagemId": mensagem_id})

        self._json(404)


if __name__ == "__main__":
    porta = int(os.environ.get("GATEWAY_FALSO_PORTA", "3333"))
    ThreadingHTTPServer(("127.0.0.1", porta), Handler).serve_forever()
