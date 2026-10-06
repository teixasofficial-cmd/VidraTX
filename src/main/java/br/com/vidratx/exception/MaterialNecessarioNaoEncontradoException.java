package br.com.vidratx.exception;

public class MaterialNecessarioNaoEncontradoException
        extends RuntimeException {

    public MaterialNecessarioNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
