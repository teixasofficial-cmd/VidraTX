package br.com.vidratx.exception;

public class MaterialDuplicadoException
        extends RuntimeException {

    public MaterialDuplicadoException(String mensagem) {
        super(mensagem);
    }
}
