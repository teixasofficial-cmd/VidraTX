package br.com.vidratx.entity;

import br.com.vidratx.enums.CategoriaMensagemSaida;
import br.com.vidratx.enums.StatusMensagemSaida;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "mensagem_saida")
public class MensagemSaida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_mensagem_saida_empresa"))
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "atendimento_id",
            foreignKey = @ForeignKey(name = "fk_mensagem_saida_atendimento"))
    private AtendimentoWhatsapp atendimento;

    @Column(name = "telefone", nullable = false, length = 20)
    private String telefone;

    @Column(name = "conteudo", nullable = false, columnDefinition = "TEXT")
    private String conteudo;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", nullable = false, length = 30)
    private CategoriaMensagemSaida categoria;

    @Column(name = "referencia_tipo", length = 30)
    private String referenciaTipo;

    @Column(name = "referencia_id")
    private Long referenciaId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pergunta_id",
            foreignKey = @ForeignKey(name = "fk_mensagem_saida_pergunta"))
    private PerguntaPendente pergunta;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusMensagemSaida status = StatusMensagemSaida.PENDENTE;

    @Column(name = "tentativas", nullable = false)
    private Integer tentativas = 0;

    @Column(name = "proxima_tentativa_em")
    private LocalDateTime proximaTentativaEm;

    @Column(name = "ultimo_erro", columnDefinition = "TEXT")
    private String ultimoErro;

    @Column(name = "whatsapp_mensagem_id", length = 128)
    private String whatsappMensagemId;

    @Column(name = "enviada_em")
    private LocalDateTime enviadaEm;

    @Column(name = "entregue_em")
    private LocalDateTime entregueEm;

    @Column(name = "lida_em")
    private LocalDateTime lidaEm;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    public MensagemSaida() {
    }

    @PrePersist
    protected void aoCriar() {
        LocalDateTime agora = LocalDateTime.now();
        criadoEm = agora;
        atualizadoEm = agora;
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

    public AtendimentoWhatsapp getAtendimento() {
        return atendimento;
    }

    public void setAtendimento(AtendimentoWhatsapp atendimento) {
        this.atendimento = atendimento;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }

    public CategoriaMensagemSaida getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaMensagemSaida categoria) {
        this.categoria = categoria;
    }

    public String getReferenciaTipo() {
        return referenciaTipo;
    }

    public void setReferenciaTipo(String referenciaTipo) {
        this.referenciaTipo = referenciaTipo;
    }

    public Long getReferenciaId() {
        return referenciaId;
    }

    public void setReferenciaId(Long referenciaId) {
        this.referenciaId = referenciaId;
    }

    public PerguntaPendente getPergunta() {
        return pergunta;
    }

    public void setPergunta(PerguntaPendente pergunta) {
        this.pergunta = pergunta;
    }

    public StatusMensagemSaida getStatus() {
        return status;
    }

    public void setStatus(StatusMensagemSaida status) {
        this.status = status;
    }

    public Integer getTentativas() {
        return tentativas;
    }

    public void setTentativas(Integer tentativas) {
        this.tentativas = tentativas;
    }

    public LocalDateTime getProximaTentativaEm() {
        return proximaTentativaEm;
    }

    public void setProximaTentativaEm(LocalDateTime proximaTentativaEm) {
        this.proximaTentativaEm = proximaTentativaEm;
    }

    public String getUltimoErro() {
        return ultimoErro;
    }

    public void setUltimoErro(String ultimoErro) {
        this.ultimoErro = ultimoErro;
    }

    public String getWhatsappMensagemId() {
        return whatsappMensagemId;
    }

    public void setWhatsappMensagemId(String whatsappMensagemId) {
        this.whatsappMensagemId = whatsappMensagemId;
    }

    public LocalDateTime getEnviadaEm() {
        return enviadaEm;
    }

    public void setEnviadaEm(LocalDateTime enviadaEm) {
        this.enviadaEm = enviadaEm == null ? null : enviadaEm.truncatedTo(ChronoUnit.SECONDS);
    }

    public LocalDateTime getEntregueEm() {
        return entregueEm;
    }

    public void setEntregueEm(LocalDateTime entregueEm) {
        this.entregueEm = entregueEm;
    }

    public LocalDateTime getLidaEm() {
        return lidaEm;
    }

    public void setLidaEm(LocalDateTime lidaEm) {
        this.lidaEm = lidaEm;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }
}
