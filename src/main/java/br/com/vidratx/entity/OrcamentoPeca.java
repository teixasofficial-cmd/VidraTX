package br.com.vidratx.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "orcamento_peca")
public class OrcamentoPeca {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "orcamento_item_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_orcamento_peca_item")
    )
    private OrcamentoItem orcamentoItem;

    @Column(name = "descricao", nullable = false, length = 100)
    private String descricao;

    @Column(name = "largura_corte_mm", nullable = false)
    private Integer larguraCorteMm;

    @Column(name = "altura_corte_mm", nullable = false)
    private Integer alturaCorteMm;

    @Column(name = "quantidade", nullable = false)
    private Integer quantidade = 1;

    @Column(name = "excede_tamanho_maximo", nullable = false)
    private Boolean excedeTamanhoMaximo = false;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    public OrcamentoPeca() {
    }

    @PrePersist
    protected void aoCriar() {

        if (criadoEm == null) {
            criadoEm = LocalDateTime.now();
        }

        if (quantidade == null) {
            quantidade = 1;
        }

        if (excedeTamanhoMaximo == null) {
            excedeTamanhoMaximo = false;
        }
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

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Integer getLarguraCorteMm() {
        return larguraCorteMm;
    }

    public void setLarguraCorteMm(Integer larguraCorteMm) {
        this.larguraCorteMm = larguraCorteMm;
    }

    public Integer getAlturaCorteMm() {
        return alturaCorteMm;
    }

    public void setAlturaCorteMm(Integer alturaCorteMm) {
        this.alturaCorteMm = alturaCorteMm;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public Boolean getExcedeTamanhoMaximo() {
        return excedeTamanhoMaximo;
    }

    public void setExcedeTamanhoMaximo(Boolean excedeTamanhoMaximo) {
        this.excedeTamanhoMaximo = excedeTamanhoMaximo;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }
}
