# Colocando o VidraTX no ar

Um servidor Linux com Docker, um domínio, HTTPS automático. Todas as
vidraçarias usam o mesmo endereço (por exemplo `https://painel.suaempresa.com.br`)
e cada uma entra com o próprio login: identificador da empresa, e-mail e
senha. Quem cria as empresas é você, pelo painel do super admin.

```
                     internet (portas 80 e 443)
                                │
                          ┌─────▼─────┐
                          │   Caddy   │  HTTPS (Let's Encrypt), painel e proxy da API
                          └─────┬─────┘
                                │ /api, /auth, /actuator/health
┌──────────────────┐      ┌─────▼─────┐      ┌─────────┐
│ whatsapp-service │◄────►│  backend  │◄────►│  MySQL  │◄── backup diário
│    (Baileys)     │      │  (Spring) │      └─────────┘
└──────────────────┘      └───────────┘
    rede interna do Docker: só o Caddy fica exposto
```

Tudo roda a partir desta pasta (`deploy/`) com `docker compose`.

## 1. Servidor e domínio

- **Servidor:** uma VPS com Ubuntu 24.04, **2 vCPUs, 4 GB de RAM e 40 GB de
  disco** atende bem o começo (Hostinger, DigitalOcean, Contabo, AWS
  Lightsail…). Com menos de 4 GB, o build das imagens pode faltar memória.
- **Domínio:** crie um registro DNS **tipo A** apontando o nome escolhido
  (ex.: `painel.suaempresa.com.br`) para o IP do servidor. Espere o DNS
  propagar (`ping painel.suaempresa.com.br` responde com o IP certo) antes
  do passo 4: sem isso o certificado HTTPS não é emitido.
- **Firewall:** libere só SSH, HTTP e HTTPS:

  ```bash
  sudo ufw allow OpenSSH && sudo ufw allow 80 && sudo ufw allow 443 && sudo ufw enable
  ```

  Use login SSH por chave e desative o login por senha.

## 2. Docker

```bash
curl -fsSL https://get.docker.com | sudo sh
sudo usermod -aG docker "$USER"    # saia e entre de novo no SSH depois disto
docker compose version             # precisa responder v2 ou mais novo
```

## 3. Código e configuração

Se o repositório for privado, cadastre antes uma *deploy key* só de
leitura (gere a chave no servidor com `ssh-keygen -t ed25519` e cole a
parte `.pub` em Settings → Deploy keys do GitHub) e clone pelo endereço
SSH (`git@github.com:teixasofficial-cmd/VidraTX.git`).

```bash
git clone -b main https://github.com/teixasofficial-cmd/VidraTX.git vidratx
cd vidratx/deploy
./scripts/configurar.sh
```

O `configurar.sh` pergunta o domínio e o e-mail (o Let's Encrypt avisa por
ele se houver problema com o certificado) e cria o `deploy/.env` com todas
as senhas geradas aleatoriamente. **Guarde uma cópia do `.env` num
gerenciador de senhas.** As demais opções estão comentadas no próprio
arquivo (veja também a [tabela de variáveis](#variáveis-do-env)).

## 4. Subir

```bash
docker compose up -d --build
```

A primeira vez leva de 5 a 10 minutos (compila backend, painel e
whatsapp-service). Depois confira:

```bash
docker compose ps                                  # todos "healthy" (o backup não tem health check)
curl https://painel.suaempresa.com.br/actuator/health   # {"status":"UP",...}
```

## 5. Senha do super admin

A conta do super admin é `admin@vidratx.local`. Defina a senha dela:

```bash
./scripts/redefinir-senha-superadmin.sh
```

A senha é digitada sem aparecer na tela, vira hash dentro do próprio
backend e só o hash vai para o banco. O mesmo script cria outra conta de
super admin se você passar outro e-mail: `./scripts/redefinir-senha-superadmin.sh voce@suaempresa.com.br`.
Depois, dá para trocar a senha pelo próprio painel (**Trocar minha
senha**, no topo da tela do super admin).

## 6. Primeira empresa

1. Entre em `https://painel.suaempresa.com.br/superadmin/login`.
2. Clique em **Nova empresa** e preencha nome, CNPJ, número de telefone,
   e-mail, endereço e senha inicial. O e-mail e a senha viram o login do
   administrador da empresa. O identificador da empresa (o "slug") é
   gerado a partir do nome e aparece embaixo dele na lista (ex.:
   `/vidracaria-sol`).
3. Passe para a vidraçaria: o endereço `https://painel.suaempresa.com.br/entrar`,
   o identificador (sem a barra), o e-mail e a senha. Ela cria os usuários
   da equipe dentro do painel dela.

O cadastro público de empresas (`POST /auth/cadastro`) fica fechado em
produção; só o super admin cria empresas.

## 7. WhatsApp de cada empresa

Logado como ADMIN ou GERENTE da empresa: **Configurações → WhatsApp →
Conectar WhatsApp**. O QR code aparece na tela; escaneie com o celular do
número da vidraçaria (WhatsApp → Aparelhos conectados → Conectar um
aparelho). A tela avisa quando conectar.

Antes de liberar para as vidraçarias, faça o [piloto com um aparelho
real](PILOTO_WHATSAPP.md): os testes automáticos usam um WhatsApp simulado.

## Operação do dia a dia

| Para… | Comando (na pasta `deploy/`) |
|---|---|
| Ver o estado dos serviços | `docker compose ps` |
| Acompanhar os logs | `docker compose logs -f backend` (ou `whatsapp`, `web`, `mysql`, `backup`) |
| Reiniciar um serviço | `docker compose restart backend` |
| Atualizar para a versão nova do código | `./scripts/atualizar.sh` (faz backup antes) |
| Fazer um backup agora | `docker compose exec backup bash /scripts/backup.sh agora` |
| Restaurar um backup | `./scripts/restaurar-backup.sh backups/vidratx_….sql.gz [backups/midia_….tar.gz]` |
| Parar tudo (os dados ficam) | `docker compose down` |

Nunca rode `docker compose down -v`: o `-v` apaga os volumes, ou seja, o
banco, as fotos dos clientes, as sessões do WhatsApp e os certificados.

### Backups

Todo dia na `BACKUP_HORA` (padrão 3h) o serviço `backup` grava em
`deploy/backups/`:

- `vidratx_AAAA-MM-DD_HHMM.sql.gz`: o banco inteiro;
- `midia_AAAA-MM-DD_HHMM.tar.gz`: as fotos recebidas pelo WhatsApp;

e apaga os que têm mais de `BACKUP_RETENCAO_DIAS` dias (padrão 14).

**Esses arquivos ficam no mesmo servidor.** Se o servidor for perdido, os
backups vão junto: copie a pasta `deploy/backups/` para fora todos os dias
(por exemplo, com `rclone` para um bucket S3/Backblaze/Google Drive, ou
`rsync` para outra máquina). Os arquivos não são criptografados e têm
dados de clientes: guarde a cópia num lugar com acesso restrito.

Teste a restauração de vez em quando, num servidor de testes:
`./scripts/restaurar-backup.sh` substitui o banco inteiro pelo conteúdo
do backup e sobe o sistema de novo.

As credenciais das sessões do WhatsApp (volume `whatsapp-dados`) não
entram no backup de propósito: quem tem esses arquivos se passa pelo
WhatsApp da vidraçaria. Se forem perdidas, cada empresa escaneia o QR de
novo.

### Monitoramento

- **Sistema fora do ar:** cadastre `https://painel.suaempresa.com.br/actuator/health`
  num monitor de disponibilidade gratuito (UptimeRobot, Better Stack,
  Healthchecks.io) com alerta por e-mail ou celular. A resposta esperada é
  HTTP 200 com `"status":"UP"`.
- **WhatsApp de uma empresa desconectado:** quando o WhatsApp de uma
  empresa ativa fica fora do ar por mais de
  `ALERTA_WHATSAPP_DESCONECTADO_MINUTOS` (padrão 30), o backend avisa uma
  vez e avisa de novo quando ele volta. O aviso sempre vai para o log do
  backend; com `ALERTA_WEBHOOK_URL` ele chega também num canal de chat:
  - Slack: crie um app com *Incoming Webhooks* e use a URL gerada;
  - Discord: Configurações do canal → Integrações → Webhooks → Copiar URL;
  - Google Chat: Apps e integrações do espaço → Webhooks.

  Depois de mudar o `.env`: `docker compose up -d backend`.
- **Painel do super admin:** mostra o status do WhatsApp de cada empresa
  (conectado, desconectado desde quando, nunca conectado) e quantas estão
  desconectadas.

## Segurança

- Só as portas 80 e 443 ficam abertas; MySQL, backend e whatsapp-service
  estão na rede interna do Docker.
- O webhook que o whatsapp-service usa para entregar as mensagens ao
  backend não é acessível pela internet.
- A documentação da API (Swagger) fica desligada e o Actuator só expõe o
  health check, sem detalhes.
- O `.env` é criado com permissão só para o seu usuário. Se ele vazar,
  troque os segredos:
  - `JWT_SECRET`: gere outro e rode `docker compose up -d backend`; todo
    mundo precisa entrar de novo;
  - `WHATSAPP_GATEWAY_TOKEN`: gere outro e rode
    `docker compose up -d backend whatsapp`;
  - senhas do MySQL: trocar exige alterar o usuário no próprio MySQL;
    peça ajuda antes.
- A migration `V24` cria o super admin com o hash de uma senha definida
  fora do repositório, e esse hash fica no histórico do git para sempre.
  Defina uma senha nova com o script do passo 5 antes de abrir o sistema.

## Limitações conhecidas

- **Uma instância de cada serviço.** O bloqueio de tentativas de login
  fica na memória do backend, e as sessões do WhatsApp ficam num único
  whatsapp-service. Para mais de um servidor, esses dois pontos precisam
  mudar antes.
- **WhatsApp não oficial.** O Baileys automatiza o WhatsApp Web; não é uma
  integração oficial da Meta e existe risco de bloqueio do número em uso
  intenso. Use um número dedicado a cada vidraçaria e veja as
  recomendações no [piloto](PILOTO_WHATSAPP.md). Para volume alto, o
  caminho é a API oficial do WhatsApp Business (só o whatsapp-service
  mudaria).

## Variáveis do `.env`

| Variável | Obrigatória | Padrão | Uso |
|---|---|---|---|
| `DOMINIO` | sim | — | Endereço do painel, sem `https://` |
| `EMAIL_ACME` | sim | — | E-mail para os avisos do Let's Encrypt |
| `MYSQL_ROOT_PASSWORD` | sim | — | Senha do root do MySQL (backups e scripts) |
| `DB_PASSWORD` | sim | — | Senha do usuário do banco usado pelo backend |
| `JWT_SECRET` | sim | — | Assinatura dos logins (mínimo 32 caracteres) |
| `WHATSAPP_GATEWAY_TOKEN` | sim | — | Segredo entre backend e whatsapp-service |
| `VIDRATX_FUSO_HORARIO` | não | `America/Sao_Paulo` | Fuso das datas quando a empresa não configurou o dela |
| `ALERTA_WEBHOOK_URL` | não | vazio | Webhook de chat para os alertas de WhatsApp |
| `ALERTA_WHATSAPP_DESCONECTADO_MINUTOS` | não | `30` | Minutos fora do ar antes do alerta |
| `BACKUP_HORA` | não | `3` | Hora do backup diário (0 a 23) |
| `BACKUP_RETENCAO_DIAS` | não | `14` | Por quantos dias os backups ficam guardados |

## Solução de problemas

- **O site não abre com HTTPS / erro de certificado:** o DNS ainda não
  aponta para o servidor, ou as portas 80/443 estão fechadas. Veja
  `docker compose logs web`; o Caddy tenta de novo sozinho.
- **O backend não fica "healthy":** `docker compose logs backend`. Erro de
  Flyway ou de conexão com o banco costuma ser `.env` incompleto.
- **O QR do WhatsApp não aparece:** `docker compose logs whatsapp`. O
  servidor precisa conseguir sair para a internet (os servidores do
  WhatsApp).
- **Disco cheio:** `docker system df`. Os logs já têm limite; confira a
  pasta `backups/` e rode `docker image prune -f` para apagar imagens
  antigas.
