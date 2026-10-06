package br.com.vidratx.exception;

public class DocumentoDuplicadoException
        extends RuntimeException {

    public DocumentoDuplicadoException(String mensagem) {
        super(mensagem);
    }
}
