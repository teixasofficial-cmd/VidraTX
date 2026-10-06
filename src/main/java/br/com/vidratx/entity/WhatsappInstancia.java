package br.com.vidratx.entity;

import br.com.vidratx.enums.StatusInstanciaWhatsapp;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "whatsapp_instancia",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_whatsapp_instancia_empresa",
                        columnNames = "empresa_id"
                ),
                @UniqueConstraint(
                        name = "uk_whatsapp_instancia_token",
                        columnNames = "webhook_token"
                )
        }
)
public class WhatsappInstancia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "empresa_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_whatsapp_instancia_empresa"
            )
    )
    private Empresa empresa;

    @Column(
            name = "numero",
            length = 20
    )
    private String numero;

    @Column(
            name = "webhook_token",
            nullable = false,
            length = 64
    )
    private String webhookToken;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    private StatusInstanciaWhatsapp status = StatusInstanciaWhatsapp.DESCONECTADO;

    @Column(name = "conectado_em")
    private LocalDateTime conectadoEm;

    @Column(name = "desconectado_em")
    private LocalDateTime desconectadoEm;

    @Column(name = "alerta_desconexao_em")
    private LocalDateTime alertaDesconexaoEm;

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

    public WhatsappInstancia() {
    }

    @PrePersist
    protected void aoCriar() {

        LocalDateTime agora =
                LocalDateTime.now();

        criadoEm = agora;
        atualizadoEm = agora;

        if (status == null) {
            status = StatusInstanciaWhatsapp.DESCONECTADO;
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

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public String getWebhookToken() {
        return webhookToken;
    }

    public void setWebhookToken(String webhookToken) {
        this.webhookToken = webhookToken;
    }

    public StatusInstanciaWhatsapp getStatus() {
        return status;
    }

    public void setStatus(StatusInstanciaWhatsapp status) {
        this.status = status;
    }

    public LocalDateTime getConectadoEm() {
        return conectadoEm;
    }

    public void setConectadoEm(LocalDateTime conectadoEm) {
        this.conectadoEm = conectadoEm;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public LocalDateTime getDesconectadoEm() {
        return desconectadoEm;
    }

    public LocalDateTime getAlertaDesconexaoEm() {
        return alertaDesconexaoEm;
    }

    public void setAlertaDesconexaoEm(LocalDateTime alertaDesconexaoEm) {
        this.alertaDesconexaoEm = alertaDesconexaoEm;
    }

    public void alterarStatus(StatusInstanciaWhatsapp novoStatus, LocalDateTime agora) {

        if (novoStatus == StatusInstanciaWhatsapp.CONECTADO) {
            desconectadoEm = null;
        } else if (status == StatusInstanciaWhatsapp.CONECTADO) {
            desconectadoEm = agora;
        }

        status = novoStatus;
    }
}
