package br.com.vidratx.exception;

public class EmpresaNaoEncontradaException
        extends RuntimeException {

    public EmpresaNaoEncontradaException(String mensagem) {
        super(mensagem);
    }
}
