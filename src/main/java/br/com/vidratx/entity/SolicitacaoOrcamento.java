package br.com.vidratx.entity;

import br.com.vidratx.enums.CanalSolicitacao;
import br.com.vidratx.enums.StatusSolicitacaoOrcamento;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "solicitacao_orcamento")
public class SolicitacaoOrcamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "empresa_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_solicitacao_orcamento_empresa"
            )
    )
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "cliente_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_solicitacao_orcamento_cliente"
            )
    )
    private Cliente cliente;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "canal",
            nullable = false,
            length = 20
    )
    private CanalSolicitacao canal;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private StatusSolicitacaoOrcamento status = StatusSolicitacaoOrcamento.RECEBIDA;

    @Column(
            name = "descricao",
            columnDefinition = "TEXT"
    )
    private String descricao;

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

    public SolicitacaoOrcamento() {
    }

    @PrePersist
    protected void aoCriar() {

        LocalDateTime agora =
                LocalDateTime.now();

        criadoEm = agora;
        atualizadoEm = agora;

        if (status == null) {
            status = StatusSolicitacaoOrcamento.RECEBIDA;
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

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public CanalSolicitacao getCanal() {
        return canal;
    }

    public void setCanal(CanalSolicitacao canal) {
        this.canal = canal;
    }

    public StatusSolicitacaoOrcamento getStatus() {
        return status;
    }

    public void setStatus(StatusSolicitacaoOrcamento status) {
        this.status = status;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }
}
