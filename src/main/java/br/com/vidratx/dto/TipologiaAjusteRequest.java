package br.com.vidratx.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class TipologiaAjusteRequest {

    @NotNull(message = "Desconto de largura é obrigatório")
    @Min(value = 0, message = "Desconto de largura não pode ser negativo")
    private Integer descontoLarguraMm;

    @NotNull(message = "Desconto de altura é obrigatório")
    @Min(value = 0, message = "Desconto de altura não pode ser negativo")
    private Integer descontoAlturaMm;

    @NotNull(message = "Transpasse é obrigatório")
    @Min(value = 0, message = "Transpasse não pode ser negativo")
    private Integer transpasseMm;

    private Boolean ativo;

    public Integer getDescontoLarguraMm() {
        return descontoLarguraMm;
    }

    public void setDescontoLarguraMm(Integer descontoLarguraMm) {
        this.descontoLarguraMm = descontoLarguraMm;
    }

    public Integer getDescontoAlturaMm() {
        return descontoAlturaMm;
    }

    public void setDescontoAlturaMm(Integer descontoAlturaMm) {
        this.descontoAlturaMm = descontoAlturaMm;
    }

    public Integer getTranspasseMm() {
        return transpasseMm;
    }

    public void setTranspasseMm(Integer transpasseMm) {
        this.transpasseMm = transpasseMm;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
}
