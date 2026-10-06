package br.com.vidratx.exception;

public class SlugDuplicadoException
        extends RuntimeException {

    public SlugDuplicadoException(String mensagem) {
        super(mensagem);
    }
}
