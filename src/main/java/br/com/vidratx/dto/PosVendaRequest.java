package br.com.vidratx.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PosVendaRequest {

    @NotBlank(message = "Descrição do problema é obrigatória")
    @Size(
            max = 2000,
            message = "Problema deve ter no máximo 2000 caracteres"
    )
    private String problema;

    public String getProblema() {
        return problema;
    }

    public void setProblema(String problema) {
        this.problema = problema;
    }
}
