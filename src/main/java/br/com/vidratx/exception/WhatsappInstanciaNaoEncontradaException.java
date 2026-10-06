package br.com.vidratx.exception;

public class WhatsappInstanciaNaoEncontradaException
        extends RuntimeException {

    public WhatsappInstanciaNaoEncontradaException(String mensagem) {
        super(mensagem);
    }
}
