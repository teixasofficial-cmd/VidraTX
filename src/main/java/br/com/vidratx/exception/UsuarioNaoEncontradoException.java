package br.com.vidratx.exception;

public class UsuarioNaoEncontradoException
        extends RuntimeException {

    public UsuarioNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
