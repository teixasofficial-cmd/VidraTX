#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")/.."

EMAIL="${1:-admin@vidratx.local}"

if ! [[ "$EMAIL" =~ ^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\.[A-Za-z]{2,}$ ]]; then
    echo "E-mail inválido: '$EMAIL'" >&2
    exit 1
fi

read -r -s -p "Nova senha para $EMAIL (mínimo 8 caracteres): " SENHA
echo
read -r -s -p "Repita a senha: " CONFIRMACAO
echo

if [[ "$SENHA" != "$CONFIRMACAO" ]]; then
    echo "As senhas não conferem." >&2
    exit 1
fi

if ! HASH=$(printf '%s\n' "$SENHA" | docker compose --progress quiet run --rm -T --no-deps backend gerar-hash-senha); then
    exit 1
fi
unset SENHA CONFIRMACAO

if ! [[ "$HASH" =~ ^\$2[aby]\$12\$[./A-Za-z0-9]{53}$ ]]; then
    echo "O backend devolveu um hash inesperado; nada foi alterado." >&2
    exit 1
fi

docker compose exec -T mysql sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql -uroot vidratx' <<SQL
INSERT INTO administrador_global (email, senha, ativo, criado_em, atualizado_em)
VALUES ('$EMAIL', '$HASH', TRUE, NOW(), NOW()) AS nova
ON DUPLICATE KEY UPDATE senha = nova.senha, ativo = TRUE, atualizado_em = NOW();
SQL

echo "Senha de $EMAIL definida. Entre em https://<seu domínio>/superadmin/login."
