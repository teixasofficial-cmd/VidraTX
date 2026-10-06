package br.com.vidratx.exception;

public class ServicoNaoEncontradoException
        extends RuntimeException {

    public ServicoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
