package br.com.vidratx.dto;

import br.com.vidratx.enums.StatusAgendamento;

import java.time.LocalDateTime;

public class MedicaoResponse {

    private Long id;
    private Long orcamentoId;
    private String clienteNome;
    private StatusAgendamento status;
    private LocalDateTime dataAgendada;
    private LocalDateTime dataRealizada;
    private String contrapropostaTexto;

    private LocalDateTime contrapropostaData;
    private LocalDateTime contrapropostaEm;

    private LocalDateTime cancelamentoSolicitadoEm;

    private String cancelamentoSolicitadoTexto;
    private String endereco;
    private String observacoes;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    private Integer versaoProposta;

    private String envioStatus;

    private String envioErro;

    private String responsavelProximaAcao;

    private String proximaAcao;

    private String aviso;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getOrcamentoId() {
        return orcamentoId;
    }

    public void setOrcamentoId(Long orcamentoId) {
        this.orcamentoId = orcamentoId;
    }

    public String getClienteNome() {
        return clienteNome;
    }

    public void setClienteNome(String clienteNome) {
        this.clienteNome = clienteNome;
    }

    public StatusAgendamento getStatus() {
        return status;
    }

    public void setStatus(StatusAgendamento status) {
        this.status = status;
    }

    public LocalDateTime getDataAgendada() {
        return dataAgendada;
    }

    public void setDataAgendada(LocalDateTime dataAgendada) {
        this.dataAgendada = dataAgendada;
    }

    public LocalDateTime getDataRealizada() {
        return dataRealizada;
    }

    public void setDataRealizada(LocalDateTime dataRealizada) {
        this.dataRealizada = dataRealizada;
    }

    public String getContrapropostaTexto() {
        return contrapropostaTexto;
    }

    public void setContrapropostaTexto(String contrapropostaTexto) {
        this.contrapropostaTexto = contrapropostaTexto;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }

    public void setAtualizadoEm(LocalDateTime atualizadoEm) {
        this.atualizadoEm = atualizadoEm;
    }

    public Integer getVersaoProposta() {
        return versaoProposta;
    }

    public void setVersaoProposta(Integer versaoProposta) {
        this.versaoProposta = versaoProposta;
    }

    public String getEnvioStatus() {
        return envioStatus;
    }

    public void setEnvioStatus(String envioStatus) {
        this.envioStatus = envioStatus;
    }

    public String getEnvioErro() {
        return envioErro;
    }

    public void setEnvioErro(String envioErro) {
        this.envioErro = envioErro;
    }

    public String getResponsavelProximaAcao() {
        return responsavelProximaAcao;
    }

    public void setResponsavelProximaAcao(String responsavelProximaAcao) {
        this.responsavelProximaAcao = responsavelProximaAcao;
    }

    public String getProximaAcao() {
        return proximaAcao;
    }

    public void setProximaAcao(String proximaAcao) {
        this.proximaAcao = proximaAcao;
    }

    public String getAviso() {
        return aviso;
    }

    public void setAviso(String aviso) {
        this.aviso = aviso;
    }

    public LocalDateTime getContrapropostaData() {
        return contrapropostaData;
    }

    public void setContrapropostaData(LocalDateTime contrapropostaData) {
        this.contrapropostaData = contrapropostaData;
    }

    public LocalDateTime getContrapropostaEm() {
        return contrapropostaEm;
    }

    public void setContrapropostaEm(LocalDateTime contrapropostaEm) {
        this.contrapropostaEm = contrapropostaEm;
    }

    public LocalDateTime getCancelamentoSolicitadoEm() {
        return cancelamentoSolicitadoEm;
    }

    public void setCancelamentoSolicitadoEm(LocalDateTime cancelamentoSolicitadoEm) {
        this.cancelamentoSolicitadoEm = cancelamentoSolicitadoEm;
    }

    public String getCancelamentoSolicitadoTexto() {
        return cancelamentoSolicitadoTexto;
    }

    public void setCancelamentoSolicitadoTexto(String cancelamentoSolicitadoTexto) {
        this.cancelamentoSolicitadoTexto = cancelamentoSolicitadoTexto;
    }
}
