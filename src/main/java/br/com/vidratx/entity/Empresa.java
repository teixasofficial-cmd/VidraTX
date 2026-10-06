package br.com.vidratx.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "empresa",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_empresa_cnpj",
                        columnNames = "cnpj"
                ),
                @UniqueConstraint(
                        name = "uk_empresa_slug",
                        columnNames = "slug"
                )
        }
)
public class Empresa {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(
            name = "razao_social",
            nullable = false,
            length = 150
    )
    private String razaoSocial;

    @Column(
            name = "nome_fantasia",
            nullable = false,
            length = 150
    )
    private String nomeFantasia;

    @Column(
            name = "cnpj",
            nullable = false,
            unique = true,
            length = 14
    )
    private String cnpj;

    @Column(
            name = "slug",
            nullable = false,
            unique = true,
            length = 60
    )
    private String slug;

    @Column(
            name = "email",
            length = 150
    )
    private String email;

    @Column(
            name = "telefone",
            length = 20
    )
    private String telefone;

    @Column(
            name = "whatsapp",
            length = 20
    )
    private String whatsapp;

    @Column(
            name = "logo_url",
            length = 500
    )
    private String logoUrl;

    @Column(
            name = "cor_primaria",
            length = 7
    )
    private String corPrimaria;

    @Column(
            name = "cor_secundaria",
            length = 7
    )
    private String corSecundaria;

    @Column(
            name = "endereco",
            length = 255
    )
    private String endereco;

    @Column(
            name = "sobre",
            columnDefinition = "TEXT"
    )
    private String sobre;

    @Column(
            name = "ativa",
            nullable = false
    )
    private Boolean ativa = true;

    @Column(name = "duracao_medicao_minutos", nullable = false)
    private Integer duracaoMedicaoMinutos = 60;

    @Column(name = "duracao_instalacao_minutos", nullable = false)
    private Integer duracaoInstalacaoMinutos = 240;

    @Column(name = "medicoes_simultaneas", nullable = false)
    private Integer medicoesSimultaneas = 1;

    @Column(name = "hora_inicio_mensagens", nullable = false)
    private Integer horaInicioMensagens = 8;

    @Column(name = "hora_fim_mensagens", nullable = false)
    private Integer horaFimMensagens = 20;

    @Column(name = "fuso_horario", length = 40)
    private String fusoHorario;

    @Column(name = "antecedencia_minima_minutos", nullable = false)
    private Integer antecedenciaMinimaMinutos = 60;

    @Column(name = "prazo_resposta_atendente_minutos", nullable = false)
    private Integer prazoRespostaAtendenteMinutos = 30;

    @Column(name = "mensagem_fora_horario", length = 500)
    private String mensagemForaHorario;

    @Column(name = "condicoes_pagamento", length = 500)
    private String condicoesPagamento;

    @Column(name = "lembrete_orcamento_ativo", nullable = false)
    private Boolean lembreteOrcamentoAtivo = true;

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

    public Empresa() {
    }

    @PrePersist
    protected void aoCriar() {

        LocalDateTime agora =
                LocalDateTime.now();

        criadoEm = agora;
        atualizadoEm = agora;

        if (ativa == null) {
            ativa = true;
        }
    }

    @PreUpdate
    protected void aoAtualizar() {
        atualizadoEm = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public void setRazaoSocial(String razaoSocial) {
        this.razaoSocial = razaoSocial;
    }

    public String getNomeFantasia() {
        return nomeFantasia;
    }

    public void setNomeFantasia(String nomeFantasia) {
        this.nomeFantasia = nomeFantasia;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getWhatsapp() {
        return whatsapp;
    }

    public void setWhatsapp(String whatsapp) {
        this.whatsapp = whatsapp;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getCorPrimaria() {
        return corPrimaria;
    }

    public void setCorPrimaria(String corPrimaria) {
        this.corPrimaria = corPrimaria;
    }

    public String getCorSecundaria() {
        return corSecundaria;
    }

    public void setCorSecundaria(String corSecundaria) {
        this.corSecundaria = corSecundaria;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getSobre() {
        return sobre;
    }

    public void setSobre(String sobre) {
        this.sobre = sobre;
    }

    public Boolean getAtiva() {
        return ativa;
    }

    public void setAtiva(Boolean ativa) {
        this.ativa = ativa;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public Integer getDuracaoMedicaoMinutos() {
        return duracaoMedicaoMinutos;
    }

    public void setDuracaoMedicaoMinutos(Integer duracaoMedicaoMinutos) {
        this.duracaoMedicaoMinutos = duracaoMedicaoMinutos;
    }

    public Integer getDuracaoInstalacaoMinutos() {
        return duracaoInstalacaoMinutos;
    }

    public void setDuracaoInstalacaoMinutos(Integer duracaoInstalacaoMinutos) {
        this.duracaoInstalacaoMinutos = duracaoInstalacaoMinutos;
    }

    public Integer getMedicoesSimultaneas() {
        return medicoesSimultaneas;
    }

    public void setMedicoesSimultaneas(Integer medicoesSimultaneas) {
        this.medicoesSimultaneas = medicoesSimultaneas;
    }

    public Integer getHoraInicioMensagens() {
        return horaInicioMensagens;
    }

    public void setHoraInicioMensagens(Integer horaInicioMensagens) {
        this.horaInicioMensagens = horaInicioMensagens;
    }

    public Integer getHoraFimMensagens() {
        return horaFimMensagens;
    }

    public void setHoraFimMensagens(Integer horaFimMensagens) {
        this.horaFimMensagens = horaFimMensagens;
    }

    public String getFusoHorario() {
        return fusoHorario;
    }

    public void setFusoHorario(String fusoHorario) {
        this.fusoHorario = fusoHorario;
    }

    public Integer getAntecedenciaMinimaMinutos() {
        return antecedenciaMinimaMinutos;
    }

    public void setAntecedenciaMinimaMinutos(Integer antecedenciaMinimaMinutos) {
        this.antecedenciaMinimaMinutos = antecedenciaMinimaMinutos;
    }

    public Integer getPrazoRespostaAtendenteMinutos() {
        return prazoRespostaAtendenteMinutos;
    }

    public void setPrazoRespostaAtendenteMinutos(Integer prazoRespostaAtendenteMinutos) {
        this.prazoRespostaAtendenteMinutos = prazoRespostaAtendenteMinutos;
    }

    public String getMensagemForaHorario() {
        return mensagemForaHorario;
    }

    public void setMensagemForaHorario(String mensagemForaHorario) {
        this.mensagemForaHorario = mensagemForaHorario;
    }

    public String getCondicoesPagamento() {
        return condicoesPagamento;
    }

    public void setCondicoesPagamento(String condicoesPagamento) {
        this.condicoesPagamento = condicoesPagamento;
    }

    public Boolean getLembreteOrcamentoAtivo() {
        return lembreteOrcamentoAtivo;
    }

    public void setLembreteOrcamentoAtivo(Boolean lembreteOrcamentoAtivo) {
        this.lembreteOrcamentoAtivo = lembreteOrcamentoAtivo;
    }
}
