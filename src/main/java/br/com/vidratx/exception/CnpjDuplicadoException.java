package br.com.vidratx.exception;

public class CnpjDuplicadoException
        extends RuntimeException {

    public CnpjDuplicadoException(String mensagem) {
        super(mensagem);
    }
}
