package br.com.vidratx.enums;

public enum TipoPergunta {

    CONFIRMAR_MEDICAO(TipoReferenciaPergunta.MEDICAO),
    SUGERIR_DATA_MEDICAO(TipoReferenciaPergunta.MEDICAO),
    CONFIRMAR_INSTALACAO(TipoReferenciaPergunta.INSTALACAO),
    SUGERIR_DATA_INSTALACAO(TipoReferenciaPergunta.INSTALACAO),
    APROVAR_ORCAMENTO(TipoReferenciaPergunta.ORCAMENTO);

    private final TipoReferenciaPergunta referencia;

    TipoPergunta(TipoReferenciaPergunta referencia) {
        this.referencia = referencia;
    }

    public TipoReferenciaPergunta getReferencia() {
        return referencia;
    }

    public boolean ehConfirmacaoDeData() {
        return this == CONFIRMAR_MEDICAO || this == CONFIRMAR_INSTALACAO;
    }

    public boolean ehSugestaoDeData() {
        return this == SUGERIR_DATA_MEDICAO || this == SUGERIR_DATA_INSTALACAO;
    }

    public static TipoPergunta confirmar(TipoAgendamento tipo) {
        return tipo == TipoAgendamento.MEDICAO ? CONFIRMAR_MEDICAO : CONFIRMAR_INSTALACAO;
    }

    public static TipoPergunta sugerirData(TipoAgendamento tipo) {
        return tipo == TipoAgendamento.MEDICAO ? SUGERIR_DATA_MEDICAO : SUGERIR_DATA_INSTALACAO;
    }

    public enum TipoReferenciaPergunta {
        MEDICAO,
        INSTALACAO,
        ORCAMENTO
    }
}
