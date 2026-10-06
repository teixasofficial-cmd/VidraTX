package br.com.vidratx.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class OrcamentoTotaisResponse {

    private BigDecimal custoTotal;
    private BigDecimal precoSugerido;

    private BigDecimal deslocamento;
    private BigDecimal ajusteComercial;
    private BigDecimal valorFinal;
    private BigDecimal margemReal;

    private BigDecimal custoRealTotal;
    private BigDecimal margemSobreCustoReal;
    private boolean custoRealIncompleto;

    private int valoresNaoRevisados;
    private List<AlertaCalculoResponse> alertas;

    private List<String> bloqueiosEnvio = new ArrayList<>();

    private BigDecimal precoFinalManual;

    private String modoPrecificacao;
    private Integer parcelasCartao;

    public BigDecimal getCustoTotal() {
        return custoTotal;
    }

    public void setCustoTotal(BigDecimal custoTotal) {
        this.custoTotal = custoTotal;
    }

    public BigDecimal getPrecoSugerido() {
        return precoSugerido;
    }

    public void setPrecoSugerido(BigDecimal precoSugerido) {
        this.precoSugerido = precoSugerido;
    }

    public BigDecimal getAjusteComercial() {
        return ajusteComercial;
    }

    public void setAjusteComercial(BigDecimal ajusteComercial) {
        this.ajusteComercial = ajusteComercial;
    }

    public BigDecimal getValorFinal() {
        return valorFinal;
    }

    public void setValorFinal(BigDecimal valorFinal) {
        this.valorFinal = valorFinal;
    }

    public BigDecimal getMargemReal() {
        return margemReal;
    }

    public void setMargemReal(BigDecimal margemReal) {
        this.margemReal = margemReal;
    }

    public BigDecimal getCustoRealTotal() {
        return custoRealTotal;
    }

    public void setCustoRealTotal(BigDecimal custoRealTotal) {
        this.custoRealTotal = custoRealTotal;
    }

    public BigDecimal getMargemSobreCustoReal() {
        return margemSobreCustoReal;
    }

    public void setMargemSobreCustoReal(BigDecimal margemSobreCustoReal) {
        this.margemSobreCustoReal = margemSobreCustoReal;
    }

    public boolean isCustoRealIncompleto() {
        return custoRealIncompleto;
    }

    public void setCustoRealIncompleto(boolean custoRealIncompleto) {
        this.custoRealIncompleto = custoRealIncompleto;
    }

    public int getValoresNaoRevisados() {
        return valoresNaoRevisados;
    }

    public void setValoresNaoRevisados(int valoresNaoRevisados) {
        this.valoresNaoRevisados = valoresNaoRevisados;
    }

    public List<AlertaCalculoResponse> getAlertas() {
        return alertas;
    }

    public void setAlertas(List<AlertaCalculoResponse> alertas) {
        this.alertas = alertas;
    }

    public List<String> getBloqueiosEnvio() {
        return bloqueiosEnvio;
    }

    public void setBloqueiosEnvio(List<String> bloqueiosEnvio) {
        this.bloqueiosEnvio = bloqueiosEnvio;
    }

    public BigDecimal getPrecoFinalManual() {
        return precoFinalManual;
    }

    public void setPrecoFinalManual(BigDecimal precoFinalManual) {
        this.precoFinalManual = precoFinalManual;
    }

    public String getModoPrecificacao() {
        return modoPrecificacao;
    }

    public void setModoPrecificacao(String modoPrecificacao) {
        this.modoPrecificacao = modoPrecificacao;
    }

    public Integer getParcelasCartao() {
        return parcelasCartao;
    }

    public void setParcelasCartao(Integer parcelasCartao) {
        this.parcelasCartao = parcelasCartao;
    }

    public BigDecimal getDeslocamento() {
        return deslocamento;
    }

    public void setDeslocamento(BigDecimal deslocamento) {
        this.deslocamento = deslocamento;
    }
}
