package br.com.vidratx.dto;

import java.math.BigDecimal;

public class OrcamentoPecaResponse {

    private Long id;
    private String descricao;
    private Integer larguraCorteMm;
    private Integer alturaCorteMm;
    private Integer quantidade;
    private BigDecimal areaM2;
    private Boolean excedeTamanhoMaximo;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Integer getLarguraCorteMm() {
        return larguraCorteMm;
    }

    public void setLarguraCorteMm(Integer larguraCorteMm) {
        this.larguraCorteMm = larguraCorteMm;
    }

    public Integer getAlturaCorteMm() {
        return alturaCorteMm;
    }

    public void setAlturaCorteMm(Integer alturaCorteMm) {
        this.alturaCorteMm = alturaCorteMm;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public BigDecimal getAreaM2() {
        return areaM2;
    }

    public void setAreaM2(BigDecimal areaM2) {
        this.areaM2 = areaM2;
    }

    public Boolean getExcedeTamanhoMaximo() {
        return excedeTamanhoMaximo;
    }

    public void setExcedeTamanhoMaximo(Boolean excedeTamanhoMaximo) {
        this.excedeTamanhoMaximo = excedeTamanhoMaximo;
    }
}
