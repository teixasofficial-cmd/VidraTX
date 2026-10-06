package br.com.vidratx.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PosVendaAtendimentoRequest {

    @NotBlank(message = "Atendimento é obrigatório")
    @Size(
            max = 2000,
            message = "Atendimento deve ter no máximo 2000 caracteres"
    )
    private String atendimento;

    public String getAtendimento() {
        return atendimento;
    }

    public void setAtendimento(String atendimento) {
        this.atendimento = atendimento;
    }
}
