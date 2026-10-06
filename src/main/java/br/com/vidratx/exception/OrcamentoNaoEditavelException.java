package br.com.vidratx.exception;

public class OrcamentoNaoEditavelException
        extends RuntimeException {

    public OrcamentoNaoEditavelException(String mensagem) {
        super(mensagem);
    }
}
