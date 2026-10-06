package br.com.vidratx.dto;

import br.com.vidratx.enums.AcabamentoVidro;
import br.com.vidratx.enums.CategoriaItemPreco;
import br.com.vidratx.enums.CorVidro;
import br.com.vidratx.enums.OrigemPreco;
import br.com.vidratx.enums.TipoVidro;
import br.com.vidratx.enums.UnidadeMedida;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public class TabelaPrecoRequest {

    @NotNull(message = "Categoria é obrigatória")
    private CategoriaItemPreco categoria;

    @Size(min = 1, max = 200, message = "Descrição deve ter entre 1 e 200 caracteres")
    private String descricao;

    private TipoVidro tipoVidro;
    private Short espessuraMm;
    private CorVidro cor;
    private AcabamentoVidro acabamento;

    @NotNull(message = "Unidade é obrigatória")
    private UnidadeMedida unidade;

    @DecimalMin(value = "0.00", message = "Custo não pode ser negativo")
    private BigDecimal custo;

    @NotNull(message = "Preço de venda é obrigatório")
    @DecimalMin(value = "0.00", message = "Preço de venda não pode ser negativo")
    private BigDecimal precoVenda;

    @Size(max = 150, message = "Fornecedor deve ter no máximo 150 caracteres")
    private String fornecedor;

    private Boolean ativo;

    private OrigemPreco origem;

    @Size(max = 255, message = "Fórmula de origem deve ter no máximo 255 caracteres")
    private String formulaOrigem;

    public CategoriaItemPreco getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaItemPreco categoria) {
        this.categoria = categoria;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
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

    public UnidadeMedida getUnidade() {
        return unidade;
    }

    public void setUnidade(UnidadeMedida unidade) {
        this.unidade = unidade;
    }

    public BigDecimal getCusto() {
        return custo;
    }

    public void setCusto(BigDecimal custo) {
        this.custo = custo;
    }

    public BigDecimal getPrecoVenda() {
        return precoVenda;
    }

    public void setPrecoVenda(BigDecimal precoVenda) {
        this.precoVenda = precoVenda;
    }

    public String getFornecedor() {
        return fornecedor;
    }

    public void setFornecedor(String fornecedor) {
        this.fornecedor = fornecedor;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public OrigemPreco getOrigem() {
        return origem;
    }

    public void setOrigem(OrigemPreco origem) {
        this.origem = origem;
    }

    public String getFormulaOrigem() {
        return formulaOrigem;
    }

    public void setFormulaOrigem(String formulaOrigem) {
        this.formulaOrigem = formulaOrigem;
    }
}
