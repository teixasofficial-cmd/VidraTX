package br.com.vidratx.exception;

public class OrcamentoNaoEncontradoException
        extends RuntimeException {

    public OrcamentoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
