package br.com.vidratx.exception;

public class SolicitacaoOrcamentoNaoEncontradaException
        extends RuntimeException {

    public SolicitacaoOrcamentoNaoEncontradaException(String mensagem) {
        super(mensagem);
    }
}
