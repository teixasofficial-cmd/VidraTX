package br.com.vidratx.exception;

public class InstalacaoJaExisteException
        extends RuntimeException {

    public InstalacaoJaExisteException(String mensagem) {
        super(mensagem);
    }
}
