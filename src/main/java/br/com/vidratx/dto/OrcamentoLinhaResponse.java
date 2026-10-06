package br.com.vidratx.dto;

import br.com.vidratx.enums.OrigemSugestao;
import br.com.vidratx.enums.TipoComponenteCusto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class OrcamentoLinhaResponse {

    private Long id;
    private TipoComponenteCusto tipo;
    private String descricao;
    private String componenteDescricao;
    private BigDecimal componenteQuantidade;
    private BigDecimal quantidade;
    private Long tabelaPrecoId;
    private BigDecimal valorUnitario;
    private BigDecimal valorSugerido;
    private BigDecimal valorFinal;
    private BigDecimal valorExibido;
    private OrigemSugestao origemSugestao;
    private Boolean editado;
    private String editadoPorNome;
    private LocalDateTime editadoEm;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TipoComponenteCusto getTipo() {
        return tipo;
    }

    public void setTipo(TipoComponenteCusto tipo) {
        this.tipo = tipo;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public BigDecimal getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(BigDecimal quantidade) {
        this.quantidade = quantidade;
    }

    public Long getTabelaPrecoId() {
        return tabelaPrecoId;
    }

    public void setTabelaPrecoId(Long tabelaPrecoId) {
        this.tabelaPrecoId = tabelaPrecoId;
    }

    public BigDecimal getValorUnitario() {
        return valorUnitario;
    }

    public void setValorUnitario(BigDecimal valorUnitario) {
        this.valorUnitario = valorUnitario;
    }

    public BigDecimal getValorSugerido() {
        return valorSugerido;
    }

    public void setValorSugerido(BigDecimal valorSugerido) {
        this.valorSugerido = valorSugerido;
    }

    public BigDecimal getValorFinal() {
        return valorFinal;
    }

    public void setValorFinal(BigDecimal valorFinal) {
        this.valorFinal = valorFinal;
    }

    public BigDecimal getValorExibido() {
        return valorExibido;
    }

    public void setValorExibido(BigDecimal valorExibido) {
        this.valorExibido = valorExibido;
    }

    public OrigemSugestao getOrigemSugestao() {
        return origemSugestao;
    }

    public void setOrigemSugestao(OrigemSugestao origemSugestao) {
        this.origemSugestao = origemSugestao;
    }

    public Boolean getEditado() {
        return editado;
    }

    public void setEditado(Boolean editado) {
        this.editado = editado;
    }

    public String getEditadoPorNome() {
        return editadoPorNome;
    }

    public void setEditadoPorNome(String editadoPorNome) {
        this.editadoPorNome = editadoPorNome;
    }

    public LocalDateTime getEditadoEm() {
        return editadoEm;
    }

    public void setEditadoEm(LocalDateTime editadoEm) {
        this.editadoEm = editadoEm;
    }

    public String getComponenteDescricao() {
        return componenteDescricao;
    }

    public void setComponenteDescricao(String componenteDescricao) {
        this.componenteDescricao = componenteDescricao;
    }

    public BigDecimal getComponenteQuantidade() {
        return componenteQuantidade;
    }

    public void setComponenteQuantidade(BigDecimal componenteQuantidade) {
        this.componenteQuantidade = componenteQuantidade;
    }
}
