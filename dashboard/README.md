# dashboard

Painel administrativo do VidraTX. **Um único frontend, igual para todas as
empresas**: o multi-tenant acontece no login (cada empresa entra com o
próprio identificador) e todos os dados são filtrados no backend pelo token
JWT. O site público personalizado de cada vidraçaria é um frontend separado.

## Rodando localmente

```bash
cp .env.example .env.local
# ajuste VITE_API_BASE_URL se o backend não estiver em localhost:8080

npm install
npm run dev
```

Abre em `http://localhost:5173`. O backend Spring precisa estar no ar e
com `CORS_ALLOWED_ORIGINS` incluindo `http://localhost:5173`, senão o
navegador bloqueia as chamadas.

## Telas

| Rota | Tela |
|---|---|
| `/entrar` | Login da empresa (identificador, e-mail e senha) |
| `/` | Visão geral: clientes esperando no WhatsApp, próximas ações e agenda |
| `/atendimentos`, `/atendimentos/:id` | Fila e conversa do WhatsApp |
| `/clientes`, `/clientes/:id` | Clientes |
| `/orcamentos`, `/orcamentos/:id` | Funil (kanban) e orçamento com cálculo por item |
| `/tabela-precos`, `/parametros-calculo` | Preços e parâmetros do cálculo |
| `/servicos`, `/materiais` | Cadastros de apoio |
| `/ordens-servico`, `/ordens-servico/:id` | Produção, instalação e pós-venda |
| `/usuarios`, `/configuracoes/empresa`, `/configuracoes/whatsapp` | Equipe e configurações |
| `/superadmin/login`, `/superadmin` | Painel do operador do SaaS (cadastro de empresas) |

## Decisões de design

- **Stack**: Vite + React + TypeScript + Tailwind CSS v4 + TanStack Query
  (cache e polling) + React Router.
- **Sem tema por empresa**: a identidade visual de cada vidraçaria fica
  para o site público dela; o painel tem uma identidade única.
- **Autenticação**: token em `localStorage`, sem refresh token; quando
  expira (`JWT_EXPIRATION` do backend), o painel pede login de novo.

## Verificações

```bash
npm run lint    # tsc + ESLint
npm test        # Vitest
npm run build
```

## Licença

Software proprietário. Todos os direitos reservados. Veja [LICENSE](../LICENSE).
