package br.com.vidratx.dto;

import br.com.vidratx.enums.CanalSolicitacao;
import br.com.vidratx.enums.StatusSolicitacaoOrcamento;

import java.time.LocalDateTime;

public class SolicitacaoOrcamentoResponse {

    private Long id;
    private Long clienteId;
    private String clienteNome;
    private CanalSolicitacao canal;
    private StatusSolicitacaoOrcamento status;
    private String descricao;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public String getClienteNome() {
        return clienteNome;
    }

    public void setClienteNome(String clienteNome) {
        this.clienteNome = clienteNome;
    }

    public CanalSolicitacao getCanal() {
        return canal;
    }

    public void setCanal(CanalSolicitacao canal) {
        this.canal = canal;
    }

    public StatusSolicitacaoOrcamento getStatus() {
        return status;
    }

    public void setStatus(StatusSolicitacaoOrcamento status) {
        this.status = status;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
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
}
