#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")/.."

BANCO="${1:?informe o arquivo do banco (backups/vidratx_*.sql.gz)}"
MIDIA="${2:-}"

for arquivo in "$BANCO" ${MIDIA:+"$MIDIA"}; do
    if [[ ! -f "$arquivo" ]]; then
        echo "Arquivo não encontrado: $arquivo" >&2
        exit 1
    fi
done

gunzip -t "$BANCO"

echo "Isto substitui TODO o banco atual pelo conteúdo de $BANCO."
read -r -p 'Digite "restaurar" para continuar: ' RESPOSTA
if [[ "$RESPOSTA" != "restaurar" ]]; then
    echo "Nada foi alterado."
    exit 1
fi

echo "==> Parando backend e whatsapp-service"
docker compose stop whatsapp backend

echo "==> Recriando o banco a partir do backup"
docker compose exec -T mysql sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql -uroot -e "DROP DATABASE IF EXISTS vidratx; CREATE DATABASE vidratx;"'
gunzip -c "$BANCO" | docker compose exec -T mysql sh -c 'MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysql -uroot vidratx'

if [[ -n "$MIDIA" ]]; then
    echo "==> Restaurando as fotos"
    docker compose --progress quiet run --rm --no-deps -T --entrypoint tar backend -xzf - -C /app/midia-whatsapp < "$MIDIA"
fi

echo "==> Subindo backend e whatsapp-service"
docker compose up -d backend whatsapp

echo "Restauração concluída."
