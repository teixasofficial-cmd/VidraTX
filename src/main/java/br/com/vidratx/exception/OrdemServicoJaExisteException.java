package br.com.vidratx.exception;

public class OrdemServicoJaExisteException
        extends RuntimeException {

    public OrdemServicoJaExisteException(String mensagem) {
        super(mensagem);
    }
}
