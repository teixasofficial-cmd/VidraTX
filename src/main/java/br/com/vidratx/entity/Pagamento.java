package br.com.vidratx.entity;

import br.com.vidratx.enums.FormaPagamento;
import br.com.vidratx.enums.SituacaoPagamento;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "pagamento")
public class Pagamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "orcamento_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_pagamento_orcamento"
            )
    )
    private Orcamento orcamento;

    @Column(
            name = "valor",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal valor;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "forma_pagamento",
            nullable = false,
            length = 20
    )
    private FormaPagamento formaPagamento;

    @Column(name = "vencimento")
    private LocalDate vencimento;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "situacao",
            nullable = false,
            length = 20
    )
    private SituacaoPagamento situacao = SituacaoPagamento.PENDENTE;

    @Column(name = "pago_em")
    private LocalDate pagoEm;

    @Column(
            name = "observacao",
            length = 500
    )
    private String observacao;

    @Column(
            name = "criado_em",
            nullable = false,
            updatable = false
    )
    private LocalDateTime criadoEm;

    @Column(
            name = "atualizado_em",
            nullable = false
    )
    private LocalDateTime atualizadoEm;

    public Pagamento() {
    }

    @PrePersist
    protected void aoCriar() {

        LocalDateTime agora =
                LocalDateTime.now();

        criadoEm = agora;
        atualizadoEm = agora;

        if (situacao == null) {
            situacao = SituacaoPagamento.PENDENTE;
        }
    }

    @PreUpdate
    protected void aoAtualizar() {
        atualizadoEm = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Orcamento getOrcamento() {
        return orcamento;
    }

    public void setOrcamento(Orcamento orcamento) {
        this.orcamento = orcamento;
    }

    public BigDecimal getValor() {
        return valor;
    }

    public void setValor(BigDecimal valor) {
        this.valor = valor;
    }

    public FormaPagamento getFormaPagamento() {
        return formaPagamento;
    }

    public void setFormaPagamento(FormaPagamento formaPagamento) {
        this.formaPagamento = formaPagamento;
    }

    public LocalDate getVencimento() {
        return vencimento;
    }

    public void setVencimento(LocalDate vencimento) {
        this.vencimento = vencimento;
    }

    public SituacaoPagamento getSituacao() {
        return situacao;
    }

    public void setSituacao(SituacaoPagamento situacao) {
        this.situacao = situacao;
    }

    public LocalDate getPagoEm() {
        return pagoEm;
    }

    public void setPagoEm(LocalDate pagoEm) {
        this.pagoEm = pagoEm;
    }

    public String getObservacao() {
        return observacao;
    }

    public void setObservacao(String observacao) {
        this.observacao = observacao;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }
}
