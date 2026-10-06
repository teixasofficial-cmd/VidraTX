package br.com.vidratx.entity;

import br.com.vidratx.enums.AcabamentoVidro;
import br.com.vidratx.enums.CategoriaItemPreco;
import br.com.vidratx.enums.CorVidro;
import br.com.vidratx.enums.OrigemPreco;
import br.com.vidratx.enums.TipoVidro;
import br.com.vidratx.enums.UnidadeMedida;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "tabela_preco")
public class TabelaPreco {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "empresa_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_tabela_preco_empresa")
    )
    private Empresa empresa;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", nullable = false, length = 20)
    private CategoriaItemPreco categoria;

    @Column(name = "descricao", nullable = false, length = 200)
    private String descricao;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_vidro", length = 20)
    private TipoVidro tipoVidro;

    @Column(name = "espessura_mm")
    private Short espessuraMm;

    @Enumerated(EnumType.STRING)
    @Column(name = "cor", length = 20)
    private CorVidro cor;

    @Enumerated(EnumType.STRING)
    @Column(name = "acabamento", length = 20)
    private AcabamentoVidro acabamento;

    @Enumerated(EnumType.STRING)
    @Column(name = "unidade", nullable = false, length = 10)
    private UnidadeMedida unidade;

    @Column(name = "custo_centavos")
    private Long custoCentavos;

    @Column(name = "preco_venda_centavos", nullable = false)
    private Long precoVendaCentavos;

    @Column(name = "fornecedor", length = 150)
    private String fornecedor;

    @Enumerated(EnumType.STRING)
    @Column(name = "origem", nullable = false, length = 20)
    private OrigemPreco origem = OrigemPreco.MANUAL;

    @Column(name = "formula_origem", length = 255)
    private String formulaOrigem;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    public TabelaPreco() {
    }

    @PrePersist
    protected void aoCriar() {

        LocalDateTime agora = LocalDateTime.now();

        criadoEm = agora;
        atualizadoEm = agora;

        if (ativo == null) {
            ativo = true;
        }

        if (origem == null) {
            origem = OrigemPreco.MANUAL;
        }
    }

    @PreUpdate
    protected void aoAtualizar() {
        atualizadoEm = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Empresa getEmpresa() {
        return empresa;
    }

    public void setEmpresa(Empresa empresa) {
        this.empresa = empresa;
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

    public Long getCustoCentavos() {
        return custoCentavos;
    }

    public void setCustoCentavos(Long custoCentavos) {
        this.custoCentavos = custoCentavos;
    }

    public Long getPrecoVendaCentavos() {
        return precoVendaCentavos;
    }

    public void setPrecoVendaCentavos(Long precoVendaCentavos) {
        this.precoVendaCentavos = precoVendaCentavos;
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

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }
}
