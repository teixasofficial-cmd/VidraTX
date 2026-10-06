package br.com.vidratx.exception;

public class MedicaoJaExisteException
        extends RuntimeException {

    public MedicaoJaExisteException(String mensagem) {
        super(mensagem);
    }
}
