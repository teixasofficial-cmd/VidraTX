#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")/.."

echo "==> Backup antes de atualizar"
docker compose exec -T backup bash /scripts/backup.sh agora

echo "==> Baixando a versão nova"
git pull --ff-only

echo "==> Reconstruindo e reiniciando"
docker compose up -d --build

docker image prune -f > /dev/null
docker compose ps
