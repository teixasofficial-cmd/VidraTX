# whatsapp-service

Adaptador de WhatsApp do VidraTX, construído sobre [Baileys](https://github.com/WhiskeySockets/Baileys).
Este serviço **não tem lógica de negócio** — ele só mantém a conexão real
com o WhatsApp (uma sessão por empresa) e transporta mensagens entre o
WhatsApp e o backend Spring. Todo o fluxo de conversa, validação de
respostas e regras de atendimento vivem no backend (`FluxoAtendimentoService`
e `WhatsappConversaService`), não aqui — de propósito, para que trocar de
provedor de WhatsApp no futuro (ex.: API oficial da Meta) não exija tocar
em nenhuma regra de negócio.

## Por que um serviço separado?

Baileys é uma biblioteca Node/TypeScript; o backend do VidraTX é Java/Spring.
Em vez de tentar rodar as duas coisas no mesmo processo, este serviço roda
sozinho e conversa com o Spring só por HTTP, em dois sentidos:

- **Node → Spring**: quando chega uma mensagem, ou quando uma conexão abre
  ou cai, este serviço chama `POST /api/whatsapp/webhook/mensagens` ou
  `POST /api/whatsapp/webhook/conexao` no Spring, autenticado com o
  `webhookToken` daquela empresa (o mesmo token usado para escolher qual
  sessão do Baileys usar).
- **Spring → Node**: quando um atendente responde manualmente, ou quando o
  ADMIN clica em "Conectar WhatsApp" no painel, o Spring chama
  `POST /mensagens/enviar` ou `POST /sessoes/:instanciaToken/iniciar` aqui,
  autenticado com um segredo único compartilhado (`GATEWAY_TOKEN`).

## Rodando localmente

```bash
cp .env.example .env
# edite .env: GATEWAY_TOKEN precisa ser IGUAL ao WHATSAPP_GATEWAY_TOKEN
# configurado no backend Spring (variável de ambiente lá).

npm install
npm run dev
```

Em produção o serviço roda em Docker pelo `deploy/docker-compose.yml`
(imagem deste `Dockerfile`), com credenciais, fila e registro de envios no
volume `/data`. Veja [deploy/README.md](../deploy/README.md).

## Conectando o WhatsApp de uma empresa

1. No painel (ADMIN ou GERENTE), em Configurações › WhatsApp, clique em
   "Conectar". O backend chama `POST /sessoes/:instanciaToken/iniciar`.
2. O QR code aparece **no próprio painel** (o backend busca em
   `GET /sessoes/:instanciaToken/qr`, que devolve a imagem como data URL);
   ele também continua sendo impresso no terminal deste serviço.
3. Ao conectar, este serviço avisa o Spring (`/api/whatsapp/webhook/conexao`),
   que marca a instância como `CONECTADO` e envia na hora as mensagens que
   estavam na fila esperando a conexão.

## Contrato com o backend

Spring → este serviço (autenticado por `GATEWAY_TOKEN`):

| Rota | Resposta |
| --- | --- |
| `GET /health` (sem token) | `{ status: "UP", sessoes: { total, conectadas } }`, para o health check do Docker |
| `POST /sessoes/:token/iniciar` | 202 |
| `GET /sessoes/:token/qr` | `{ status, qr }` — `qr` é um data URL PNG ou `null` |
| `GET /sessoes/:token/status` | `{ status }` — `CONECTADO`, `CONECTANDO` ou `DESCONECTADO` |
| `POST /mensagens/enviar` `{ instanciaToken, telefone, mensagem, idempotencyKey }` | 200 `{ mensagemId }`; 409 sem sessão conectada; 422 número sem WhatsApp |

O envio é idempotente pela `idempotencyKey`: o backend reenvia com a mesma
chave quando não recebe a resposta, e a repetição devolve o mesmo
`mensagemId` sem mandar a mensagem de novo ao cliente. O registro das
chaves fica em disco (`ARQUIVO_IDEMPOTENCIA`, validade de 24 h) e vale
também depois de um reinício do serviço. O número é
conferido com `onWhatsApp` antes do envio, o que também resolve o endereço
real de celulares antigos sem o nono dígito.

Este serviço → Spring (autenticado pelo `webhookToken` da empresa):

| Rota | Quando |
| --- | --- |
| `POST /api/whatsapp/webhook/mensagens` `{ telefone, mensagem, mensagemId, timestamp, midiaNaoSuportada?, reacaoA?, editaMensagemId?, apagaMensagemId? }` | texto, resposta de botão/lista; áudio, vídeo, figurinha, documento, localização e contato chegam com `midiaNaoSuportada`; reação com `reacaoA` (id da mensagem reagida); edição com `editaMensagemId` e o texto novo; "apagar para todos" com `apagaMensagemId` |
| `POST /api/whatsapp/webhook/midia` (multipart: `telefone`, `arquivo`, `legenda`, `mensagemId`, `timestamp`) | foto |
| `POST /api/whatsapp/webhook/status` `{ mensagemId, status }` | recibo de entrega (`ENTREGUE`), leitura (`LIDA`) ou erro (`ERRO`) |
| `POST /api/whatsapp/webhook/conexao` `{ status, numero }` | sessão conectou ou caiu |

`mensagemId` (o `key.id` do WhatsApp) é o que permite ao backend descartar
reentregas; `timestamp` (`messageTimestamp`) é o horário em que o cliente
escreveu — o backend usa para saber a qual pergunta a resposta se refere e
para ignorar mensagens que chegaram fora de ordem.

Mensagens temporárias e de visualização única são desembrulhadas;
contatos com endereço `@lid` são resolvidos pelo `senderPn` quando o
WhatsApp informa o número (sem ele não há como identificar o cliente — fica
no log). As mensagens de um mesmo número são repassadas em série, na ordem.

## Nada se perde se o backend estiver fora

Uma mensagem de cliente que o backend não aceita depois de 3 tentativas vai
para uma fila em disco (`FILA_DIR`, padrão `fila_webhook/`) e é reenviada a
cada 15 segundos, em ordem, até entrar. Como o backend descarta reentregas
pelo `mensagemId`, reenviar nunca duplica nada. Uma recusa definitiva do
backend (4xx que não seja 408/429) é descartada com log.

O aviso de conexão (`/api/whatsapp/webhook/conexao`) que não entra também
é reenviado a cada 15 segundos, só o mais recente de cada instância, até o
backend aceitar. Isso importa depois de um reboot do servidor: a sessão
reconecta antes de o backend terminar de subir, e sem o aviso o backend
seguraria as mensagens de saída achando que o WhatsApp está desconectado.

## Variáveis de ambiente

| Variável | Padrão | Uso |
| --- | --- | --- |
| `PORT` | 3333 | porta HTTP |
| `GATEWAY_TOKEN` | — | igual ao `WHATSAPP_GATEWAY_TOKEN` do backend |
| `SPRING_BASE_URL` | http://localhost:8080 | endereço do backend |
| `AUTH_STATE_DIR` | auth_sessions | credenciais das sessões (segredo) |
| `FILA_DIR` | fila_webhook | fila em disco das mensagens a entregar ao backend |
| `TIMEOUT_VERSAO_MS` | 5000 | tempo máximo para consultar a versão do WhatsApp Web; sem resposta, usa a embutida no Baileys |
| `JANELA_MENSAGENS_OFFLINE_HORAS` | 72 | mensagens recebidas com o serviço fora do ar só são repassadas se forem dessas últimas horas |
| `ARQUIVO_IDEMPOTENCIA` | envios-idempotencia.json | registro das chaves de idempotência dos envios |

## Persistência da sessão

As credenciais de cada sessão ficam em `auth_sessions/<instanciaToken>/`
(uma pasta por empresa, via `useMultiFileAuthState` do Baileys). Esse
diretório nunca deve ir para o git nem ser compartilhado — quem tiver
acesso a ele consegue se passar pelo WhatsApp conectado. Em produção,
trate-o como um segredo (volume persistente, backup controlado).

## Reconexão e reinício

Na subida, o serviço retoma sozinho todas as sessões já pareadas (as
pastas em `AUTH_STATE_DIR` com credenciais salvas). Uma queda de conexão
que não seja logout é reconectada com espera crescente (2 s, 5 s, 15 s,
30 s, 60 s). Um logout de verdade (desconectado pelo celular) apaga as
credenciais daquela instância e exige escanear um QR novo pelo painel.

## Limitações conhecidas

- Fotos são repassadas e ficam na conversa (e no orçamento criado da
  solicitação); áudio, vídeo, figurinha e documento não são baixados — o
  bot pede ao cliente para escrever, e um atendente vê na conversa que o
  cliente mandou esse tipo de mídia.
- O comportamento com contatos `@lid` e números antigos sem o nono dígito
  depende do que o WhatsApp informa em cada caso e precisa ser conferido em
  aparelho real.
- Baileys automatiza o WhatsApp Web e não é uma integração oficial da
  Meta: existe risco de bloqueio do número em uso comercial intenso. Para
  um volume alto de mensagens, considere migrar para a API oficial do
  WhatsApp Business — a arquitetura foi pensada para isso: só este
  serviço mudaria, o resto do sistema não.

Mensagens que o cliente mandou enquanto este serviço estava desconectado
chegam na reconexão (o Baileys as entrega em `messages.upsert` do tipo
`append`) e são repassadas normalmente, em ordem de envio, se forem das
últimas `JANELA_MENSAGENS_OFFLINE_HORAS` (padrão 72 h). O histórico
sincronizado do aparelho vem por outro evento e não é repassado.

## Testes

```bash
npm test
```

Cobrem a tradução de cada tipo de mensagem do Baileys para o aviso ao
backend (`src/repasse.ts`: texto, reação, edição, remoção, mídia, janela e
ordem das mensagens offline), a idempotência de envio entre reinícios
(`src/idempotencia.ts`) e o reenvio do aviso de conexão
(`src/springClient.ts`).
