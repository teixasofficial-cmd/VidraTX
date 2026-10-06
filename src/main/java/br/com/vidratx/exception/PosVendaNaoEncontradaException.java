package br.com.vidratx.exception;

public class PosVendaNaoEncontradaException
        extends RuntimeException {

    public PosVendaNaoEncontradaException(String mensagem) {
        super(mensagem);
    }
}
