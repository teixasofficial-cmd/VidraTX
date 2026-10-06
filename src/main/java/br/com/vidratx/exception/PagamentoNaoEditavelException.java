package br.com.vidratx.exception;

public class PagamentoNaoEditavelException
        extends RuntimeException {

    public PagamentoNaoEditavelException(String mensagem) {
        super(mensagem);
    }
}
