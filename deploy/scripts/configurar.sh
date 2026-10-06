#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")/.."

if [[ -e .env ]]; then
    echo "O arquivo .env já existe. Apague-o antes se quiser gerar outro." >&2
    exit 1
fi

read -r -p "Domínio do painel (ex.: painel.suavidracaria.com.br): " DOMINIO
read -r -p "E-mail para os avisos do certificado HTTPS: " EMAIL

DOMINIO="${DOMINIO#http://}"
DOMINIO="${DOMINIO#https://}"
DOMINIO="${DOMINIO%%/*}"

if ! [[ "$DOMINIO" =~ ^[A-Za-z0-9.-]+$ ]]; then
    echo "Domínio inválido: '$DOMINIO'" >&2
    exit 1
fi

if ! [[ "$EMAIL" =~ ^[^@[:space:]]+@[^@[:space:]]+\.[^@[:space:]]+$ ]]; then
    echo "E-mail inválido: '$EMAIL'" >&2
    exit 1
fi

segredo() {
    if command -v openssl > /dev/null; then
        openssl rand -hex 32
    else
        head -c 32 /dev/urandom | od -An -tx1 | tr -d ' \n'
    fi
}

umask 077
sed -e "s|^DOMINIO=.*|DOMINIO=$DOMINIO|" \
    -e "s|^EMAIL_ACME=.*|EMAIL_ACME=$EMAIL|" \
    -e "s|^MYSQL_ROOT_PASSWORD=.*|MYSQL_ROOT_PASSWORD=$(segredo)|" \
    -e "s|^DB_PASSWORD=.*|DB_PASSWORD=$(segredo)|" \
    -e "s|^JWT_SECRET=.*|JWT_SECRET=$(segredo)|" \
    -e "s|^WHATSAPP_GATEWAY_TOKEN=.*|WHATSAPP_GATEWAY_TOKEN=$(segredo)|" \
    .env.example > .env

echo "Criado deploy/.env (só o seu usuário lê). Guarde uma cópia num gerenciador de"
echo "senhas: ele tem as senhas do banco e os segredos do sistema."
