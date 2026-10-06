package br.com.vidratx.exception;

public class ProducaoNaoAplicavelException
        extends RuntimeException {

    public ProducaoNaoAplicavelException(String mensagem) {
        super(mensagem);
    }
}
