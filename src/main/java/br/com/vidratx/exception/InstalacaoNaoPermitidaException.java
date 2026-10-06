package br.com.vidratx.exception;

public class InstalacaoNaoPermitidaException
        extends RuntimeException {

    public InstalacaoNaoPermitidaException(String mensagem) {
        super(mensagem);
    }
}
