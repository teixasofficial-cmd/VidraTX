#!/usr/bin/env bash
set -uo pipefail

HORA="${BACKUP_HORA:-3}"
RETENCAO="${BACKUP_RETENCAO_DIAS:-14}"

umask 077

DONO="$(stat -c %u:%g /scripts)"

registrar() {
    echo "$(date '+%F %T') $*"
}

fazer_backup() {

    local carimbo banco midia
    carimbo="$(date +%Y-%m-%d_%H%M)"
    banco="/backups/vidratx_${carimbo}.sql.gz"
    midia="/backups/midia_${carimbo}.tar.gz"

    if ! MYSQL_PWD="$MYSQL_ROOT_PASSWORD" mysqldump -h mysql -uroot \
            --single-transaction --routines --triggers --no-tablespaces vidratx \
            | gzip > "${banco}.parcial"; then
        rm -f "${banco}.parcial"
        registrar "ERRO: o backup do banco falhou"
        return 1
    fi
    mv "${banco}.parcial" "$banco"

    if ! tar -czf "${midia}.parcial" -C /midia .; then
        rm -f "${midia}.parcial"
        registrar "ERRO: o backup das fotos falhou (o do banco foi gravado)"
        return 1
    fi
    mv "${midia}.parcial" "$midia"

    chown "$DONO" /backups "$banco" "$midia"

    find /backups -maxdepth 1 \( -name 'vidratx_*.sql.gz' -o -name 'midia_*.tar.gz' \) \
        -mtime +"$RETENCAO" -delete

    registrar "backup concluído: $(basename "$banco") e $(basename "$midia")"
}

if [[ "${1:-}" == "agora" ]]; then
    fazer_backup
    exit $?
fi

if ! [[ "$HORA" =~ ^([01]?[0-9]|2[0-3])$ ]]; then
    registrar "ERRO: BACKUP_HORA precisa ser uma hora de 0 a 23 (recebido: $HORA)"
    exit 1
fi

registrar "backup diário às ${HORA}h, guardando ${RETENCAO} dias"

while true; do
    agora=$(date +%s)
    proximo=$(date -d "today ${HORA}:00" +%s)
    if (( proximo <= agora )); then
        proximo=$(date -d "tomorrow ${HORA}:00" +%s)
    fi
    sleep $(( proximo - agora ))
    fazer_backup
done
