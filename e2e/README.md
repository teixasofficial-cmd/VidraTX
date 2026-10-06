# Testes de ponta a ponta (WhatsApp, agenda, preço e permissões)

Roteiros que exercitam o **backend real** (Spring + MySQL) do jeito que um
cliente e uma vidraçaria usariam: mensagens chegando pelo webhook do
WhatsApp, usuários agindo pelo painel ao mesmo tempo, WhatsApp caindo no
meio de uma etapa. Nada é simulado dentro do backend: só o
`whatsapp-service` é trocado por um gateway falso que registra cada
mensagem que seria enviada ao cliente.

Cada verificação descreve o comportamento **esperado** e imprime
`[PASSA]` ou `[FALHA]` com o que o sistema de fato fez. Uma `FALHA` aqui
é um defeito do produto, não do roteiro: as verificações são o critério
de aceite.

Cada roteiro nasceu de uma rodada de revisão do produto: os defeitos
encontrados viraram verificações e foram corrigidos até todas passarem
(backend com `TZ=UTC`, empresa em America/Sao_Paulo):

| Roteiro | Quando foi escrito | Hoje |
|---|---|---|
| `testes_whatsapp.py` | 22 de 75 | 82 de 82 |
| `testes_preco_permissao.py` | 11 de 28 | 28 de 28 |
| `testes_e2e.py` | 21 de 42 | 43 de 43 |
| `dia_inteiro.py` | vários problemas | 0 em todos os indicadores |
| `testes_quebra.py` | 3 de 15 | 35 de 35 |
| `testes_regras.py` (regras de negócio decididas) | — | 42 de 42 |
| `testes_cenarios.py` | 17 de 36 | 36 de 36 |

As verificações acrescentadas cobrem o que as correções introduziram:
estimativa antes da medição, recusar a sugestão do cliente com histórico
de propostas, data entendida do texto da sugestão, "sim" com duas
perguntas abertas (lista numerada) e "sim" repetido, fila de
envio entregue ao reconectar com o status de entrega atualizado, a
última mensagem do cliente valendo mesmo fora de ordem, instalação na
agenda, produção dispensável na OS e encerramento com confirmação.

| Arquivo | O que cobre |
|---|---|
| `testes_whatsapp.py` | 20 cenários de WhatsApp (aceite, recusa, contraproposta, duplicidade, webhook repetido, fora de contexto, atendente humano, conflito de agenda, falha de entrega, WhatsApp desconectado, mensagens fora de ordem, dois orçamentos, conversa encerrada) e 3 extras |
| `testes_preco_permissao.py` | Preço mostrado × preço enviado, edição de linha, vidro sem preço, quantidade, congelamento de orçamento enviado, preço final manual, permissões por perfil, integridade orçamento × medição |
| `testes_e2e.py` | Cenários A–E (fluxo perfeito, recusa de data, negociação múltipla, instalação, falha do WhatsApp), fluxo do bot, concorrência na criação de conversa, dashboard, fuso horário |
| `dia_inteiro.py` | Um dia de operação com 14 clientes e 3 usuários agindo em paralelo; no fim compara o banco com o que a dashboard mostra |
| `testes_quebra.py` | Casos de quebra: "sem dúvida não vou fechar" aprovando orçamento, data sugerida com mais de uma data, confirmação entregue fora de ordem, conversa encerrada com cliente na fila, estimativa com item sem preço, "agora não" perdendo o orçamento, fila de saída com mensagem de dias atrás; reação, edição e "apagar para todos", ordem de envio por contato, lista "você tem N assuntos" e consulta com vários orçamentos |
| `testes_regras.py` | As 11 regras de negócio decididas: fuso da empresa, antecedência mínima, equipe na instalação, orçamento com medição em negociação, reabrir orçamento perdido, pedido de cancelamento e "manter a data", prazo de resposta do atendente com aviso ao cliente, saída "0" da fila e encerramento de conversa humana esquecida |
| `testes_cenarios.py` | 15 cenários de uso real e extras: primeira mensagem aproveitada, medida confirmada, pedido virando rascunho com valor sugerido, dúvidas frequentes, fora do horário, "e aquele orçamento?", leitor de medidas, quantidade, deslocamento, renovação de sessão, permissão de preço e LGPD |
| `vt.py` | Funções compartilhadas (empresa de teste, clientes, orçamentos, webhook) |
| `gateway_falso.py` | Substituto do whatsapp-service (porta 3333); `{"atrasarProximaSegundos": N}` em `/modo` deixa o próximo envio lento |

## Como rodar

Pré-requisitos: MySQL com o banco `vidratx`, JDK 25, Python 3.11+ com
`requests` e o cliente de linha de comando `mysql` (alguns testes leem o
histórico direto do banco e o token do webhook, que não sai mais na API).

```bash
# 1. gateway falso no lugar do whatsapp-service
python3 e2e/gateway_falso.py &

# 2. backend apontando para ele (TZ=UTC reproduz um servidor em nuvem)
mvn -q -DskipTests package
#    WHATSAPP_AVISO_ESPERA_INTERVALO=PT10S: o aviso de demora ao cliente
#    na fila sai em segundos, não em até 5 minutos
#    CADASTRO_PUBLICO_HABILITADO=true: cada roteiro cria a própria empresa
#    pela rota pública de cadastro, fechada por padrão
TZ=UTC DB_PASSWORD=root JWT_SECRET=$(openssl rand -hex 32) \
  WHATSAPP_GATEWAY_URL=http://localhost:3333 WHATSAPP_GATEWAY_TOKEN=qualquer \
  WHATSAPP_AVISO_ESPERA_INTERVALO=PT10S CADASTRO_PUBLICO_HABILITADO=true \
  java -jar target/vidratx-0.0.1-SNAPSHOT.jar &

# 3. roteiros (cada um cria a própria empresa de teste)
cd e2e
python3 testes_whatsapp.py
python3 testes_preco_permissao.py
python3 testes_e2e.py
python3 dia_inteiro.py
python3 testes_quebra.py
python3 testes_regras.py
python3 testes_cenarios.py
```

Variáveis opcionais: `VIDRATX_API` (padrão `http://localhost:8080`),
`VIDRATX_GATEWAY_FALSO` (padrão `http://localhost:3333`), `DB_HOST`,
`DB_USERNAME`, `DB_PASSWORD`, `DB_NAME`.

Cada roteiro grava `resultado_*.json` nesta pasta com todas as
verificações.

## O que estes roteiros não cobrem

- O `whatsapp-service` de verdade (Baileys): formato de número/JID,
  mensagens de áudio, mensagens temporárias, reconexão. Esses pontos
  precisam de um aparelho real.
- As rotinas agendadas rodando no horário real (`OrcamentoFollowUpService`,
  `OrcamentoExpiracaoService`, vencimento de propostas de data, encerramento
  de conversas inativas). A regra de cada uma é coberta por testes de
  unidade (`mvn test`) com relógio fixo; aqui só o efeito aparece quando o
  roteiro força a data no banco. A exceção é o aviso de demora ao cliente
  na fila (`testes_regras.py`, I3/I4), que roda de verdade com o intervalo
  curto acima. O aviso respeita a janela de mensagens automáticas, que não
  inclui domingo: nesse dia I3 e I4 são pulados com um aviso.
- As telas do painel. Elas foram conferidas num navegador (Playwright)
  contra este mesmo backend, subindo-o com
  `CORS_ALLOWED_ORIGINS=http://localhost:4173` e servindo o build com
  `npx vite preview --port 4173`.
