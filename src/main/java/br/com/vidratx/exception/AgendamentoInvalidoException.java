package br.com.vidratx.exception;

public class AgendamentoInvalidoException extends RuntimeException {

    private final String campo;

    public AgendamentoInvalidoException(String campo, String mensagem) {
        super(mensagem);
        this.campo = campo;
    }

    public String getCampo() {
        return campo;
    }
}
