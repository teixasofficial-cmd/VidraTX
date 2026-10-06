package br.com.vidratx.enums;

public enum CategoriaMensagemSaida {

    BOT,
    ATENDENTE,
    PROPOSTA_DATA,
    CONFIRMACAO,
    CANCELAMENTO,
    ORCAMENTO,
    ESTIMATIVA,
    LEMBRETE,
    AVISO,
    ESCALONAMENTO,
    CONCLUSAO;

    public boolean respeitaHorarioComercial() {
        return this == LEMBRETE || this == AVISO;
    }

    public boolean respondeAoCliente() {
        return this != LEMBRETE && this != AVISO && this != ESCALONAMENTO && this != CONCLUSAO;
    }
}
