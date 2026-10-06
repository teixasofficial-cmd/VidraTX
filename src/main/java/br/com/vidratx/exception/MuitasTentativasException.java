package br.com.vidratx.exception;

public class MuitasTentativasException
        extends RuntimeException {

    public MuitasTentativasException(String mensagem) {
        super(mensagem);
    }
}
