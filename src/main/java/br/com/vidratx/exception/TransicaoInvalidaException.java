package br.com.vidratx.exception;

public class TransicaoInvalidaException
        extends RuntimeException {

    public TransicaoInvalidaException(String mensagem) {
        super(mensagem);
    }
}
