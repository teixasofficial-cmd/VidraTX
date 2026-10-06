package br.com.vidratx.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class PosVendaResolverRequest {

    @NotBlank(message = "Solução é obrigatória")
    @Size(
            max = 2000,
            message = "Solução deve ter no máximo 2000 caracteres"
    )
    private String solucao;

    public String getSolucao() {
        return solucao;
    }

    public void setSolucao(String solucao) {
        this.solucao = solucao;
    }
}
