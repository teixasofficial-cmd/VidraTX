package br.com.vidratx.entity;

import br.com.vidratx.enums.StatusProducao;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "ordem_servico",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_ordem_servico_orcamento",
                        columnNames = "orcamento_id"
                )
        }
)
public class OrdemServico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "empresa_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_ordem_servico_empresa"
            )
    )
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "orcamento_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_ordem_servico_orcamento"
            )
    )
    private Orcamento orcamento;

    @Column(
            name = "necessita_producao",
            nullable = false
    )
    private Boolean necessitaProducao = true;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status_producao",
            length = 30
    )
    private StatusProducao statusProducao;

    @Column(
            name = "observacoes",
            columnDefinition = "TEXT"
    )
    private String observacoes;

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

    @Column(name = "producao_iniciada_em")
    private LocalDateTime producaoIniciadaEm;

    @Column(name = "producao_concluida_em")
    private LocalDateTime producaoConcluidaEm;

    @Column(name = "producao_conferida_em")
    private LocalDateTime producaoConferidaEm;

    public OrdemServico() {
    }

    @PrePersist
    protected void aoCriar() {

        LocalDateTime agora =
                LocalDateTime.now();

        criadoEm = agora;
        atualizadoEm = agora;

        if (necessitaProducao == null) {
            necessitaProducao = true;
        }

        if (Boolean.TRUE.equals(necessitaProducao)
                && statusProducao == null) {

            statusProducao = StatusProducao.AGUARDANDO_PRODUCAO;
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

    public Orcamento getOrcamento() {
        return orcamento;
    }

    public void setOrcamento(Orcamento orcamento) {
        this.orcamento = orcamento;
    }

    public Boolean getNecessitaProducao() {
        return necessitaProducao;
    }

    public void setNecessitaProducao(Boolean necessitaProducao) {
        this.necessitaProducao = necessitaProducao;
    }

    public StatusProducao getStatusProducao() {
        return statusProducao;
    }

    public void setStatusProducao(StatusProducao statusProducao) {
        this.statusProducao = statusProducao;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public LocalDateTime getProducaoIniciadaEm() {
        return producaoIniciadaEm;
    }

    public void setProducaoIniciadaEm(LocalDateTime producaoIniciadaEm) {
        this.producaoIniciadaEm = producaoIniciadaEm;
    }

    public LocalDateTime getProducaoConcluidaEm() {
        return producaoConcluidaEm;
    }

    public void setProducaoConcluidaEm(LocalDateTime producaoConcluidaEm) {
        this.producaoConcluidaEm = producaoConcluidaEm;
    }

    public LocalDateTime getProducaoConferidaEm() {
        return producaoConferidaEm;
    }

    public void setProducaoConferidaEm(LocalDateTime producaoConferidaEm) {
        this.producaoConferidaEm = producaoConferidaEm;
    }
}
