package br.com.vidratx.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public class OrdemServicoRequest {

    @NotNull(message = "Orçamento é obrigatório")
    @Positive(message = "Orçamento inválido")
    private Long orcamentoId;

    private Boolean necessitaProducao = true;

    @Size(
            max = 2000,
            message = "Observações devem ter no máximo 2000 caracteres"
    )
    private String observacoes;

    public Long getOrcamentoId() {
        return orcamentoId;
    }

    public void setOrcamentoId(Long orcamentoId) {
        this.orcamentoId = orcamentoId;
    }

    public Boolean getNecessitaProducao() {
        return necessitaProducao;
    }

    public void setNecessitaProducao(Boolean necessitaProducao) {
        this.necessitaProducao = necessitaProducao;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
}
