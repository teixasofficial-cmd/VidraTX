package br.com.vidratx.exception;

public class AtendimentoNaoEncontradoException
        extends RuntimeException {

    public AtendimentoNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
