package br.com.vidratx.dto;

import jakarta.validation.constraints.Size;

public class ReabrirOrcamentoRequest {

    @Size(max = 300, message = "Motivo deve ter no máximo 300 caracteres")
    private String motivo;

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }
}
