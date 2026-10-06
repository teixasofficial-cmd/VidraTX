package br.com.vidratx.entity;

import br.com.vidratx.enums.StatusAgendamento;
import br.com.vidratx.enums.TipoAgendamento;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "instalacao",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_instalacao_ordem_servico",
                        columnNames = "ordem_servico_id"
                )
        }
)
public class Instalacao implements Agendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "ordem_servico_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_instalacao_ordem_servico"
            )
    )
    private OrdemServico ordemServico;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private StatusAgendamento status = StatusAgendamento.PROPOSTA_ENVIADA;

    @Column(
            name = "data_agendada",
            nullable = false
    )
    private LocalDateTime dataAgendada;

    @Column(name = "data_realizada")
    private LocalDateTime dataRealizada;

    @Column(
            name = "contraproposta_texto",
            columnDefinition = "TEXT"
    )
    private String contrapropostaTexto;

    @Column(name = "cancelamento_solicitado_em")
    private LocalDateTime cancelamentoSolicitadoEm;

    @Column(name = "cancelamento_solicitado_texto", columnDefinition = "TEXT")
    private String cancelamentoSolicitadoTexto;

    @Column(
            name = "endereco",
            length = 255
    )
    private String endereco;

    @Column(
            name = "equipe_responsavel",
            length = 150
    )
    private String equipeResponsavel;

    @Column(
            name = "checklist",
            columnDefinition = "TEXT"
    )
    private String checklist;

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

    @Version
    @Column(name = "versao", nullable = false)
    private Long versao;

    @Column(name = "versao_proposta", nullable = false)
    private Integer versaoProposta = 0;

    public Instalacao() {
    }

    @PrePersist
    protected void aoCriar() {

        LocalDateTime agora =
                LocalDateTime.now();

        criadoEm = agora;
        atualizadoEm = agora;

        if (status == null) {
            status = StatusAgendamento.PROPOSTA_ENVIADA;
        }
    }

    @PreUpdate
    protected void aoAtualizar() {
        atualizadoEm = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public OrdemServico getOrdemServico() {
        return ordemServico;
    }

    public void setOrdemServico(OrdemServico ordemServico) {
        this.ordemServico = ordemServico;
    }

    public StatusAgendamento getStatus() {
        return status;
    }

    public void setStatus(StatusAgendamento status) {
        this.status = status;
    }

    public LocalDateTime getDataAgendada() {
        return dataAgendada;
    }

    public void setDataAgendada(LocalDateTime dataAgendada) {
        this.dataAgendada = dataAgendada;
    }

    public LocalDateTime getDataRealizada() {
        return dataRealizada;
    }

    public void setDataRealizada(LocalDateTime dataRealizada) {
        this.dataRealizada = dataRealizada;
    }

    public String getContrapropostaTexto() {
        return contrapropostaTexto;
    }

    public void setContrapropostaTexto(String contrapropostaTexto) {
        this.contrapropostaTexto = contrapropostaTexto;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getEquipeResponsavel() {
        return equipeResponsavel;
    }

    public void setEquipeResponsavel(String equipeResponsavel) {
        this.equipeResponsavel = equipeResponsavel;
    }

    public String getChecklist() {
        return checklist;
    }

    public void setChecklist(String checklist) {
        this.checklist = checklist;
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

    public Long getVersao() {
        return versao;
    }

    @Override
    public Integer getVersaoProposta() {
        return versaoProposta;
    }

    @Override
    public void setVersaoProposta(Integer versaoProposta) {
        this.versaoProposta = versaoProposta;
    }

    @Override
    public TipoAgendamento getTipoAgendamento() {
        return TipoAgendamento.INSTALACAO;
    }

    @Override
    public Orcamento getOrcamentoReferencia() {
        return ordemServico == null ? null : ordemServico.getOrcamento();
    }

    @Override
    public LocalDateTime getCancelamentoSolicitadoEm() {
        return cancelamentoSolicitadoEm;
    }

    @Override
    public void setCancelamentoSolicitadoEm(LocalDateTime cancelamentoSolicitadoEm) {
        this.cancelamentoSolicitadoEm = cancelamentoSolicitadoEm;
    }

    @Override
    public String getCancelamentoSolicitadoTexto() {
        return cancelamentoSolicitadoTexto;
    }

    @Override
    public void setCancelamentoSolicitadoTexto(String cancelamentoSolicitadoTexto) {
        this.cancelamentoSolicitadoTexto = cancelamentoSolicitadoTexto;
    }
}
