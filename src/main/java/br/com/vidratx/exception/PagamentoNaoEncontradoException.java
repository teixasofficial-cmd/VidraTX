package br.com.vidratx.exception;

public class PagamentoNaoEncontradoException
        extends RuntimeException {

    public PagamentoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
