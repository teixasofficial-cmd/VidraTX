# Piloto com WhatsApp real

Os testes automáticos (`e2e/`) conversam com um WhatsApp simulado
(`e2e/gateway_falso.py`). Eles provam as regras do atendimento, mas não o
que depende do WhatsApp de verdade: o pareamento, os recibos de entrega e
leitura, como chegam reações, edições, fotos e áudios, os contatos `@lid`,
os números antigos sem o nono dígito, as mensagens que chegam com o
serviço parado. Este roteiro confere tudo isso com dois celulares, antes de
liberar o sistema para as vidraçarias.

Leva de uma a duas horas. Anote o resultado de cada passo na
[tabela do final](#resultado).

## Antes de começar

- **Sistema no ar** ([deploy/README.md](README.md)), com uma empresa de
  piloto criada pelo super admin e você logado nela como ADMIN.
- **Celular A**, o da vidraçaria: um número dedicado, com WhatsApp (de
  preferência o WhatsApp Business) ativo há algum tempo. Evite um chip
  recém-ativado (veja [cuidados](#cuidados-para-não-bloquear-o-número)).
- **Celular B**, o do "cliente": qualquer outro número com WhatsApp, que
  não esteja cadastrado como cliente da empresa de piloto.
- **Se tiver:** um terceiro número com conta antiga de WhatsApp, criada
  antes do nono dígito chegar ao DDD dele (passo 17).
- **Alertas mais rápidos durante o piloto:** no `deploy/.env`, ponha
  `ALERTA_WHATSAPP_DESCONECTADO_MINUTOS=5` e configure o
  `ALERTA_WEBHOOK_URL`; depois `docker compose up -d backend`. Volte para
  30 no fim.
- **Logs abertos** num terminal do servidor, na pasta `deploy/`:

  ```bash
  docker compose logs -f --since 1m whatsapp backend
  ```

## Roteiro

### Conexão

1. **Parear.** No painel: Configurações → WhatsApp → Conectar WhatsApp.
   No celular A: WhatsApp → Aparelhos conectados → Conectar um aparelho,
   e escaneie o QR da tela.
   *Esperado:* em poucos segundos a tela mostra "Conectado" com o número
   do celular A. No painel do super admin, a empresa aparece com o
   WhatsApp conectado.

### Conversa com o bot

2. **Primeira mensagem.** Do celular B, mande `oi` para o número A.
   *Esperado:* o bot responde pedindo o nome em até uns 5 segundos, e a
   conversa aparece em Atendimentos.
3. **Pedido de orçamento completo.** Responda o nome, depois `1`
   (Solicitar orçamento), o serviço (`box de banheiro`), a descrição com
   medidas e endereço, e `1` para confirmar.
   *Esperado:* "Perfeito! Recebemos sua solicitação…" e um orçamento novo
   na tela de Orçamentos, no nome do cliente, com o telefone do celular B.
4. **Foto.** Numa conversa nova (ou antes de confirmar), mande uma foto
   com legenda.
   *Esperado:* o bot responde "Foto recebida!…", e a foto aparece na
   conversa do painel e no orçamento criado.
5. **Áudio e outros tipos.** Mande um áudio, depois uma figurinha e um
   documento PDF.
   *Esperado:* o bot avisa que ainda não consegue ouvir áudios nem abrir
   vídeos, figurinhas ou documentos e pede para escrever; a conversa no
   painel mostra que o cliente mandou esse tipo de mídia.
6. **Mensagem temporária e visualização única.** No celular B, ative as
   mensagens temporárias na conversa e mande um texto; depois mande uma
   foto de visualização única.
   *Esperado:* os dois chegam ao painel como uma mensagem e uma foto
   normais.
7. **Pedir atendente.** Mande `3` no menu (ou escreva "quero falar com um
   atendente").
   *Esperado:* o bot avisa que vai transferir e a conversa fica marcada
   para atendimento humano no painel.

### Mensagens da empresa

8. **Resposta do atendente e recibos.** Responda o cliente pelo painel,
   na conversa.
   *Esperado:* a mensagem chega no celular B; no painel, o status da
   mensagem passa de "Enviada" para "Entregue" e, quando o celular B abre
   a conversa, para "Lida" (se o celular B estiver com a confirmação de
   leitura ligada; sem ela, fica em "Entregue").
9. **Orçamento pelo WhatsApp.** Preencha o orçamento do passo 3 e clique
   em **Enviar orçamento pelo WhatsApp**.
   *Esperado:* o celular B recebe o orçamento; o status do envio aparece
   no orçamento.
10. **Proposta de medição.** No orçamento, painel de medição → **Propor
    data**. No celular B, responda `sim`.
    *Esperado:* a medição fica agendada no painel. Repita com `não` (a
    proposta é recusada) e com uma contraproposta, por exemplo `dia 12 às
    10h fica melhor` (aparece para a empresa aceitar ou recusar).

### Reação, edição e remoção

11. **Reação que responde.** Proponha uma data de novo e, no celular B,
    reaja à mensagem da proposta com 👍.
    *Esperado:* vale como `sim`: a medição fica agendada. Uma reação ❤️
    ou 😂 só é registrada na conversa e não muda nada.
12. **Edição.** Numa pergunta do bot, responda `1` e depois edite a
    mensagem para `2` (segure a mensagem → Editar).
    *Esperado:* a conversa mostra "Editou a mensagem "1" para: "2"", o
    bot diz que um atendente vai conferir e a conversa é marcada para
    atendimento humano. Corrigir só a escrita (`sim` para `Sim!`) não
    gera alerta.
13. **Apagar para todos.** Responda uma pergunta e apague a resposta com
    "Apagar para todos".
    *Esperado:* a conversa mostra "Apagou a mensagem…" e é marcada para
    atendimento humano.

### Quedas e reinícios

14. **Mensagens com o serviço do WhatsApp parado.** No servidor:
    `docker compose stop whatsapp`. Do celular B, mande três mensagens
    seguidas (`oi`, um nome, `1`). Espere 2 minutos e rode
    `docker compose start whatsapp`.
    *Esperado:* o serviço reconecta sozinho, sem QR novo; as três
    mensagens entram no painel na ordem em que foram escritas e o bot
    responde a elas. Mensagens de mais de 72 horas
    (`JANELA_MENSAGENS_OFFLINE_HORAS`) não são respondidas.
15. **Backend fora do ar.** `docker compose stop backend`, mande uma
    mensagem do celular B, espere 1 minuto e rode
    `docker compose start backend`.
    *Esperado:* quando o backend volta (1 a 2 minutos), a mensagem é
    entregue e respondida uma vez só, sem duplicar.
16. **Reinício do servidor.** `sudo reboot`. Quando o servidor voltar:
    *Esperado:* todos os serviços sobem sozinhos (`docker compose ps`), o
    WhatsApp volta a "Conectado" sem QR, e uma mensagem do celular B é
    respondida.
17. **Desconectado pelo celular e alerta.** No celular A: Aparelhos
    conectados → toque na sessão do VidraTX → Desconectar. Enquanto está
    desconectado, responda o cliente pelo painel.
    *Esperado:*
    - o painel da empresa e o do super admin mostram "Desconectado";
    - a resposta fica "Na fila de envio";
    - entre 5 e 10 minutos depois chega o alerta de WhatsApp desconectado
      no canal do webhook (e no log do backend).

    Conecte de novo pelo QR (passo 1).
    *Esperado:* chega o alerta "conectado de novo" e a resposta que estava
    na fila é entregue ao celular B.

### Números

18. **Número antigo sem o nono dígito** (se tiver esse número). Cadastre
    o cliente no painel com o número completo, com o 9, e envie um
    orçamento para ele; depois peça que ele mande uma mensagem.
    *Esperado:* o orçamento chega, e a mensagem dele entra na conversa do
    mesmo cliente, sem criar um cliente duplicado.
19. **Número sem WhatsApp.** Cadastre um cliente com um telefone fixo e
    tente enviar um orçamento para ele.
    *Esperado:* o envio fica como "Não entregue", com o erro "Este número
    não tem WhatsApp".
20. **Contato `@lid`.** Não dá para provocar de propósito: durante todo o
    piloto, procure nos logs do whatsapp-service a frase
    `contato @lid sem número informado`.
    *Esperado:* não aparecer. Se aparecer, anote o horário: é uma
    mensagem que não chegou ao painel.

## Cuidados para não bloquear o número

O sistema usa o WhatsApp Web por automação (Baileys), não a API oficial
da Meta. O WhatsApp bloqueia números que parecem robôs de propaganda. Para
reduzir o risco:

- Use um **número dedicado** a cada vidraçaria, com histórico de uso
  normal. Um chip novo que já começa conversando com muita gente é o
  perfil mais bloqueado.
- **Comece pequeno:** poucos clientes reais na primeira semana, depois
  aumente.
- O sistema só responde quem escreveu e manda mensagens para os próprios
  clientes da vidraçaria. Não use o número para disparo em massa.
- Peça aos clientes que salvem o número da vidraçaria nos contatos.
- Mantenha o **celular A ligado e com internet**. Se ele ficar 14 dias
  sem uso, o WhatsApp desconecta os aparelhos conectados, inclusive o
  VidraTX.
- Se o número for bloqueado, o recurso é pelo próprio aplicativo do
  WhatsApp (o sistema não tem como desbloquear). Para volume alto, a
  saída definitiva é a API oficial do WhatsApp Business.

## Resultado

| # | Passo | OK / Falhou | Observação |
|---|---|---|---|
| 1 | Parear | | |
| 2 | Primeira mensagem | | |
| 3 | Pedido de orçamento completo | | |
| 4 | Foto | | |
| 5 | Áudio e outros tipos | | |
| 6 | Temporária e visualização única | | |
| 7 | Pedir atendente | | |
| 8 | Resposta do atendente e recibos | | |
| 9 | Orçamento pelo WhatsApp | | |
| 10 | Proposta de medição | | |
| 11 | Reação | | |
| 12 | Edição | | |
| 13 | Apagar para todos | | |
| 14 | Serviço do WhatsApp parado | | |
| 15 | Backend fora do ar | | |
| 16 | Reinício do servidor | | |
| 17 | Desconectado pelo celular e alerta | | |
| 18 | Número sem o nono dígito | | |
| 19 | Número sem WhatsApp | | |
| 20 | Contato `@lid` | | |

Quando um passo falhar, anote o horário e salve os logs antes de tentar
de novo:

```bash
docker compose logs --since 30m whatsapp backend > piloto-$(date +%F-%H%M).log
```

O arquivo tem números de telefone e mensagens de clientes: não publique,
mande só para quem vai investigar.
