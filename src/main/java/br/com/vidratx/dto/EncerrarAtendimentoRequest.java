package br.com.vidratx.dto;

import jakarta.validation.constraints.Size;

public class EncerrarAtendimentoRequest {

    private boolean forcar;

    @Size(max = 300, message = "Motivo deve ter no máximo 300 caracteres")
    private String motivo;

    public boolean isForcar() {
        return forcar;
    }

    public void setForcar(boolean forcar) {
        this.forcar = forcar;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
