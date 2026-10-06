package br.com.vidratx.entity;

import br.com.vidratx.enums.StatusPergunta;
import br.com.vidratx.enums.TipoPergunta;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Entity
@Table(name = "pergunta_pendente")
public class PerguntaPendente {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_pergunta_pendente_empresa"))
    private Empresa empresa;

    @Column(name = "telefone", nullable = false, length = 20)
    private String telefone;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cliente_id",
            foreignKey = @ForeignKey(name = "fk_pergunta_pendente_cliente"))
    private Cliente cliente;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo", nullable = false, length = 40)
    private TipoPergunta tipo;

    @Column(name = "referencia_id", nullable = false)
    private Long referenciaId;

    @Column(name = "versao", nullable = false)
    private Integer versao;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private StatusPergunta status = StatusPergunta.ATIVA;

    @Column(name = "resumo", nullable = false, length = 255)
    private String resumo;

    @Column(name = "texto_pergunta", nullable = false, columnDefinition = "TEXT")
    private String textoPergunta;

    @Column(name = "tentativas", nullable = false)
    private Integer tentativas = 0;

    @Column(name = "criada_em", nullable = false, updatable = false)
    private LocalDateTime criadaEm;

    @Column(name = "respondida_em")
    private LocalDateTime respondidaEm;

    @Column(name = "expira_em")
    private LocalDateTime expiraEm;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    public PerguntaPendente() {
    }

    @PrePersist
    protected void aoCriar() {
        LocalDateTime agora = LocalDateTime.now();
        if (criadaEm == null) {
            criadaEm = agora.truncatedTo(ChronoUnit.SECONDS);
        }
        atualizadoEm = agora;
    }

    @PreUpdate
    protected void aoAtualizar() {
        atualizadoEm = LocalDateTime.now();
    }

    public boolean ativa() {
        return status == StatusPergunta.ATIVA;
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

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public TipoPergunta getTipo() {
        return tipo;
    }

    public void setTipo(TipoPergunta tipo) {
        this.tipo = tipo;
    }

    public Long getReferenciaId() {
        return referenciaId;
    }

    public void setReferenciaId(Long referenciaId) {
        this.referenciaId = referenciaId;
    }

    public Integer getVersao() {
        return versao;
    }

    public void setVersao(Integer versao) {
        this.versao = versao;
    }

    public StatusPergunta getStatus() {
        return status;
    }

    public void setStatus(StatusPergunta status) {
        this.status = status;
    }

    public String getResumo() {
        return resumo;
    }

    public void setResumo(String resumo) {
        this.resumo = resumo;
    }

    public String getTextoPergunta() {
        return textoPergunta;
    }

    public void setTextoPergunta(String textoPergunta) {
        this.textoPergunta = textoPergunta;
    }

    public Integer getTentativas() {
        return tentativas;
    }

    public void setTentativas(Integer tentativas) {
        this.tentativas = tentativas;
    }

    public LocalDateTime getCriadaEm() {
        return criadaEm;
    }

    public void setCriadaEm(LocalDateTime criadaEm) {
        this.criadaEm = criadaEm == null ? null : criadaEm.truncatedTo(ChronoUnit.SECONDS);
    }

    public LocalDateTime getRespondidaEm() {
        return respondidaEm;
    }

    public void setRespondidaEm(LocalDateTime respondidaEm) {
        this.respondidaEm = respondidaEm;
    }

    public LocalDateTime getExpiraEm() {
        return expiraEm;
    }

    public void setExpiraEm(LocalDateTime expiraEm) {
        this.expiraEm = expiraEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }
}
