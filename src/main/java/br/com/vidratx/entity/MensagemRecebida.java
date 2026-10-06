package br.com.vidratx.entity;

import br.com.vidratx.enums.StatusMensagemRecebida;
import br.com.vidratx.enums.TipoMensagemRecebida;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mensagem_recebida")
public class MensagemRecebida {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_mensagem_recebida_empresa"))
    private Empresa empresa;

    @Column(name = "telefone", nullable = false, length = 20)
    private String telefone;

    @Column(name = "whatsapp_mensagem_id", length = 128)
    private String whatsappMensagemId;

    @Column(name = "enviada_em")
    private LocalDateTime enviadaEm;

    @Column(name = "recebida_em", nullable = false, updatable = false)
    private LocalDateTime recebidaEm;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 20)
    private TipoMensagemRecebida tipo = TipoMensagemRecebida.TEXTO;

    @Column(name = "conteudo", columnDefinition = "TEXT")
    private String conteudo;

    @Column(name = "midia_url", length = 500)
    private String midiaUrl;

    @Column(name = "midia_content_type", length = 100)
    private String midiaContentType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusMensagemRecebida status = StatusMensagemRecebida.RECEBIDA;

    @Column(name = "tentativas", nullable = false)
    private Integer tentativas = 0;

    @Column(name = "erro", columnDefinition = "TEXT")
    private String erro;

    @Column(name = "processada_em")
    private LocalDateTime processadaEm;

    @Column(name = "reacao_a_mensagem_id", length = 128)
    private String reacaoAMensagemId;

    @Column(name = "edita_mensagem_id", length = 128)
    private String editaMensagemId;

    @Column(name = "apaga_mensagem_id", length = 128)
    private String apagaMensagemId;

    public MensagemRecebida() {
    }

    @PrePersist
    protected void aoCriar() {
        if (recebidaEm == null) {
            recebidaEm = LocalDateTime.now();
        }
    }

    public LocalDateTime momentoDoCliente() {
        return enviadaEm != null ? enviadaEm : recebidaEm;
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

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
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
        this.enviadaEm = enviadaEm;
    }

    public LocalDateTime getRecebidaEm() {
        return recebidaEm;
    }

    public void setRecebidaEm(LocalDateTime recebidaEm) {
        this.recebidaEm = recebidaEm;
    }

    public TipoMensagemRecebida getTipo() {
        return tipo;
    }

    public void setTipo(TipoMensagemRecebida tipo) {
        this.tipo = tipo;
    }

    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }

    public String getMidiaUrl() {
        return midiaUrl;
    }

    public void setMidiaUrl(String midiaUrl) {
        this.midiaUrl = midiaUrl;
    }

    public String getMidiaContentType() {
        return midiaContentType;
    }

    public void setMidiaContentType(String midiaContentType) {
        this.midiaContentType = midiaContentType;
    }

    public StatusMensagemRecebida getStatus() {
        return status;
    }

    public void setStatus(StatusMensagemRecebida status) {
        this.status = status;
    }

    public Integer getTentativas() {
        return tentativas;
    }

    public void setTentativas(Integer tentativas) {
        this.tentativas = tentativas;
    }

    public String getErro() {
        return erro;
    }

    public void setErro(String erro) {
        this.erro = erro;
    }

    public LocalDateTime getProcessadaEm() {
        return processadaEm;
    }

    public void setProcessadaEm(LocalDateTime processadaEm) {
        this.processadaEm = processadaEm;
    }

    public String getReacaoAMensagemId() {
        return reacaoAMensagemId;
    }

    public void setReacaoAMensagemId(String reacaoAMensagemId) {
        this.reacaoAMensagemId = reacaoAMensagemId;
    }

    public String getEditaMensagemId() {
        return editaMensagemId;
    }

    public void setEditaMensagemId(String editaMensagemId) {
        this.editaMensagemId = editaMensagemId;
    }

    public String getApagaMensagemId() {
        return apagaMensagemId;
    }

    public void setApagaMensagemId(String apagaMensagemId) {
        this.apagaMensagemId = apagaMensagemId;
    }

    public boolean ehReacao() {
        return reacaoAMensagemId != null;
    }

    public boolean alteraMensagemAnterior() {
        return editaMensagemId != null || apagaMensagemId != null;
    }
}
