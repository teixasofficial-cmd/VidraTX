package br.com.vidratx.exception;

public class OrcamentoNaoAprovadoException
        extends RuntimeException {

    public OrcamentoNaoAprovadoException(String mensagem) {
        super(mensagem);
    }
}
