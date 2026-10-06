# Inventário de funcionalidades do VidraTX

Base do manual do usuário (`manual-vidratx.html`).
Levantado no código do `main`, a partir de:

- rotas e menu do painel (`dashboard/src/App.tsx`, `components/Sidebar.tsx`);
- páginas e componentes;
- chamadas que o painel faz (`dashboard/src/lib/api.ts`);
- endpoints (`src/main/java/.../controller`) e permissões (`@PreAuthorize`);
- entidades e enums;
- rotinas automáticas (`@Scheduled`);
- `application.properties`.

**Imagens:** capturadas do próprio sistema rodando este código, com uma
vidraçaria fictícia, e guardadas em `docs/manual/imagens/`. O WhatsApp das
capturas é simulado: o QR code de `26-whatsapp-qr.jpg` é um exemplo.

**Perfis:** o sistema tem três, ADMIN (administrador), GERENTE e
FUNCIONARIO. Os envios ao cliente vão pelo WhatsApp da vidraçaria. O
backend também avisa em caso de WhatsApp desconectado, mas esse alerta vai
para o operador do sistema, não para a vidraçaria.

## Prontas: entram no manual

| # | Funcionalidade | Onde fica no menu | O que faz | Quem usa | Imagem correspondente | Prioridade |
|---|---|---|---|---|---|---|
| 1 | Login por empresa | Tela `/entrar` | Entra com identificador da empresa + e-mail + senha. Bloqueia novas tentativas depois de várias senhas erradas seguidas. | Todos | `01-login.jpg` | Essencial |
| 2 | Atendimento automático no WhatsApp (bot) | Não tem tela: roda no WhatsApp da empresa | Responde na hora; pede o nome e cadastra o cliente sozinho. Menu: 1 orçamento, 2 acompanhar, 3 atendente. Coleta tipo de serviço, detalhes e fotos, confirma e gera a solicitação. Transfere para uma pessoa quando não entende ou quando pedem. Entende reação 👍, mensagem editada ou apagada e datas escritas ("amanhã às 9"). Responde "Ainda não consigo ouvir áudios…" | Automático; cliente final | `11-atendimento-solicitacao.jpg` | Essencial |
| 3 | Conexão do WhatsApp | Configurações › WhatsApp | Conecta o número por QR code e mostra o status. Mostra mensagens na fila e mensagens não entregues nos últimos 7 dias. | ADMIN, GERENTE | `26-whatsapp-qr.jpg`, `21-whatsapp.jpg` | Essencial |
| 4 | Caixa de atendimentos | Atendimentos (contador verde no menu) | Fila de quem espera uma pessoa. Permite: assumir, responder, transferir para um colega, devolver ao bot, vincular a um cliente, encerrar (avisa pendências), reabrir e criar orçamento da solicitação. A conversa fica "atrasada" depois do prazo configurado. | Todos | `09-atendimentos.jpg`, `10-atendimento-conversa.jpg` | Essencial |
| 5 | Visão geral | Dashboard | Faixa de "clientes esperando resposta" e de mensagens não entregues. Cartões Sua vez, Aguardando cliente, Medições hoje e Instalações hoje. Próximas ações com "Criar orçamento" para solicitações do WhatsApp. Agenda de 7 dias. Indicadores: em aberto, aprovados, perdidos, taxa de conversão e valor aprovado. | Todos | `02-visao-geral.jpg` | Essencial |
| 6 | Funil de orçamentos | Orçamentos | Kanban de arrastar ou lista com filtros. Colunas: Novo contato, Pré-orçamento, Visita agendada, Medido, Orçamento final, Enviado, Aprovado e Perdido. Pede o motivo ao marcar como perdido. Indicadores do funil. O card anda sozinho com estimativa enviada, medição confirmada e medição realizada. Novo orçamento: cliente, validade e observações. | Todos | `03-funil-kanban.jpg`, `36-orcamento-novo.jpg` | Essencial |
| 7 | Orçamento com cálculo automático | Orçamentos › abrir orçamento › Itens e cálculo | Item por tipologia: 13 prontas (box frontal, box de canto, box de abrir, espelho, porta pivotante/de correr, janela 2 e 4 folhas, vidro avulso, guarda-corpo, sacada, tampo/prateleira, item livre). Informa ambiente, vão (cm), tipo de vidro (comum, temperado, laminado, espelho), espessura, cor (incolor, fumê, verde, bronze), acabamento (jateado, acidato, serigrafado, canelado) e quantidade. Componentes escolhidos da tabela, item a item (não entram sozinhos): ferragem, kit, mão de obra, deslocamento etc. Calcula peças de corte com folga e arredondamento, área mínima e perdas. Alertas: vidro de segurança, laminado em guarda-corpo, peça maior que a chapa e margem abaixo da mínima. Mostra a margem real colorida nos dois modos; no modo preço de venda ela é calculada sobre o "Seu custo" da tabela (vidro e perdas usam o custo do vidro do item) e não aparece enquanto houver componente com valor digitado à mão ou item sem custo. Valores sugeridos passam pela revisão do gerente ("Aceitar todas"). Preço final fixado com desconto/acréscimo, com opção de voltar ao calculado. | Todos montam; só ADMIN/GERENTE mexem em preço | `05-orcamento-item-modal.jpg`, `04-orcamento-calculo.jpg` | Essencial |
| 8 | Envio e aprovação pelo WhatsApp | Orçamento › "Enviar orçamento pelo WhatsApp" | Mensagem com itens, total, validade e observações. O cliente responde 1 Aprovar / 2 Pedir alteração / 3 Atendente / 4 Não tenho interesse. Na aprovação, a OS é aberta sozinha. Status de entrega: enviada, entregue, lida. Lembrete automático depois de 24 h sem resposta. Expira sozinho na validade. "Revisar orçamento" gera uma nova versão. Aprovar ou perder à mão também é possível. O envio é travado enquanto houver pendências. | Todos | `27-orcamento-final-envio.jpg`, `06-orcamento-enviado.jpg` | Essencial |
| 9 | Medição agendada pelo WhatsApp | Orçamento › painel Medição | Propõe data e hora, com endereço e observações; a mensagem leva o valor da visita, se houver. O cliente confirma, sugere outra ou recusa, e a empresa aceita a sugestão ou propõe outra. Também: "Cliente confirmou por telefone", reagendar, manter data, cancelar e marcar como realizada. Proposta vencida vira "Precisa de nova data". Evita conflito de horário. | Todos | `30-medicao-proposta.jpg`, `29-medicao-confirmada.jpg` | Essencial |
| 10 | Configuração Expressa | Tabela de Preços › Configuração Expressa | 8 passos que geram a tabela: temperado 8 e 10 mm incolor e nas cores (% a mais), espelho 4 mm e "kit + mão de obra" calibrado pelo preço de um box instalado. Também preenche os parâmetros: margem, impostos pelo regime, visita e arredondamento. | ADMIN, GERENTE | `15-configuracao-expressa.jpg` | Essencial |
| 11 | Tabela de preços | Tabela de Preços | Itens por categoria (vidro, ferragem, kit, mão de obra, deslocamento, outro) e unidade (m², ml, un, kit, hora, km), com custo, preço de venda e fornecedor. Alerta de venda abaixo do custo. | Todos veem; ADMIN/GERENTE editam | `14-tabela-precos.jpg`, `37-tabela-novo-item.jpg` | Essencial |
| 12 | Estimativa antes de medir | Orçamento › "Enviar estimativa" | Manda ao cliente a faixa "entre R$ X e R$ Y" (−10% a +15% do calculado, por padrão), antes da medição. | Todos | `28-estimativa-enviada.jpg` | Importante |
| 13 | Ordens de serviço e produção | Ordens de serviço | A OS nasce na aprovação. Etapas: Aguardando produção → Em produção → Pronto → Conferido, com a opção "Não passa por produção". Tem observações. | Todos veem; ADMIN/GERENTE movem a produção | `17-ordens-servico.jpg`, `18-os-producao.jpg` | Importante |
| 14 | Instalação agendada pelo WhatsApp | OS › painel Instalação | Igual à medição, mais equipe/responsável e checklist ao concluir. Só libera o agendamento com a produção conferida (ou OS sem produção). A mesma equipe não pega dois horários iguais. O cliente recebe "Instalação concluída!" | Todos | `19-os-instalacao-agendada.jpg` | Importante |
| 15 | Pós-venda | OS › painel Pós-venda | Abrir chamado, iniciar atendimento, registrar solução, encerrar. | Todos | `20-os-pos-venda.jpg` | Importante |
| 16 | Clientes e histórico | Clientes | Cadastro (nome, telefone, WhatsApp, e-mail, CPF/CNPJ, endereço, observações), busca por nome, orçamentos do cliente e histórico de eventos. | Todos | `12-clientes.jpg`, `13-cliente-detalhe.jpg`, `35-cliente-novo.jpg` | Importante |
| 17 | Parâmetros de cálculo | Parâmetros de Cálculo | Custo ou preço de venda, regime, impostos, comissão, margem desejada e mínima, arredondamento do preço final. Também: múltiplo de medida, área mínima, perdas, chapa máxima, visita técnica, validade e faixa da estimativa. | Todos veem; ADMIN/GERENTE editam | `16-parametros.jpg` | Importante |
| 18 | Agenda e mensagens automáticas | Configurações › Empresa › Agenda | Duração da medição e da instalação, medições ao mesmo tempo, janela de horário dos lembretes, fuso, antecedência mínima para propor data e prazo para responder quem pediu atendente. | ADMIN, GERENTE | `32-agenda.jpg` | Importante |
| 19 | Usuários e perfis | Usuários | Contas com perfil ADMIN, GERENTE ou FUNCIONARIO, ativo/inativo e senha inicial. Cada um troca a própria senha (ícone de chave). | ADMIN (a troca de senha: todos) | `23-usuarios.jpg`, `38-usuario-novo.jpg`, `39-trocar-senha.jpg` | Importante |
| 20 | Dados da empresa | Configurações › Empresa | Razão social, nome fantasia (vai nas mensagens ao cliente), CNPJ e contatos. | ADMIN | `22-empresa.jpg` | Secundária |

## Incompletas, desativadas ou só no backend: não entram no manual como prontas

| # | Funcionalidade | Onde fica | O que faz (ou faria) | Situação |
|---|---|---|---|---|
| 21 | Financeiro | Menu, seção "Em breve" | — | **Desativado**: item do menu sem tela. Explicado nas perguntas frequentes do manual |
| 22 | Recebimentos / pagamentos do orçamento | — | Registrar parcelas, marcar pago, resumo (`/api/orcamentos/{id}/pagamentos`) | **Só no backend**: o painel não chama |
| 23 | Materiais necessários do orçamento | — | Lista de materiais por orçamento (`/api/orcamentos/{id}/materiais`) | **Só no backend** |
| 24 | Catálogo de Materiais | Materiais | Cadastro de materiais (nome, descrição, ativo) | **Incompleto**: o cadastro funciona, mas nenhuma outra tela usa. No manual como catálogo de consulta (seção 21, `34-materiais.jpg`) |
| 25 | Catálogo de Serviços | Serviços | Cadastro de serviços (nome, descrição, ativo) | **Incompleto**: não é usado ao montar orçamentos nem pelo atendimento automático. No manual como catálogo de consulta (seção 20, `33-servicos.jpg`) |
| 26 | Fotos pelo painel (medição, antes, durante, depois) | — | Upload e exclusão de fotos no orçamento | **Só no backend**: o painel só mostra as fotos que o cliente mandou pelo WhatsApp |
| 27 | Ajuste de folgas por tipologia | — | Mudar as folgas (mm) de cada tipologia | **Só no backend**: a função existe no cliente da API, mas nenhuma tela a usa. Valem as folgas padrão |
| 28 | Parcelamento no cartão | Orçamento › resumo | "em até Nx no cartão" na mensagem | **Incompleto**: as taxas por parcela não são editáveis no painel e a Configuração Expressa só cria 1x |
| 29 | Site público da vidraçaria | — | Página da empresa (logo, cores, "sobre", pedidos pelo canal SITE) | **Só no backend** (`/api/public/empresas/{slug}`); o site não existe neste repositório |
| 30 | Deslocamento global e tolerância de prumo | Parâmetros de Cálculo | Regra (fixo, por km, por bairro), valor e tolerância em mm | **Sem efeito**: gravados, mas nenhum cálculo lê. O deslocamento cobrado vem do componente da tabela |
| 31 | Referência de mercado por cidade | Configuração Expressa, passo "Cidade e UF" | "Referência de mercado da sua região" | **Não implementado**: o campo é pedido e não é usado |
| 32 | Cadastro público de empresas | `POST /auth/cadastro` | Autocadastro | **Desativado** em produção: o super admin cria as empresas |
| 33 | Painel do super admin | `/superadmin` | Criar, editar e ativar empresas, redefinir senha, status do WhatsApp | Interno do operador: no Anexo A do manual (`40-operador-login.jpg`, `41-operador-empresas.jpg`, `42-operador-nova-empresa.jpg`), separado das seções da vidraçaria |
| 34 | Pedido ao fornecedor/têmpera, plano de corte, sobras, estoque | — | — | **Não existe** |
| 35 | E-mail, nota fiscal, gateway de Pix/boleto, PDF do orçamento | — | — | **Não existe**: a única integração é o WhatsApp |
| 36 | Lembrete ao cliente na véspera da visita | — | — | **Não existe**; o único lembrete é o do orçamento sem resposta |
