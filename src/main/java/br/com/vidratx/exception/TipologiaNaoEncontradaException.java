package br.com.vidratx.exception;

public class TipologiaNaoEncontradaException extends RuntimeException {

    public TipologiaNaoEncontradaException(String mensagem) {
        super(mensagem);
    }
}
