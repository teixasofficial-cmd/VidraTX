package br.com.vidratx.dto;

import br.com.vidratx.enums.StatusProducao;

import java.time.LocalDateTime;

public class OrdemServicoResponse {

    private Long id;
    private Long empresaId;
    private Long orcamentoId;
    private String clienteNome;
    private Boolean necessitaProducao;
    private StatusProducao statusProducao;
    private String observacoes;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
    private LocalDateTime producaoIniciadaEm;
    private LocalDateTime producaoConcluidaEm;
    private LocalDateTime producaoConferidaEm;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getEmpresaId() {
        return empresaId;
    }

    public void setEmpresaId(Long empresaId) {
        this.empresaId = empresaId;
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

    public Boolean getNecessitaProducao() {
        return necessitaProducao;
    }

    public void setNecessitaProducao(Boolean necessitaProducao) {
        this.necessitaProducao = necessitaProducao;
    }

    public StatusProducao getStatusProducao() {
        return statusProducao;
    }

    public void setStatusProducao(StatusProducao statusProducao) {
        this.statusProducao = statusProducao;
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

    public LocalDateTime getProducaoIniciadaEm() {
        return producaoIniciadaEm;
    }

    public void setProducaoIniciadaEm(LocalDateTime producaoIniciadaEm) {
        this.producaoIniciadaEm = producaoIniciadaEm;
    }

    public LocalDateTime getProducaoConcluidaEm() {
        return producaoConcluidaEm;
    }

    public void setProducaoConcluidaEm(LocalDateTime producaoConcluidaEm) {
        this.producaoConcluidaEm = producaoConcluidaEm;
    }

    public LocalDateTime getProducaoConferidaEm() {
        return producaoConferidaEm;
    }

    public void setProducaoConferidaEm(LocalDateTime producaoConferidaEm) {
        this.producaoConferidaEm = producaoConferidaEm;
    }
}
