package br.com.vidratx.exception;

public class ClienteNaoEncontradoException
        extends RuntimeException {

    public ClienteNaoEncontradoException(String mensagem) {
        super(mensagem);
    }
}
