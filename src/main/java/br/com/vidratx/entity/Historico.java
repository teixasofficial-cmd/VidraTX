package br.com.vidratx.entity;

import br.com.vidratx.enums.TipoEventoHistorico;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "historico")
public class Historico {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "empresa_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_historico_empresa")
    )
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "cliente_id",
            foreignKey = @ForeignKey(name = "fk_historico_cliente")
    )
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "atendimento_id",
            foreignKey = @ForeignKey(name = "fk_historico_atendimento")
    )
    private AtendimentoWhatsapp atendimento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "orcamento_id",
            foreignKey = @ForeignKey(name = "fk_historico_orcamento")
    )
    private Orcamento orcamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "instalacao_id",
            foreignKey = @ForeignKey(name = "fk_historico_instalacao")
    )
    private Instalacao instalacao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "usuario_id",
            foreignKey = @ForeignKey(name = "fk_historico_usuario")
    )
    private Usuario usuario;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 40)
    private TipoEventoHistorico tipo;

    @Column(name = "descricao", nullable = false, columnDefinition = "TEXT")
    private String descricao;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    public Historico() {
    }

    @PrePersist
    protected void aoCriar() {

        if (criadoEm == null) {
            criadoEm = LocalDateTime.now();
        }
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

    public AtendimentoWhatsapp getAtendimento() {
        return atendimento;
    }

    public void setAtendimento(AtendimentoWhatsapp atendimento) {
        this.atendimento = atendimento;
    }

    public Orcamento getOrcamento() {
        return orcamento;
    }

    public void setOrcamento(Orcamento orcamento) {
        this.orcamento = orcamento;
    }

    public Instalacao getInstalacao() {
        return instalacao;
    }

    public void setInstalacao(Instalacao instalacao) {
        this.instalacao = instalacao;
    }

    public Usuario getUsuario() {
        return usuario;
    }

    public void setUsuario(Usuario usuario) {
        this.usuario = usuario;
    }

    public TipoEventoHistorico getTipo() {
        return tipo;
    }

    public void setTipo(TipoEventoHistorico tipo) {
        this.tipo = tipo;
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
}
