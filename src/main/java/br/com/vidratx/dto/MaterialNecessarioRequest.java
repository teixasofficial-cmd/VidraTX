package br.com.vidratx.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class MaterialNecessarioRequest {

    @NotNull(message = "Material é obrigatório")
    @Positive(message = "Material inválido")
    private Long materialId;

    @NotNull(message = "Quantidade é obrigatória")
    @DecimalMin(
            value = "0.0",
            inclusive = false,
            message = "Quantidade deve ser maior que zero"
    )
    private BigDecimal quantidade;

    @Size(
            max = 20,
            message = "Unidade deve ter no máximo 20 caracteres"
    )
    private String unidade;

    @Size(
            max = 500,
            message = "Observação deve ter no máximo 500 caracteres"
    )
    private String observacao;

    public Long getMaterialId() {
        return materialId;
    }

    public void setMaterialId(Long materialId) {
        this.materialId = materialId;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(BigDecimal quantidade) {
        this.quantidade = quantidade;
    }

    public String getUnidade() {
        return unidade;
    }

    public void setUnidade(String unidade) {
        this.unidade = unidade;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }
}
