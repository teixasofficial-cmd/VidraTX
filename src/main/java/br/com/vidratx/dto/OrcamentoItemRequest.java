package br.com.vidratx.dto;

import br.com.vidratx.enums.AcabamentoVidro;
import br.com.vidratx.enums.CorVidro;
import br.com.vidratx.enums.TipoVidro;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

import java.util.List;

public class OrcamentoItemRequest {

    @NotNull(message = "Tipologia é obrigatória")
    private Long tipologiaId;

    @Size(max = 100, message = "Ambiente deve ter no máximo 100 caracteres")
    private String ambiente;

    private Integer larguraVaoMm;
    private Integer alturaVaoMm;
    private Integer larguraVao2Mm;
    private Integer alturaVao2Mm;

    @Size(max = 100, message = "Medida deve ter no máximo 100 caracteres")
    private String medidaTexto;

    private TipoVidro tipoVidro;
    private Short espessuraMm;
    private CorVidro cor;
    private AcabamentoVidro acabamento;

    @Size(max = 30, message = "Cor da ferragem deve ter no máximo 30 caracteres")
    private String corFerragem;

    @Positive(message = "Quantidade deve ser maior que zero")
    private Integer quantidade;

    @Size(max = 2000, message = "Observações devem ter no máximo 2000 caracteres")
    private String observacoes;

    @Valid
    private List<ComponenteManualRequest> componentes;

    public Long getTipologiaId() {
        return tipologiaId;
    }

    public void setTipologiaId(Long tipologiaId) {
        this.tipologiaId = tipologiaId;
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

    public String getMedidaTexto() {
        return medidaTexto;
    }

    public void setMedidaTexto(String medidaTexto) {
        this.medidaTexto = medidaTexto;
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

    public List<ComponenteManualRequest> getComponentes() {
        return componentes;
    }

    public void setComponentes(List<ComponenteManualRequest> componentes) {
        this.componentes = componentes;
    }
}
