package br.com.vidratx.exception;

public class MensagemNaoEncontradaException
        extends RuntimeException {

    public MensagemNaoEncontradaException(String mensagem) {
        super(mensagem);
    }
}
