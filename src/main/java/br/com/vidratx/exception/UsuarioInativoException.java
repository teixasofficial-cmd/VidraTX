package br.com.vidratx.exception;

public class UsuarioInativoException
        extends RuntimeException {

    public UsuarioInativoException(String mensagem) {
        super(mensagem);
    }
}
