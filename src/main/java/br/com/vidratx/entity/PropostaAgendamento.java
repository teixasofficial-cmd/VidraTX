package br.com.vidratx.entity;

import br.com.vidratx.enums.OrigemProposta;
import br.com.vidratx.enums.StatusProposta;
import br.com.vidratx.enums.TipoAgendamento;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "proposta_agendamento")
public class PropostaAgendamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_proposta_agendamento_empresa"))
    private Empresa empresa;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoAgendamento tipo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "medicao_id",
            foreignKey = @ForeignKey(name = "fk_proposta_agendamento_medicao"))
    private Medicao medicao;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "instalacao_id",
            foreignKey = @ForeignKey(name = "fk_proposta_agendamento_instalacao"))
    private Instalacao instalacao;

    @Column(name = "versao", nullable = false)
    private Integer versao;

    @Enumerated(EnumType.STRING)
    @Column(name = "origem", nullable = false, length = 20)
    private OrigemProposta origem;

    @Column(name = "data_proposta")
    private LocalDateTime dataProposta;

    @Column(name = "texto_cliente", columnDefinition = "TEXT")
    private String textoCliente;

    @Column(name = "equipe", length = 150)
    private String equipe;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusProposta status = StatusProposta.PENDENTE;

    @Column(name = "motivo", columnDefinition = "TEXT")
    private String motivo;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "criado_por_id",
            foreignKey = @ForeignKey(name = "fk_proposta_agendamento_criado_por"))
    private Usuario criadoPor;

    @Column(name = "respondido_pelo_cliente", nullable = false)
    private Boolean respondidoPeloCliente = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "respondido_por_id",
            foreignKey = @ForeignKey(name = "fk_proposta_agendamento_respondido_por"))
    private Usuario respondidoPor;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mensagem_saida_id",
            foreignKey = @ForeignKey(name = "fk_proposta_agendamento_saida"))
    private MensagemSaida mensagemSaida;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "respondido_em")
    private LocalDateTime respondidoEm;

    public PropostaAgendamento() {
    }

    @PrePersist
    protected void aoCriar() {
        if (criadoEm == null) {
            criadoEm = LocalDateTime.now();
        }
    }

    public void vincular(Agendamento agendamento) {

        if (agendamento instanceof Medicao m) {
            this.medicao = m;
        } else if (agendamento instanceof Instalacao i) {
            this.instalacao = i;
        }

        this.tipo = agendamento.getTipoAgendamento();
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

    public TipoAgendamento getTipo() {
        return tipo;
    }

    public void setTipo(TipoAgendamento tipo) {
        this.tipo = tipo;
    }

    public Medicao getMedicao() {
        return medicao;
    }

    public Instalacao getInstalacao() {
        return instalacao;
    }

    public Integer getVersao() {
        return versao;
    }

    public void setVersao(Integer versao) {
        this.versao = versao;
    }

    public OrigemProposta getOrigem() {
        return origem;
    }

    public void setOrigem(OrigemProposta origem) {
        this.origem = origem;
    }

    public LocalDateTime getDataProposta() {
        return dataProposta;
    }

    public void setDataProposta(LocalDateTime dataProposta) {
        this.dataProposta = dataProposta;
    }

    public String getTextoCliente() {
        return textoCliente;
    }

    public void setTextoCliente(String textoCliente) {
        this.textoCliente = textoCliente;
    }

    public String getEquipe() {
        return equipe;
    }

    public void setEquipe(String equipe) {
        this.equipe = equipe;
    }

    public StatusProposta getStatus() {
        return status;
    }

    public void setStatus(StatusProposta status) {
        this.status = status;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public Usuario getCriadoPor() {
        return criadoPor;
    }

    public void setCriadoPor(Usuario criadoPor) {
        this.criadoPor = criadoPor;
    }

    public Boolean getRespondidoPeloCliente() {
        return respondidoPeloCliente;
    }

    public void setRespondidoPeloCliente(Boolean respondidoPeloCliente) {
        this.respondidoPeloCliente = respondidoPeloCliente;
    }

    public Usuario getRespondidoPor() {
        return respondidoPor;
    }

    public void setRespondidoPor(Usuario respondidoPor) {
        this.respondidoPor = respondidoPor;
    }

    public MensagemSaida getMensagemSaida() {
        return mensagemSaida;
    }

    public void setMensagemSaida(MensagemSaida mensagemSaida) {
        this.mensagemSaida = mensagemSaida;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public LocalDateTime getRespondidoEm() {
        return respondidoEm;
    }

    public void setRespondidoEm(LocalDateTime respondidoEm) {
        this.respondidoEm = respondidoEm;
    }
}
