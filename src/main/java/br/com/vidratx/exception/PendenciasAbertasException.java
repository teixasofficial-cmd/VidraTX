package br.com.vidratx.exception;

import java.util.List;

public class PendenciasAbertasException extends RuntimeException {

    private final List<String> pendencias;

    public PendenciasAbertasException(String mensagem, List<String> pendencias) {
        super(mensagem);
        this.pendencias = List.copyOf(pendencias);
    }

    public List<String> getPendencias() {
        return pendencias;
    }
}
