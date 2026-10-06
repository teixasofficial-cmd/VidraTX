package br.com.vidratx.dto;

import br.com.vidratx.enums.ArredondamentoComercial;
import br.com.vidratx.enums.ModoPrecificacao;
import br.com.vidratx.enums.RegimeTributario;
import br.com.vidratx.enums.RegraDeslocamento;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.Map;

public class ParametroCalculoRequest {

    @NotNull(message = "Modo de precificação é obrigatório")
    private ModoPrecificacao modoPrecificacao;

    @Min(value = 1, message = "Múltiplo de arredondamento deve ser maior que zero")
    private Short multiploArredondamentoMm;

    @DecimalMin(value = "0.0000", message = "Área mínima não pode ser negativa")
    private BigDecimal areaMinimaM2;

    @DecimalMin(value = "0.00", message = "Percentual de perdas não pode ser negativo")
    private BigDecimal percentualPerdas;

    @DecimalMin(value = "0.00", message = "Percentual de impostos não pode ser negativo")
    private BigDecimal percentualImpostos;

    @DecimalMin(value = "0.00", message = "Percentual de comissão não pode ser negativo")
    private BigDecimal percentualComissao;

    @DecimalMin(value = "0.00", message = "Margem desejada não pode ser negativa")
    private BigDecimal percentualMargemDesejada;

    @DecimalMin(value = "0.00", message = "Margem mínima não pode ser negativa")
    private BigDecimal percentualMargemMinima;

    private Map<Integer, BigDecimal> taxaCartaoParcelas;

    @NotNull(message = "Arredondamento comercial é obrigatório")
    private ArredondamentoComercial arredondamentoComercial;

    private BigDecimal variacaoPreOrcamentoMinPct;
    private BigDecimal variacaoPreOrcamentoMaxPct;

    @DecimalMin(value = "0.00", message = "Valor da visita técnica não pode ser negativo")
    private BigDecimal valorVisitaTecnica;

    private RegraDeslocamento regraDeslocamento;

    @DecimalMin(value = "0.00", message = "Valor de deslocamento não pode ser negativo")
    private BigDecimal valorDeslocamento;

    @Min(value = 1, message = "Validade padrão deve ser de pelo menos 1 dia")
    private Integer validadePadraoDias;

    @Min(value = 1, message = "Tamanho máximo de chapa (largura) deve ser maior que zero")
    private Integer tamanhoMaximoChapaLarguraMm;

    @Min(value = 1, message = "Tamanho máximo de chapa (altura) deve ser maior que zero")
    private Integer tamanhoMaximoChapaAlturaMm;

    @Min(value = 0, message = "Tolerância de prumo/nível não pode ser negativa")
    private Short toleranciaPrumoNivelMm;

    private RegimeTributario regimeTributario;

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
