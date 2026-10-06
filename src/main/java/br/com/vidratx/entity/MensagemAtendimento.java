package br.com.vidratx.entity;

import br.com.vidratx.enums.RemetenteMensagem;
import br.com.vidratx.enums.TipoMensagem;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mensagem_atendimento")
public class MensagemAtendimento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "atendimento_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_mensagem_atendimento_atendimento"
            )
    )
    private AtendimentoWhatsapp atendimento;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "remetente",
            nullable = false,
            length = 20
    )
    private RemetenteMensagem remetente;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "tipo",
            nullable = false,
            length = 20
    )
    private TipoMensagem tipo = TipoMensagem.TEXTO;

    @Column(
            name = "conteudo",
            columnDefinition = "TEXT"
    )
    private String conteudo;

    @Column(
            name = "midia_url",
            length = 500
    )
    private String midiaUrl;

    @Column(
            name = "midia_content_type",
            length = 100
    )
    private String midiaContentType;

    @Column(name = "whatsapp_mensagem_id", length = 128)
    private String whatsappMensagemId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mensagem_saida_id",
            foreignKey = @ForeignKey(name = "fk_mensagem_atendimento_saida"))
    private MensagemSaida mensagemSaida;

    @Column(
            name = "enviado_em",
            nullable = false,
            updatable = false
    )
    private LocalDateTime enviadoEm;

    public MensagemAtendimento() {
    }

    @PrePersist
    protected void aoCriar() {

        if (enviadoEm == null) {
            enviadoEm = LocalDateTime.now();
        }

        if (tipo == null) {
            tipo = TipoMensagem.TEXTO;
        }
    }

    public Long getId() {
        return id;
    }

    public AtendimentoWhatsapp getAtendimento() {
        return atendimento;
    }

    public void setAtendimento(AtendimentoWhatsapp atendimento) {
        this.atendimento = atendimento;
    }

    public RemetenteMensagem getRemetente() {
        return remetente;
    }

    public void setRemetente(RemetenteMensagem remetente) {
        this.remetente = remetente;
    }

    public TipoMensagem getTipo() {
        return tipo;
    }

    public void setTipo(TipoMensagem tipo) {
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

    public LocalDateTime getEnviadoEm() {
        return enviadoEm;
    }

    public String getWhatsappMensagemId() {
        return whatsappMensagemId;
    }

    public void setWhatsappMensagemId(String whatsappMensagemId) {
        this.whatsappMensagemId = whatsappMensagemId;
    }

    public MensagemSaida getMensagemSaida() {
        return mensagemSaida;
    }

    public void setMensagemSaida(MensagemSaida mensagemSaida) {
        this.mensagemSaida = mensagemSaida;
    }

    public void setEnviadoEm(LocalDateTime enviadoEm) {
        this.enviadoEm = enviadoEm;
    }
}
