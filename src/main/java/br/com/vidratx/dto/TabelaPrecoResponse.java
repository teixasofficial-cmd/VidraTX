package br.com.vidratx.dto;

import br.com.vidratx.enums.AcabamentoVidro;
import br.com.vidratx.enums.CategoriaItemPreco;
import br.com.vidratx.enums.CorVidro;
import br.com.vidratx.enums.OrigemPreco;
import br.com.vidratx.enums.TipoVidro;
import br.com.vidratx.enums.UnidadeMedida;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class TabelaPrecoResponse {

    private Long id;
    private Long empresaId;
    private CategoriaItemPreco categoria;
    private String descricao;
    private TipoVidro tipoVidro;
    private Short espessuraMm;
    private CorVidro cor;
    private AcabamentoVidro acabamento;
    private UnidadeMedida unidade;
    private BigDecimal custo;
    private BigDecimal precoVenda;

    private boolean precoAbaixoDoCusto;

    private String fornecedor;
    private OrigemPreco origem;
    private String formulaOrigem;
    private Boolean ativo;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

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

    public boolean isPrecoAbaixoDoCusto() {
        return precoAbaixoDoCusto;
    }

    public void setPrecoAbaixoDoCusto(boolean precoAbaixoDoCusto) {
        this.precoAbaixoDoCusto = precoAbaixoDoCusto;
    }

    public String getFornecedor() {
        return fornecedor;
    }

    public void setFornecedor(String fornecedor) {
        this.fornecedor = fornecedor;
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

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(LocalDateTime atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }
}
