package br.com.vidratx.dto;

import jakarta.validation.constraints.Size;

public class MedicaoRealizarRequest {

    @Size(
            max = 2000,
            message = "Observações devem ter no máximo 2000 caracteres"
    )
    private String observacoes;

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
}
