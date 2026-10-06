#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "${BASH_SOURCE[0]}")"

LOG_DIR=".dev-logs"
PID_FILE="$LOG_DIR/pids"
SECRETS_FILE=".dev-secrets"
MYSQL_CONTAINER="vidratx-mysql"
EMPRESA_SLUG="vidracaria-teste"
EMPRESA_EMAIL="voce@vidracariateste.com"
EMPRESA_SENHA="uma-senha-com-8-ou-mais-caracteres"

log() { printf '\n\033[1;36m==> %s\033[0m\n' "$1"; }
fail() { printf '\n\033[1;31mERRO: %s\033[0m\n' "$1" >&2; exit 1; }

stop_all() {
    if [ -f "$PID_FILE" ]; then
        log "Parando processos deste script"
        while read -r pid; do
            kill "$pid" 2>/dev/null || true
        done < "$PID_FILE"
        rm -f "$PID_FILE"
    fi
    docker stop "$MYSQL_CONTAINER" >/dev/null 2>&1 || true
    log "Parado. Se o MySQL era um container deste script, ele só foi parado (docker start $MYSQL_CONTAINER religa com os dados intactos)."
    exit 0
}

if [ "${1:-}" = "stop" ]; then
    stop_all
fi

mkdir -p "$LOG_DIR"
: > "$PID_FILE"

command -v mvn    >/dev/null || fail "Maven não encontrado (mvn)."
command -v node   >/dev/null || fail "Node.js não encontrado."
command -v npm    >/dev/null || fail "npm não encontrado."
command -v java   >/dev/null || fail "Java não encontrado."

JAVA_MAJOR=$(java -version 2>&1 | head -1 | grep -oE '"[0-9]+' | tr -d '"')
if [ "${JAVA_MAJOR:-0}" -lt 25 ]; then
    fail "JDK 25 é obrigatório (pom.xml está fixado nessa versão). Você tem Java $JAVA_MAJOR. Instale o JDK 25 e rode de novo."
fi

if [ ! -f "$SECRETS_FILE" ]; then
    log "Gerando segredos de desenvolvimento em $SECRETS_FILE (não commitado)"
    {
        echo "DB_PASSWORD=root"
        echo "JWT_SECRET=$(openssl rand -hex 32)"
        echo "WHATSAPP_GATEWAY_TOKEN=$(openssl rand -hex 24)"
    } > "$SECRETS_FILE"
fi
# shellcheck disable=SC1090
source "$SECRETS_FILE"
export DB_PASSWORD JWT_SECRET WHATSAPP_GATEWAY_TOKEN
export CORS_ALLOWED_ORIGINS="http://localhost:5173"
export CADASTRO_PUBLICO_HABILITADO=true
export API_DOCS_HABILITADO=true

log "Verificando MySQL"
if (exec 3<>/dev/tcp/127.0.0.1/3306) 2>/dev/null; then
    exec 3<&- 3>&- 2>/dev/null || true
    echo "Já tem um MySQL respondendo em :3306, usando ele (sem Docker)."
    if command -v mysql >/dev/null; then
        mysql -h127.0.0.1 -uroot -p"$DB_PASSWORD" -e "CREATE DATABASE IF NOT EXISTS vidratx;" 2>/dev/null \
            || echo "Aviso: não criei o banco 'vidratx' automaticamente (senha/usuário diferente de root:\$DB_PASSWORD). Crie manualmente se precisar."
    fi
elif command -v docker >/dev/null; then
    echo "Nenhum MySQL local, subindo via Docker"
    if docker ps -a --format '{{.Names}}' | grep -qx "$MYSQL_CONTAINER"; then
        docker start "$MYSQL_CONTAINER" >/dev/null
    else
        docker run --name "$MYSQL_CONTAINER" \
            -e MYSQL_ROOT_PASSWORD="$DB_PASSWORD" \
            -e MYSQL_DATABASE=vidratx \
            -p 3306:3306 -d mysql:8 >/dev/null
    fi

    printf 'Esperando o MySQL aceitar conexões'
    for _ in $(seq 1 60); do
        if docker exec "$MYSQL_CONTAINER" mysqladmin ping -uroot -p"$DB_PASSWORD" --silent >/dev/null 2>&1; then
            echo
            break
        fi
        printf '.'
        sleep 2
    done
else
    fail "Nem um MySQL local em :3306 nem Docker foram encontrados. Instale o MySQL (ou use Docker) e rode de novo. No Windows sem virtualização, veja dev.ps1."
fi

log "Subindo backend (mvn spring-boot:run) — log em $LOG_DIR/backend.log"
mvn spring-boot:run > "$LOG_DIR/backend.log" 2>&1 &
echo $! >> "$PID_FILE"

printf 'Esperando o backend responder em :8080'
for _ in $(seq 1 90); do
    if curl -sf -o /dev/null http://localhost:8080/actuator/health 2>/dev/null || \
       curl -s -o /dev/null -w '%{http_code}' http://localhost:8080/auth/login 2>/dev/null | grep -qE '^[0-9]+$'; then
        echo
        break
    fi
    printf '.'
    sleep 2
done

if ! curl -s -o /dev/null http://localhost:8080/auth/login; then
    fail "Backend não respondeu a tempo. Veja $LOG_DIR/backend.log"
fi

log "Garantindo empresa de teste ($EMPRESA_SLUG)"
curl -s -o /dev/null -X POST http://localhost:8080/auth/cadastro \
    -H "Content-Type: application/json" \
    -d '{
        "razaoSocial": "Vidraçaria Teste LTDA",
        "nomeFantasia": "Vidraçaria Teste",
        "cnpj": "11222333000181",
        "slug": "'"$EMPRESA_SLUG"'",
        "emailEmpresa": "contato@vidracariateste.com",
        "telefone": "11988887777",
        "administrador": {
            "nome": "Seu Nome",
            "email": "'"$EMPRESA_EMAIL"'",
            "senha": "'"$EMPRESA_SENHA"'"
        }
    }' || true

log "Subindo whatsapp-service — log em $LOG_DIR/whatsapp.log"
(
    cd whatsapp-service
    if [ ! -f .env ]; then
        sed "s/^GATEWAY_TOKEN=.*/GATEWAY_TOKEN=$WHATSAPP_GATEWAY_TOKEN/" .env.example > .env
    fi
    [ -d node_modules ] || npm install
    npm run dev
) > "$LOG_DIR/whatsapp.log" 2>&1 &
echo $! >> "$PID_FILE"

log "Subindo dashboard — log em $LOG_DIR/dashboard.log"
(
    cd dashboard
    [ -f .env.local ] || cp .env.example .env.local
    [ -d node_modules ] || npm install
    npm run dev -- --host
) > "$LOG_DIR/dashboard.log" 2>&1 &
echo $! >> "$PID_FILE"

printf 'Esperando o dashboard responder em :5173'
for _ in $(seq 1 60); do
    if curl -s -o /dev/null http://localhost:5173; then
        echo
        break
    fi
    printf '.'
    sleep 1
done

log "Pronto"
cat <<EOF

Abra:      http://localhost:5173/entrar
Empresa:   $EMPRESA_SLUG
E-mail:    $EMPRESA_EMAIL
Senha:     $EMPRESA_SENHA

Logs:      $LOG_DIR/{backend,whatsapp,dashboard}.log
Para parar: ./dev.sh stop
EOF
