package br.com.vidratx.exception;

public class MaterialNaoEncontradoException
        extends RuntimeException {

    public MaterialNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
