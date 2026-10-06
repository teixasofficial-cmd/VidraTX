package br.com.vidratx.exception;

public class ServicoDuplicadoException
        extends RuntimeException {

    public ServicoDuplicadoException(String mensagem) {
        super(mensagem);
    }
}
