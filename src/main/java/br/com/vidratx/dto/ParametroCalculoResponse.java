package br.com.vidratx.dto;

import br.com.vidratx.enums.ArredondamentoComercial;
import br.com.vidratx.enums.ModoPrecificacao;
import br.com.vidratx.enums.RegimeTributario;
import br.com.vidratx.enums.RegraDeslocamento;

import java.math.BigDecimal;
import java.util.Map;

public class ParametroCalculoResponse {

    private Long id;
    private Long empresaId;
    private ModoPrecificacao modoPrecificacao;
    private Short multiploArredondamentoMm;
    private BigDecimal areaMinimaM2;
    private BigDecimal percentualPerdas;
    private BigDecimal percentualImpostos;
    private BigDecimal percentualComissao;
    private BigDecimal percentualMargemDesejada;
    private BigDecimal percentualMargemMinima;
    private Map<Integer, BigDecimal> taxaCartaoParcelas;
    private ArredondamentoComercial arredondamentoComercial;
    private BigDecimal variacaoPreOrcamentoMinPct;
    private BigDecimal variacaoPreOrcamentoMaxPct;
    private BigDecimal valorVisitaTecnica;
    private RegraDeslocamento regraDeslocamento;
    private BigDecimal valorDeslocamento;
    private Integer validadePadraoDias;
    private Integer tamanhoMaximoChapaLarguraMm;
    private Integer tamanhoMaximoChapaAlturaMm;
    private Short toleranciaPrumoNivelMm;
    private RegimeTributario regimeTributario;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(Long empresaId) {
        this.empresaId = empresaId;
    }

    public ModoPrecificacao getModoPrecificacao() {
        return modoPrecificacao;
    }

    public void setModoPrecificacao(ModoPrecificacao modoPrecificacao) {
        this.modoPrecificacao = modoPrecificacao;
    }

    public Short getMultiploArredondamentoMm() {
        return multiploArredondamentoMm;
    }

    public void setMultiploArredondamentoMm(Short multiploArredondamentoMm) {
        this.multiploArredondamentoMm = multiploArredondamentoMm;
    }

    public BigDecimal getAreaMinimaM2() {
        return areaMinimaM2;
    }

    public void setAreaMinimaM2(BigDecimal areaMinimaM2) {
        this.areaMinimaM2 = areaMinimaM2;
    }

    public BigDecimal getPercentualPerdas() {
        return percentualPerdas;
    }

    public void setPercentualPerdas(BigDecimal percentualPerdas) {
        this.percentualPerdas = percentualPerdas;
    }

    public BigDecimal getPercentualImpostos() {
        return percentualImpostos;
    }

    public void setPercentualImpostos(BigDecimal percentualImpostos) {
        this.percentualImpostos = percentualImpostos;
    }

    public BigDecimal getPercentualComissao() {
        return percentualComissao;
    }

    public void setPercentualComissao(BigDecimal percentualComissao) {
        this.percentualComissao = percentualComissao;
    }

    public BigDecimal getPercentualMargemDesejada() {
        return percentualMargemDesejada;
    }

    public void setPercentualMargemDesejada(BigDecimal percentualMargemDesejada) {
        this.percentualMargemDesejada = percentualMargemDesejada;
    }

    public BigDecimal getPercentualMargemMinima() {
        return percentualMargemMinima;
    }

    public void setPercentualMargemMinima(BigDecimal percentualMargemMinima) {
        this.percentualMargemMinima = percentualMargemMinima;
    }

    public Map<Integer, BigDecimal> getTaxaCartaoParcelas() {
        return taxaCartaoParcelas;
    }

    public void setTaxaCartaoParcelas(Map<Integer, BigDecimal> taxaCartaoParcelas) {
        this.taxaCartaoParcelas = taxaCartaoParcelas;
    }

    public ArredondamentoComercial getArredondamentoComercial() {
        return arredondamentoComercial;
    }

    public void setArredondamentoComercial(ArredondamentoComercial arredondamentoComercial) {
        this.arredondamentoComercial = arredondamentoComercial;
    }

    public BigDecimal getVariacaoPreOrcamentoMinPct() {
        return variacaoPreOrcamentoMinPct;
    }

    public void setVariacaoPreOrcamentoMinPct(BigDecimal variacaoPreOrcamentoMinPct) {
        this.variacaoPreOrcamentoMinPct = variacaoPreOrcamentoMinPct;
    }

    public BigDecimal getVariacaoPreOrcamentoMaxPct() {
        return variacaoPreOrcamentoMaxPct;
    }

    public void setVariacaoPreOrcamentoMaxPct(BigDecimal variacaoPreOrcamentoMaxPct) {
        this.variacaoPreOrcamentoMaxPct = variacaoPreOrcamentoMaxPct;
    }

    public BigDecimal getValorVisitaTecnica() {
        return valorVisitaTecnica;
    }

    public void setValorVisitaTecnica(BigDecimal valorVisitaTecnica) {
        this.valorVisitaTecnica = valorVisitaTecnica;
    }

    public RegraDeslocamento getRegraDeslocamento() {
        return regraDeslocamento;
    }

    public void setRegraDeslocamento(RegraDeslocamento regraDeslocamento) {
        this.regraDeslocamento = regraDeslocamento;
    }

    public BigDecimal getValorDeslocamento() {
        return valorDeslocamento;
    }

    public void setValorDeslocamento(BigDecimal valorDeslocamento) {
        this.valorDeslocamento = valorDeslocamento;
    }

    public Integer getValidadePadraoDias() {
        return validadePadraoDias;
    }

    public void setValidadePadraoDias(Integer validadePadraoDias) {
        this.validadePadraoDias = validadePadraoDias;
    }

    public Integer getTamanhoMaximoChapaLarguraMm() {
        return tamanhoMaximoChapaLarguraMm;
    }

    public void setTamanhoMaximoChapaLarguraMm(Integer tamanhoMaximoChapaLarguraMm) {
        this.tamanhoMaximoChapaLarguraMm = tamanhoMaximoChapaLarguraMm;
    }

    public Integer getTamanhoMaximoChapaAlturaMm() {
        return tamanhoMaximoChapaAlturaMm;
    }

    public void setTamanhoMaximoChapaAlturaMm(Integer tamanhoMaximoChapaAlturaMm) {
        this.tamanhoMaximoChapaAlturaMm = tamanhoMaximoChapaAlturaMm;
    }

    public Short getToleranciaPrumoNivelMm() {
        return toleranciaPrumoNivelMm;
    }

    public void setToleranciaPrumoNivelMm(Short toleranciaPrumoNivelMm) {
        this.toleranciaPrumoNivelMm = toleranciaPrumoNivelMm;
    }

    public RegimeTributario getRegimeTributario() {
        return regimeTributario;
    }

    public void setRegimeTributario(RegimeTributario regimeTributario) {
        this.regimeTributario = regimeTributario;
    }
}
