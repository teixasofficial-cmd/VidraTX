package br.com.vidratx.exception;

import java.math.BigDecimal;

public class PrecoAlteradoException extends RuntimeException {

    private final BigDecimal valorEsperado;
    private final BigDecimal valorAtual;

    public PrecoAlteradoException(String mensagem, BigDecimal valorEsperado, BigDecimal valorAtual) {
        super(mensagem);
        this.valorEsperado = valorEsperado;
        this.valorAtual = valorAtual;
    }

    public BigDecimal getValorEsperado() {
        return valorEsperado;
    }

    public BigDecimal getValorAtual() {
        return valorAtual;
    }
}
