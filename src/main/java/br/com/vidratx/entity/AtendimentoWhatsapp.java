package br.com.vidratx.entity;

import br.com.vidratx.enums.EtapaFluxo;
import br.com.vidratx.enums.StatusAtendimento;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "atendimento_whatsapp")
public class AtendimentoWhatsapp {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "empresa_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_atendimento_whatsapp_empresa"
            )
    )
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "cliente_id",
            foreignKey = @ForeignKey(
                    name = "fk_atendimento_whatsapp_cliente"
            )
    )
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "solicitacao_orcamento_id",
            foreignKey = @ForeignKey(
                    name = "fk_atendimento_whatsapp_solicitacao"
            )
    )
    private SolicitacaoOrcamento solicitacaoOrcamento;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "atendente_id",
            foreignKey = @ForeignKey(
                    name = "fk_atendimento_whatsapp_atendente"
            )
    )
    private Usuario atendente;

    @Column(
            name = "telefone",
            nullable = false,
            length = 20
    )
    private String telefone;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 30
    )
    private StatusAtendimento status = StatusAtendimento.EM_FLUXO_BOT;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "etapa_fluxo",
            length = 30
    )
    private EtapaFluxo etapaFluxo;

    @Column(
            name = "tentativas_erro",
            nullable = false
    )
    private Integer tentativasErro = 0;

    @Column(
            name = "dados_coletados",
            columnDefinition = "TEXT"
    )
    private String dadosColetados;

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

    @Column(name = "encerrado_em")
    private LocalDateTime encerradoEm;

    @Version
    @Column(name = "versao", nullable = false)
    private Long versao;

    @Column(name = "ultima_mensagem_cliente_em")
    private LocalDateTime ultimaMensagemClienteEm;

    @Column(name = "ultima_mensagem_empresa_em")
    private LocalDateTime ultimaMensagemEmpresaEm;

    @Column(name = "motivo_encerramento", length = 40)
    private String motivoEncerramento;

    @Column(name = "cliente_aguardando_desde")
    private LocalDateTime clienteAguardandoDesde;

    @Column(name = "aviso_espera_enviado_em")
    private LocalDateTime avisoEsperaEnviadoEm;

    public AtendimentoWhatsapp() {
    }

    @PrePersist
    protected void aoCriar() {

        LocalDateTime agora =
                LocalDateTime.now();

        criadoEm = agora;
        atualizadoEm = agora;

        if (status == null) {
            status = StatusAtendimento.EM_FLUXO_BOT;
        }

        if (tentativasErro == null) {
            tentativasErro = 0;
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

    public SolicitacaoOrcamento getSolicitacaoOrcamento() {
        return solicitacaoOrcamento;
    }

    public void setSolicitacaoOrcamento(SolicitacaoOrcamento solicitacaoOrcamento) {
        this.solicitacaoOrcamento = solicitacaoOrcamento;
    }

    public Usuario getAtendente() {
        return atendente;
    }

    public void setAtendente(Usuario atendente) {
        this.atendente = atendente;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public StatusAtendimento getStatus() {
        return status;
    }

    public void setStatus(StatusAtendimento status) {
        this.status = status;
    }

    public EtapaFluxo getEtapaFluxo() {
        return etapaFluxo;
    }

    public void setEtapaFluxo(EtapaFluxo etapaFluxo) {
        this.etapaFluxo = etapaFluxo;
    }

    public Integer getTentativasErro() {
        return tentativasErro;
    }

    public void setTentativasErro(Integer tentativasErro) {
        this.tentativasErro = tentativasErro;
    }

    public String getDadosColetados() {
        return dadosColetados;
    }

    public void setDadosColetados(String dadosColetados) {
        this.dadosColetados = dadosColetados;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public LocalDateTime getEncerradoEm() {
        return encerradoEm;
    }

    public void setEncerradoEm(LocalDateTime encerradoEm) {
        this.encerradoEm = encerradoEm;
    }

    public boolean clienteAguardandoResposta() {

        return ultimaMensagemClienteEm != null
                && (ultimaMensagemEmpresaEm == null || ultimaMensagemClienteEm.isAfter(ultimaMensagemEmpresaEm));
    }

    public boolean comPessoa() {
        return status == StatusAtendimento.AGUARDANDO_ATENDENTE || status == StatusAtendimento.EM_ATENDIMENTO_HUMANO;
    }

    public long minutosEsperandoResposta(LocalDateTime agora) {

        if (!comPessoa() || !clienteAguardandoResposta()) {
            return 0;
        }

        return Math.max(0, java.time.Duration.between(inicioDaEspera(), agora).toMinutes());
    }

    public LocalDateTime inicioDaEspera() {

        if (clienteAguardandoDesde == null || clienteAguardandoDesde.isAfter(ultimaMensagemClienteEm)
                || (ultimaMensagemEmpresaEm != null && clienteAguardandoDesde.isBefore(ultimaMensagemEmpresaEm))) {
            return ultimaMensagemClienteEm;
        }

        return clienteAguardandoDesde;
    }

    public boolean avisoDeEsperaJaEnviado() {
        return avisoEsperaEnviadoEm != null && ultimaMensagemClienteEm != null
                && !avisoEsperaEnviadoEm.isBefore(inicioDaEspera());
    }

    public int prazoRespostaMinutos() {

        Integer prazo = empresa != null ? empresa.getPrazoRespostaAtendenteMinutos() : null;

        return prazo != null && prazo > 0 ? prazo : 30;
    }

    public boolean respostaAtrasada(LocalDateTime agora) {
        return comPessoa() && clienteAguardandoResposta() && minutosEsperandoResposta(agora) >= prazoRespostaMinutos();
    }

    public Long getVersao() {
        return versao;
    }

    public LocalDateTime getUltimaMensagemClienteEm() {
        return ultimaMensagemClienteEm;
    }

    public void setUltimaMensagemClienteEm(LocalDateTime ultimaMensagemClienteEm) {
        this.ultimaMensagemClienteEm = ultimaMensagemClienteEm;
    }

    public LocalDateTime getUltimaMensagemEmpresaEm() {
        return ultimaMensagemEmpresaEm;
    }

    public void setUltimaMensagemEmpresaEm(LocalDateTime ultimaMensagemEmpresaEm) {
        this.ultimaMensagemEmpresaEm = ultimaMensagemEmpresaEm;
    }

    public String getMotivoEncerramento() {
        return motivoEncerramento;
    }

    public void setMotivoEncerramento(String motivoEncerramento) {
        this.motivoEncerramento = motivoEncerramento;
    }

    public LocalDateTime getAvisoEsperaEnviadoEm() {
        return avisoEsperaEnviadoEm;
    }

    public void setAvisoEsperaEnviadoEm(LocalDateTime avisoEsperaEnviadoEm) {
        this.avisoEsperaEnviadoEm = avisoEsperaEnviadoEm;
    }

    public LocalDateTime getClienteAguardandoDesde() {
        return clienteAguardandoDesde;
    }

    public void setClienteAguardandoDesde(LocalDateTime clienteAguardandoDesde) {
        this.clienteAguardandoDesde = clienteAguardandoDesde;
    }
}
