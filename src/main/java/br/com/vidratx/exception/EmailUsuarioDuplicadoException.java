package br.com.vidratx.exception;

public class EmailUsuarioDuplicadoException
        extends RuntimeException {

    public EmailUsuarioDuplicadoException(String mensagem) {
        super(mensagem);
    }
}
