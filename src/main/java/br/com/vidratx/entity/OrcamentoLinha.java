package br.com.vidratx.entity;

import br.com.vidratx.enums.OrigemSugestao;
import br.com.vidratx.enums.TipoComponenteCusto;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "orcamento_linha")
public class OrcamentoLinha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "orcamento_item_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_orcamento_linha_item")
    )
    private OrcamentoItem orcamentoItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "tabela_preco_id",
            foreignKey = @ForeignKey(name = "fk_orcamento_linha_tabela_preco")
    )
    private TabelaPreco tabelaPreco;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_componente", nullable = false, length = 20)
    private TipoComponenteCusto tipoComponente;

    @Column(name = "descricao", nullable = false, length = 200)
    private String descricao;

    @Column(name = "quantidade", nullable = false, precision = 10, scale = 3)
    private BigDecimal quantidade = BigDecimal.ONE;

    @Column(name = "componente_descricao", length = 200)
    private String componenteDescricao;

    @Column(name = "componente_quantidade", precision = 10, scale = 3)
    private BigDecimal componenteQuantidade;

    @Column(name = "valor_unitario_centavos")
    private Long valorUnitarioCentavos;

    @Column(name = "valor_sugerido_centavos", nullable = false)
    private Long valorSugeridoCentavos;

    @Column(name = "valor_final_centavos")
    private Long valorFinalCentavos;

    @Enumerated(EnumType.STRING)
    @Column(name = "origem_sugestao", nullable = false, length = 20)
    private OrigemSugestao origemSugestao;

    @Column(name = "editado", nullable = false)
    private Boolean editado = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "editado_por_id",
            foreignKey = @ForeignKey(name = "fk_orcamento_linha_editor")
    )
    private Usuario editadoPor;

    @Column(name = "editado_em")
    private LocalDateTime editadoEm;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    public OrcamentoLinha() {
    }

    @PrePersist
    protected void aoCriar() {

        LocalDateTime agora = LocalDateTime.now();

        criadoEm = agora;
        atualizadoEm = agora;

        if (editado == null) {
            editado = false;
        }

        if (quantidade == null) {
            quantidade = BigDecimal.ONE;
        }
    }

    @PreUpdate
    protected void aoAtualizar() {
        atualizadoEm = LocalDateTime.now();
    }

    public Long getValorExibidoCentavos() {
        return valorFinalCentavos != null ? valorFinalCentavos : valorSugeridoCentavos;
    }

    public Long getId() {
        return id;
    }

    public OrcamentoItem getOrcamentoItem() {
        return orcamentoItem;
    }

    public void setOrcamentoItem(OrcamentoItem orcamentoItem) {
        this.orcamentoItem = orcamentoItem;
    }

    public TabelaPreco getTabelaPreco() {
        return tabelaPreco;
    }

    public void setTabelaPreco(TabelaPreco tabelaPreco) {
        this.tabelaPreco = tabelaPreco;
    }

    public TipoComponenteCusto getTipoComponente() {
        return tipoComponente;
    }

    public void setTipoComponente(TipoComponenteCusto tipoComponente) {
        this.tipoComponente = tipoComponente;
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

    public Long getValorUnitarioCentavos() {
        return valorUnitarioCentavos;
    }

    public void setValorUnitarioCentavos(Long valorUnitarioCentavos) {
        this.valorUnitarioCentavos = valorUnitarioCentavos;
    }

    public Long getValorSugeridoCentavos() {
        return valorSugeridoCentavos;
    }

    public void setValorSugeridoCentavos(Long valorSugeridoCentavos) {
        this.valorSugeridoCentavos = valorSugeridoCentavos;
    }

    public Long getValorFinalCentavos() {
        return valorFinalCentavos;
    }

    public void setValorFinalCentavos(Long valorFinalCentavos) {
        this.valorFinalCentavos = valorFinalCentavos;
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

    public Usuario getEditadoPor() {
        return editadoPor;
    }

    public void setEditadoPor(Usuario editadoPor) {
        this.editadoPor = editadoPor;
    }

    public LocalDateTime getEditadoEm() {
        return editadoEm;
    }

    public void setEditadoEm(LocalDateTime editadoEm) {
        this.editadoEm = editadoEm;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
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
