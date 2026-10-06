package br.com.vidratx.dto;

import br.com.vidratx.enums.AcabamentoVidro;
import br.com.vidratx.enums.CorVidro;
import br.com.vidratx.enums.TipoVidro;

import java.math.BigDecimal;
import java.util.List;

public class OrcamentoItemResponse {

    private Long id;
    private Long orcamentoId;
    private Long tipologiaId;
    private String tipologiaNome;
    private String ambiente;
    private Integer larguraVaoMm;
    private Integer alturaVaoMm;
    private Integer larguraVao2Mm;
    private Integer alturaVao2Mm;
    private String medidaTextoOriginal;
    private Boolean medidaAproximada;
    private TipoVidro tipoVidro;
    private Short espessuraMm;
    private CorVidro cor;
    private AcabamentoVidro acabamento;
    private String corFerragem;
    private Integer quantidade;
    private String observacoes;
    private Integer ordem;
    private BigDecimal custoItem;
    private List<OrcamentoPecaResponse> pecas;
    private List<OrcamentoLinhaResponse> linhas;
    private List<AlertaCalculoResponse> alertas;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrcamentoId() {
        return orcamentoId;
    }

    public void setOrcamentoId(Long orcamentoId) {
        this.orcamentoId = orcamentoId;
    }

    public Long getTipologiaId() {
        return tipologiaId;
    }

    public void setTipologiaId(Long tipologiaId) {
        this.tipologiaId = tipologiaId;
    }

    public String getTipologiaNome() {
        return tipologiaNome;
    }

    public void setTipologiaNome(String tipologiaNome) {
        this.tipologiaNome = tipologiaNome;
    }

    public String getAmbiente() {
        return ambiente;
    }

    public void setAmbiente(String ambiente) {
        this.ambiente = ambiente;
    }

    public Integer getLarguraVaoMm() {
        return larguraVaoMm;
    }

    public void setLarguraVaoMm(Integer larguraVaoMm) {
        this.larguraVaoMm = larguraVaoMm;
    }

    public Integer getAlturaVaoMm() {
        return alturaVaoMm;
    }

    public void setAlturaVaoMm(Integer alturaVaoMm) {
        this.alturaVaoMm = alturaVaoMm;
    }

    public Integer getLarguraVao2Mm() {
        return larguraVao2Mm;
    }

    public void setLarguraVao2Mm(Integer larguraVao2Mm) {
        this.larguraVao2Mm = larguraVao2Mm;
    }

    public Integer getAlturaVao2Mm() {
        return alturaVao2Mm;
    }

    public void setAlturaVao2Mm(Integer alturaVao2Mm) {
        this.alturaVao2Mm = alturaVao2Mm;
    }

    public String getMedidaTextoOriginal() {
        return medidaTextoOriginal;
    }

    public void setMedidaTextoOriginal(String medidaTextoOriginal) {
        this.medidaTextoOriginal = medidaTextoOriginal;
    }

    public Boolean getMedidaAproximada() {
        return medidaAproximada;
    }

    public void setMedidaAproximada(Boolean medidaAproximada) {
        this.medidaAproximada = medidaAproximada;
    }

    public TipoVidro getTipoVidro() {
        return tipoVidro;
    }

    public void setTipoVidro(TipoVidro tipoVidro) {
        this.tipoVidro = tipoVidro;
    }

    public Short getEspessuraMm() {
        return espessuraMm;
    }

    public void setEspessuraMm(Short espessuraMm) {
        this.espessuraMm = espessuraMm;
    }

    public CorVidro getCor() {
        return cor;
    }

    public void setCor(CorVidro cor) {
        this.cor = cor;
    }

    public AcabamentoVidro getAcabamento() {
        return acabamento;
    }

    public void setAcabamento(AcabamentoVidro acabamento) {
        this.acabamento = acabamento;
    }

    public String getCorFerragem() {
        return corFerragem;
    }

    public void setCorFerragem(String corFerragem) {
        this.corFerragem = corFerragem;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public Integer getOrdem() {
        return ordem;
    }

    public void setOrdem(Integer ordem) {
        this.ordem = ordem;
    }

    public BigDecimal getCustoItem() {
        return custoItem;
    }

    public void setCustoItem(BigDecimal custoItem) {
        this.custoItem = custoItem;
    }

    public List<OrcamentoPecaResponse> getPecas() {
        return pecas;
    }

    public void setPecas(List<OrcamentoPecaResponse> pecas) {
        this.pecas = pecas;
    }

    public List<OrcamentoLinhaResponse> getLinhas() {
        return linhas;
    }

    public void setLinhas(List<OrcamentoLinhaResponse> linhas) {
        this.linhas = linhas;
    }

    public List<AlertaCalculoResponse> getAlertas() {
        return alertas;
    }

    public void setAlertas(List<AlertaCalculoResponse> alertas) {
        this.alertas = alertas;
    }
}
