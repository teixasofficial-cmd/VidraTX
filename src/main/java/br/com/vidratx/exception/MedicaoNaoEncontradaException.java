package br.com.vidratx.exception;

public class MedicaoNaoEncontradaException
        extends RuntimeException {

    public MedicaoNaoEncontradaException(String mensagem) {
        super(mensagem);
    }
}
