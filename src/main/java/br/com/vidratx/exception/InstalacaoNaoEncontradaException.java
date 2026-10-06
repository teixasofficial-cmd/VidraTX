package br.com.vidratx.exception;

public class InstalacaoNaoEncontradaException
        extends RuntimeException {

    public InstalacaoNaoEncontradaException(String mensagem) {
        super(mensagem);
    }
}
