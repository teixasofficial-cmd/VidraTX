package br.com.vidratx.exception;

public class FotoNaoEncontradaException
        extends RuntimeException {

    public FotoNaoEncontradaException(String mensagem) {
        super(mensagem);
    }
}
