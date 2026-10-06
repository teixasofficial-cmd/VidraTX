# Guia de desenvolvimento

Como rodar o VidraTX na sua máquina, serviço por serviço. Para a visão
geral do projeto, veja o [README](../README.md). Todos os comandos abaixo
partem da raiz do repositório.

Três serviços rodam juntos:

| Serviço | Pasta | Tecnologia | O que faz |
|---|---|---|---|
| Backend | raiz (`src/`) | Java 25 + Spring Boot + MySQL | API, regras de negócio, multi-tenant |
| whatsapp-service | `whatsapp-service/` | Node + TypeScript + Baileys | Conexão real com o WhatsApp |
| dashboard | `dashboard/` | React + Vite + TypeScript | Painel administrativo (igual para todas as empresas) |

## Pré-requisitos

- **JDK 25** (`java -version` precisa mostrar 25.x — o `pom.xml` está
  fixado nessa versão, 21 ou 24 não compilam)
- **Maven**
- **Docker** (para o MySQL) — sem ele, veja o passo 1 alternativo
- **Node.js 20+** e **npm**

## Caminho rápido

Linux/Mac (com Docker) ou Windows com WSL2/Git Bash:

```bash
./dev.sh
```

Windows sem Docker (ex: "virtualisation support wasn't detected" no
Docker Desktop) — instale o MySQL Server direto
(`winget install Oracle.MySQL`) e rode:

```powershell
powershell -ExecutionPolicy Bypass -File .\dev.ps1
```

Os dois sobem backend, whatsapp-service e dashboard, criam a empresa de
teste e imprimem a URL e o login no final. `./dev.sh stop` /
`... dev.ps1 stop` derrubam tudo. Os passos abaixo são o que esses
scripts automatizam, para quem quiser rodar (ou depurar) cada serviço
manualmente.

## 1. Banco de dados

Caminho mais rápido, com Docker:

```bash
docker run --name vidratx-mysql \
  -e MYSQL_ROOT_PASSWORD=root \
  -e MYSQL_DATABASE=vidratx \
  -p 3306:3306 -d mysql:8
```

Sem Docker: instale o MySQL normalmente e crie o banco `vidratx`
(`CREATE DATABASE vidratx;`). As tabelas são criadas sozinhas pelo Flyway
na primeira vez que o backend sobe — não precisa rodar SQL manualmente.

## 2. Backend

Três variáveis de ambiente são obrigatórias (sem elas o Spring recusa
subir — de propósito, são segredos):

```bash
export DB_PASSWORD=root                                  # a senha do MySQL acima
export JWT_SECRET=$(openssl rand -hex 32)                 # assinatura dos tokens de login
export WHATSAPP_GATEWAY_TOKEN=$(openssl rand -hex 24)      # segredo entre backend e whatsapp-service
export CORS_ALLOWED_ORIGINS=http://localhost:5173          # origem do dashboard em dev
export CADASTRO_PUBLICO_HABILITADO=true                    # libera o cadastro do passo 3 (só em dev)
export API_DOCS_HABILITADO=true                            # documentação em /swagger-ui.html (só em dev)
```

Guarde o valor de `$WHATSAPP_GATEWAY_TOKEN` — ele entra de novo no passo 4.

```bash
mvn spring-boot:run
```

Sobe em `http://localhost:8080`. Verifique o log: deve aparecer
"Started VidratxApplication" sem erro de Flyway.

## 3. Criar a primeira empresa

O dashboard só tem tela de **login**, não de cadastro. Em produção quem
cria as empresas é o super admin, pelo painel em `/superadmin` (ver
[deploy/README.md](../deploy/README.md)); a rota pública `POST /auth/cadastro`
fica fechada e só responde com `CADASTRO_PUBLICO_HABILITADO=true`, como no
passo 2. Em desenvolvimento, crie a primeira empresa de teste por ela:

```bash
curl -X POST http://localhost:8080/auth/cadastro -H "Content-Type: application/json" -d '{
  "razaoSocial": "Vidraçaria Teste LTDA",
  "nomeFantasia": "Vidraçaria Teste",
  "cnpj": "11222333000181",
  "slug": "vidracaria-teste",
  "emailEmpresa": "contato@vidracariateste.com",
  "telefone": "11988887777",
  "administrador": {
    "nome": "Seu Nome",
    "email": "voce@vidracariateste.com",
    "senha": "uma-senha-com-8-ou-mais-caracteres"
  }
}'
```

O CNPJ precisa passar na validação de dígito verificador de verdade (não
é só formato) — o exemplo acima (`11.222.333/0001-81`) é um CNPJ de teste
válido, comum em exemplos como este.

### Super admin em desenvolvimento

A migration `V24` cria a conta `admin@vidratx.local` com a senha definida
fora do repositório. Para usar o painel `/superadmin` localmente, grave
uma senha sua com o mesmo codificador da aplicação (depois do `mvn package`):

```bash
HASH=$(printf '%s\n' 'senha-local-123' | java -jar target/vidratx-0.0.1-SNAPSHOT.jar gerar-hash-senha)
mysql -uroot -p vidratx -e "UPDATE administrador_global SET senha='$HASH' WHERE email='admin@vidratx.local'"
```

Depois entre em `http://localhost:5173/superadmin/login`. Em produção, use
o script do [deploy/README.md](../deploy/README.md) (passo 5).

## 4. whatsapp-service

```bash
cd whatsapp-service
cp .env.example .env
```

Edite o `.env`: `GATEWAY_TOKEN` precisa ser **exatamente igual** ao
`WHATSAPP_GATEWAY_TOKEN` exportado no passo 2. `SPRING_BASE_URL` já vem
certo (`http://localhost:8080`) se o backend estiver na mesma máquina.

```bash
npm install
npm run dev
```

## 5. dashboard

```bash
cd dashboard
cp .env.example .env.local
npm install
npm run dev
```

Abra `http://localhost:5173/entrar` e entre com `vidracaria-teste` /
`voce@vidracariateste.com` / a senha que você cadastrou no passo 3.

## 6. Conectando o WhatsApp de verdade

Já dá para fazer isso direto no dashboard: logado como ADMIN ou GERENTE,
vá em **Configurações → WhatsApp** e clique em **Conectar WhatsApp**. A
tela avisa sozinha quando conectar (fica se atualizando a cada poucos
segundos enquanto está "Conectando...").

O QR code aparece **na própria tela** (e também no terminal onde o
`whatsapp-service` está rodando). Escaneie com o WhatsApp do número que a
vidraçaria vai usar (Aparelhos conectados → Conectar um aparelho). Assim que conectar, mande
uma mensagem de um celular qualquer para esse número — ela deve aparecer
como pendente no dashboard em `/atendimentos` em poucos segundos.

## Testando sem WhatsApp de verdade

Para testar o fluxo de atendimento sem depender de um número real e um
celular por perto, simule uma mensagem chegando direto no webhook do
backend. Troque `TOKEN_DA_INSTANCIA` pelo token da instância de WhatsApp
da empresa, que só fica no banco (a instância é criada na primeira vez
que alguém abre Configurações → WhatsApp no dashboard, sem precisar
conectar):

```bash
mysql -uroot -proot vidratx -e "SELECT webhook_token FROM whatsapp_instancia"
```

```bash
curl -X POST http://localhost:8080/api/whatsapp/webhook/mensagens \
  -H "Authorization: Bearer TOKEN_DA_INSTANCIA" \
  -H "Content-Type: application/json" \
  -d '{"telefone": "5511999999999", "mensagem": "oi"}'
```

Repita trocando o valor de `mensagem` para ir avançando na conversa (seu
nome, depois `1`, depois o serviço desejado, etc. — ver
[`FluxoAtendimentoService.java`](../src/main/java/br/com/vidratx/service/FluxoAtendimentoService.java)
para o roteiro completo).

## Produção

Um servidor com Docker, um domínio e HTTPS automático: o passo a passo
completo, com backups, monitoramento e atualização, está em
[deploy/README.md](../deploy/README.md). Antes de liberar para as
vidraçarias, faça o [piloto com WhatsApp real](../deploy/PILOTO_WHATSAPP.md).

## Testes e CI

- Backend: `mvn verify` (precisa do MySQL do passo 1 e das variáveis do
  passo 2).
- dashboard: `npm run lint && npm test && npm run build`.
- whatsapp-service: `npm test`.
- Roteiros de ponta a ponta com um WhatsApp simulado: [e2e/README.md](../e2e/README.md).

O GitHub Actions ([`ci.yml`](../.github/workflows/ci.yml)) roda tudo isso em
cada push e pull request, confere os scripts de deploy e monta as imagens
Docker. Os roteiros de ponta a ponta rodam nos pull requests, no branch
principal e sob demanda (Actions → CI → Run workflow).
