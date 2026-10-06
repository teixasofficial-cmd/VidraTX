package br.com.vidratx.exception;

public class TabelaPrecoNaoEncontradaException extends RuntimeException {

    public TabelaPrecoNaoEncontradaException(String mensagem) {
        super(mensagem);
    }
}
