package br.com.vidratx.dto;

import br.com.vidratx.enums.EtapaFluxo;
import br.com.vidratx.enums.StatusAtendimento;

import java.time.LocalDateTime;
import java.util.List;

public class AtendimentoWhatsappResponse {

    private Long id;
    private Long clienteId;
    private String clienteNome;
    private String telefone;
    private StatusAtendimento status;
    private EtapaFluxo etapaFluxo;
    private Integer tentativasErro;
    private Long solicitacaoOrcamentoId;
    private Long atendenteId;
    private String atendenteNome;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
    private LocalDateTime encerradoEm;
    private boolean clienteAguardandoResposta;
    private LocalDateTime ultimaMensagemClienteEm;
    private LocalDateTime ultimaMensagemEmpresaEm;
    private String motivoEncerramento;
    private String responsavelProximaAcao;
    private String proximaAcao;
    private List<String> pendencias;

    private long minutosEsperando;
    private boolean respostaAtrasada;

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

    public Long getSolicitacaoOrcamentoId() {
        return solicitacaoOrcamentoId;
    }

    public void setSolicitacaoOrcamentoId(Long solicitacaoOrcamentoId) {
        this.solicitacaoOrcamentoId = solicitacaoOrcamentoId;
    }

    public Long getAtendenteId() {
        return atendenteId;
    }

    public void setAtendenteId(Long atendenteId) {
        this.atendenteId = atendenteId;
    }

    public String getAtendenteNome() {
        return atendenteNome;
    }

    public void setAtendenteNome(String atendenteNome) {
        this.atendenteNome = atendenteNome;
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

    public LocalDateTime getEncerradoEm() {
        return encerradoEm;
    }

    public void setEncerradoEm(LocalDateTime encerradoEm) {
        this.encerradoEm = encerradoEm;
    }

    public boolean isClienteAguardandoResposta() {
        return clienteAguardandoResposta;
    }

    public void setClienteAguardandoResposta(boolean clienteAguardandoResposta) {
        this.clienteAguardandoResposta = clienteAguardandoResposta;
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

    public List<String> getPendencias() {
        return pendencias;
    }

    public void setPendencias(List<String> pendencias) {
        this.pendencias = pendencias;
    }

    public long getMinutosEsperando() {
        return minutosEsperando;
    }

    public void setMinutosEsperando(long minutosEsperando) {
        this.minutosEsperando = minutosEsperando;
    }

    public boolean isRespostaAtrasada() {
        return respostaAtrasada;
    }

    public void setRespostaAtrasada(boolean respostaAtrasada) {
        this.respostaAtrasada = respostaAtrasada;
    }
}
