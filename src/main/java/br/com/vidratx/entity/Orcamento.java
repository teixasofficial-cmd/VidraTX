package br.com.vidratx.entity;

import br.com.vidratx.enums.MotivoPerda;
import br.com.vidratx.enums.StatusOrcamento;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "orcamento")
public class Orcamento {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "empresa_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_orcamento_empresa"
            )
    )
    private Empresa empresa;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "cliente_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_orcamento_cliente"
            )
    )
    private Cliente cliente;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "solicitacao_orcamento_id",
            foreignKey = @ForeignKey(
                    name = "fk_orcamento_solicitacao"
            )
    )
    private SolicitacaoOrcamento solicitacaoOrcamento;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "status",
            nullable = false,
            length = 20
    )
    private StatusOrcamento status = StatusOrcamento.NOVO_CONTATO;

    @Column(
            name = "observacoes",
            columnDefinition = "TEXT"
    )
    private String observacoes;

    @Column(name = "valido_ate")
    private LocalDate validoAte;

    @Column(
            name = "valor_total",
            nullable = false,
            precision = 12,
            scale = 2
    )
    private BigDecimal valorTotal = BigDecimal.ZERO;

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

    @Column(name = "enviado_em")
    private LocalDateTime enviadoEm;

    @Column(name = "respondido_em")
    private LocalDateTime respondidoEm;

    @Column(name = "lembrete_enviado_em")
    private LocalDateTime lembreteEnviadoEm;

    @Enumerated(EnumType.STRING)
    @Column(name = "motivo_perda", length = 30)
    private MotivoPerda motivoPerda;

    @Column(name = "motivo_perda_outro", length = 255)
    private String motivoPerdaOutro;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_antes_perda", length = 30)
    private StatusOrcamento statusAntesPerda;

    @Column(name = "custo_total_centavos")
    private Long custoTotalCentavos;

    @Column(name = "preco_sugerido_centavos")
    private Long precoSugeridoCentavos;

    @Column(name = "valor_total_centavos")
    private Long valorTotalCentavos;

    @Column(name = "ajuste_comercial_centavos", nullable = false)
    private Long ajusteComercialCentavos = 0L;

    @Column(name = "margem_real_percentual", precision = 5, scale = 2)
    private BigDecimal margemRealPercentual;

    @Column(name = "custo_real_total_centavos")
    private Long custoRealTotalCentavos;

    @Column(name = "margem_sobre_custo_real_percentual", precision = 5, scale = 2)
    private BigDecimal margemSobreCustoRealPercentual;

    @Column(name = "custo_real_incompleto", nullable = false)
    private Boolean custoRealIncompleto = false;

    @Version
    @Column(name = "versao", nullable = false)
    private Long versao;

    @Column(name = "preco_final_manual_centavos")
    private Long precoFinalManualCentavos;

    @Column(name = "parcelas_cartao")
    private Integer parcelasCartao;

    @Column(name = "revisao_envio", nullable = false)
    private Integer revisaoEnvio = 0;

    @Column(name = "alteracao_solicitada_em")
    private LocalDateTime alteracaoSolicitadaEm;

    @Column(name = "alteracao_solicitada_texto", columnDefinition = "TEXT")
    private String alteracaoSolicitadaTexto;

    @Column(name = "estimativa_enviada_em")
    private LocalDateTime estimativaEnviadaEm;

    public Orcamento() {
    }

    @PrePersist
    protected void aoCriar() {

        LocalDateTime agora =
                LocalDateTime.now();

        criadoEm = agora;
        atualizadoEm = agora;

        if (status == null) {
            status = StatusOrcamento.NOVO_CONTATO;
        }

        if (valorTotal == null) {
            valorTotal = BigDecimal.ZERO;
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

    public Cliente getCliente() {
        return cliente;
    }

    public void setCliente(Cliente cliente) {
        this.cliente = cliente;
    }

    public SolicitacaoOrcamento getSolicitacaoOrcamento() {
        return solicitacaoOrcamento;
    }

    public void setSolicitacaoOrcamento(SolicitacaoOrcamento solicitacaoOrcamento) {
        this.solicitacaoOrcamento = solicitacaoOrcamento;
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

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
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

    public LocalDateTime getLembreteEnviadoEm() {
        return lembreteEnviadoEm;
    }

    public void setLembreteEnviadoEm(LocalDateTime lembreteEnviadoEm) {
        this.lembreteEnviadoEm = lembreteEnviadoEm;
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

    public Long getCustoTotalCentavos() {
        return custoTotalCentavos;
    }

    public void setCustoTotalCentavos(Long custoTotalCentavos) {
        this.custoTotalCentavos = custoTotalCentavos;
    }

    public Long getPrecoSugeridoCentavos() {
        return precoSugeridoCentavos;
    }

    public void setPrecoSugeridoCentavos(Long precoSugeridoCentavos) {
        this.precoSugeridoCentavos = precoSugeridoCentavos;
    }

    public Long getValorTotalCentavos() {
        return valorTotalCentavos;
    }

    public void setValorTotalCentavos(Long valorTotalCentavos) {
        this.valorTotalCentavos = valorTotalCentavos;
    }

    public Long getAjusteComercialCentavos() {
        return ajusteComercialCentavos;
    }

    public void setAjusteComercialCentavos(Long ajusteComercialCentavos) {
        this.ajusteComercialCentavos = ajusteComercialCentavos == null ? 0L : ajusteComercialCentavos;
    }

    public BigDecimal getMargemRealPercentual() {
        return margemRealPercentual;
    }

    public void setMargemRealPercentual(BigDecimal margemRealPercentual) {
        this.margemRealPercentual = margemRealPercentual;
    }

    public Long getCustoRealTotalCentavos() {
        return custoRealTotalCentavos;
    }

    public void setCustoRealTotalCentavos(Long custoRealTotalCentavos) {
        this.custoRealTotalCentavos = custoRealTotalCentavos;
    }

    public BigDecimal getMargemSobreCustoRealPercentual() {
        return margemSobreCustoRealPercentual;
    }

    public void setMargemSobreCustoRealPercentual(BigDecimal margemSobreCustoRealPercentual) {
        this.margemSobreCustoRealPercentual = margemSobreCustoRealPercentual;
    }

    public Boolean getCustoRealIncompleto() {
        return custoRealIncompleto;
    }

    public void setCustoRealIncompleto(Boolean custoRealIncompleto) {
        this.custoRealIncompleto = custoRealIncompleto == null ? Boolean.FALSE : custoRealIncompleto;
    }

    public Long getVersao() {
        return versao;
    }

    public Long getPrecoFinalManualCentavos() {
        return precoFinalManualCentavos;
    }

    public void setPrecoFinalManualCentavos(Long precoFinalManualCentavos) {
        this.precoFinalManualCentavos = precoFinalManualCentavos;
    }

    public Integer getParcelasCartao() {
        return parcelasCartao;
    }

    public void setParcelasCartao(Integer parcelasCartao) {
        this.parcelasCartao = parcelasCartao;
    }

    public Integer getRevisaoEnvio() {
        return revisaoEnvio;
    }

    public void setRevisaoEnvio(Integer revisaoEnvio) {
        this.revisaoEnvio = revisaoEnvio;
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

    public StatusOrcamento getStatusAntesPerda() {
        return statusAntesPerda;
    }

    public void setStatusAntesPerda(StatusOrcamento statusAntesPerda) {
        this.statusAntesPerda = statusAntesPerda;
    }
}
