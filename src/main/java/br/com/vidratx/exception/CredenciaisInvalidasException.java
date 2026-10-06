package br.com.vidratx.exception;

public class CredenciaisInvalidasException
        extends RuntimeException {

    public CredenciaisInvalidasException(String mensagem) {
        super(mensagem);
    }
}
