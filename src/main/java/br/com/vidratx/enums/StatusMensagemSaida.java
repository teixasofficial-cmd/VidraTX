package br.com.vidratx.enums;

public enum StatusMensagemSaida {

    PENDENTE,
    ENVIANDO,
    ENVIADA,
    ENTREGUE,
    LIDA,
    FALHOU,
    CANCELADA;

    public boolean chegouAoWhatsapp() {
        return this == ENVIADA || this == ENTREGUE || this == LIDA;
    }
}
