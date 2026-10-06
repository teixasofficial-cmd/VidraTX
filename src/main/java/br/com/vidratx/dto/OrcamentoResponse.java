package br.com.vidratx.dto;

import br.com.vidratx.enums.MotivoPerda;
import br.com.vidratx.enums.StatusOrcamento;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class OrcamentoResponse {

    private Long id;
    private Long empresaId;
    private Long clienteId;
    private String clienteNome;
    private String clienteTelefone;
    private String clienteWhatsapp;
    private String clienteEmail;
    private Long solicitacaoOrcamentoId;
    private String especificacoes;
    private StatusOrcamento status;
    private String observacoes;
    private LocalDate validoAte;
    private BigDecimal valorTotal;
    private LocalDateTime criadoEm;
    private LocalDateTime atualizadoEm;
    private LocalDateTime enviadoEm;
    private LocalDateTime respondidoEm;
    private MotivoPerda motivoPerda;
    private String motivoPerdaOutro;
    private BigDecimal custoTotal;
    private BigDecimal precoSugerido;
    private BigDecimal ajusteComercial;
    private BigDecimal margemReal;
    private Integer revisaoEnvio;
    private Integer parcelasCartao;
    private BigDecimal precoFinalManual;
    private LocalDateTime alteracaoSolicitadaEm;
    private String alteracaoSolicitadaTexto;
    private LocalDateTime estimativaEnviadaEm;
    private boolean vencido;
    private String envioStatus;
    private String envioErro;
    private String responsavelProximaAcao;
    private String proximaAcao;
    private String medicaoStatus;
    private LocalDateTime medicaoData;
    private Long ordemServicoId;
    private String aviso;

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

    public String getClienteTelefone() {
        return clienteTelefone;
    }

    public void setClienteTelefone(String clienteTelefone) {
        this.clienteTelefone = clienteTelefone;
    }

    public String getClienteWhatsapp() {
        return clienteWhatsapp;
    }

    public void setClienteWhatsapp(String clienteWhatsapp) {
        this.clienteWhatsapp = clienteWhatsapp;
    }

    public String getClienteEmail() {
        return clienteEmail;
    }

    public void setClienteEmail(String clienteEmail) {
        this.clienteEmail = clienteEmail;
    }

    public Long getSolicitacaoOrcamentoId() {
        return solicitacaoOrcamentoId;
    }

    public void setSolicitacaoOrcamentoId(Long solicitacaoOrcamentoId) {
        this.solicitacaoOrcamentoId = solicitacaoOrcamentoId;
    }

    public String getEspecificacoes() {
        return especificacoes;
    }

    public void setEspecificacoes(String especificacoes) {
        this.especificacoes = especificacoes;
    }

    public StatusOrcamento getStatus() {
        return status;
    }

    public void setStatus(StatusOrcamento status) {
        this.status = status;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public LocalDate getValidoAte() {
        return validoAte;
    }

    public void setValidoAte(LocalDate validoAte) {
        this.validoAte = validoAte;
    }

    public BigDecimal getValorTotal() {
        return valorTotal;
    }

    public void setValorTotal(BigDecimal valorTotal) {
        this.valorTotal = valorTotal;
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

    public LocalDateTime getEnviadoEm() {
        return enviadoEm;
    }

    public void setEnviadoEm(LocalDateTime enviadoEm) {
        this.enviadoEm = enviadoEm;
    }

    public LocalDateTime getRespondidoEm() {
        return respondidoEm;
    }

    public void setRespondidoEm(LocalDateTime respondidoEm) {
        this.respondidoEm = respondidoEm;
    }

    public MotivoPerda getMotivoPerda() {
        return motivoPerda;
    }

    public void setMotivoPerda(MotivoPerda motivoPerda) {
        this.motivoPerda = motivoPerda;
    }

    public String getMotivoPerdaOutro() {
        return motivoPerdaOutro;
    }

    public void setMotivoPerdaOutro(String motivoPerdaOutro) {
        this.motivoPerdaOutro = motivoPerdaOutro;
    }

    public BigDecimal getCustoTotal() {
        return custoTotal;
    }

    public void setCustoTotal(BigDecimal custoTotal) {
        this.custoTotal = custoTotal;
    }

    public BigDecimal getPrecoSugerido() {
        return precoSugerido;
    }

    public void setPrecoSugerido(BigDecimal precoSugerido) {
        this.precoSugerido = precoSugerido;
    }

    public BigDecimal getAjusteComercial() {
        return ajusteComercial;
    }

    public void setAjusteComercial(BigDecimal ajusteComercial) {
        this.ajusteComercial = ajusteComercial;
    }

    public BigDecimal getMargemReal() {
        return margemReal;
    }

    public void setMargemReal(BigDecimal margemReal) {
        this.margemReal = margemReal;
    }

    public Integer getRevisaoEnvio() {
        return revisaoEnvio;
    }

    public void setRevisaoEnvio(Integer revisaoEnvio) {
        this.revisaoEnvio = revisaoEnvio;
    }

    public Integer getParcelasCartao() {
        return parcelasCartao;
    }

    public void setParcelasCartao(Integer parcelasCartao) {
        this.parcelasCartao = parcelasCartao;
    }

    public BigDecimal getPrecoFinalManual() {
        return precoFinalManual;
    }

    public void setPrecoFinalManual(BigDecimal precoFinalManual) {
        this.precoFinalManual = precoFinalManual;
    }

    public LocalDateTime getAlteracaoSolicitadaEm() {
        return alteracaoSolicitadaEm;
    }

    public void setAlteracaoSolicitadaEm(LocalDateTime alteracaoSolicitadaEm) {
        this.alteracaoSolicitadaEm = alteracaoSolicitadaEm;
    }

    public String getAlteracaoSolicitadaTexto() {
        return alteracaoSolicitadaTexto;
    }

    public void setAlteracaoSolicitadaTexto(String alteracaoSolicitadaTexto) {
        this.alteracaoSolicitadaTexto = alteracaoSolicitadaTexto;
    }

    public LocalDateTime getEstimativaEnviadaEm() {
        return estimativaEnviadaEm;
    }

    public void setEstimativaEnviadaEm(LocalDateTime estimativaEnviadaEm) {
        this.estimativaEnviadaEm = estimativaEnviadaEm;
    }

    public boolean isVencido() {
        return vencido;
    }

    public void setVencido(boolean vencido) {
        this.vencido = vencido;
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

    public String getMedicaoStatus() {
        return medicaoStatus;
    }

    public void setMedicaoStatus(String medicaoStatus) {
        this.medicaoStatus = medicaoStatus;
    }

    public LocalDateTime getMedicaoData() {
        return medicaoData;
    }

    public void setMedicaoData(LocalDateTime medicaoData) {
        this.medicaoData = medicaoData;
    }

    public Long getOrdemServicoId() {
        return ordemServicoId;
    }

    public void setOrdemServicoId(Long ordemServicoId) {
        this.ordemServicoId = ordemServicoId;
    }

    public String getAviso() {
        return aviso;
    }

    public void setAviso(String aviso) {
        this.aviso = aviso;
    }
}
