package br.com.vidratx.entity;

import br.com.vidratx.enums.ArredondamentoComercial;
import br.com.vidratx.enums.ModoPrecificacao;
import br.com.vidratx.enums.RegimeTributario;
import br.com.vidratx.enums.RegraDeslocamento;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "parametro_calculo")
public class ParametroCalculo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "empresa_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_parametro_calculo_empresa")
    )
    private Empresa empresa;

    @Enumerated(EnumType.STRING)
    @Column(name = "modo_precificacao", nullable = false, length = 10)
    private ModoPrecificacao modoPrecificacao = ModoPrecificacao.CUSTO;

    @Column(name = "multiplo_arredondamento_mm", nullable = false)
    private Short multiploArredondamentoMm = 50;

    @Column(name = "area_minima_m2", nullable = false, precision = 6, scale = 4)
    private BigDecimal areaMinimaM2 = new BigDecimal("0.2500");

    @Column(name = "percentual_perdas", nullable = false, precision = 5, scale = 2)
    private BigDecimal percentualPerdas = new BigDecimal("3.00");

    @Column(name = "percentual_impostos", nullable = false, precision = 5, scale = 2)
    private BigDecimal percentualImpostos = new BigDecimal("6.00");

    @Column(name = "percentual_comissao", nullable = false, precision = 5, scale = 2)
    private BigDecimal percentualComissao = BigDecimal.ZERO;

    @Column(name = "percentual_margem_desejada", nullable = false, precision = 5, scale = 2)
    private BigDecimal percentualMargemDesejada = new BigDecimal("25.00");

    @Column(name = "percentual_margem_minima", nullable = false, precision = 5, scale = 2)
    private BigDecimal percentualMargemMinima = new BigDecimal("15.00");

    @Column(name = "taxa_cartao_parcelas", columnDefinition = "json")
    private String taxaCartaoParcelas;

    @Enumerated(EnumType.STRING)
    @Column(name = "arredondamento_comercial", nullable = false, length = 20)
    private ArredondamentoComercial arredondamentoComercial = ArredondamentoComercial.NENHUM;

    @Column(name = "variacao_pre_orcamento_min_pct", nullable = false, precision = 5, scale = 2)
    private BigDecimal variacaoPreOrcamentoMinPct = new BigDecimal("-10.00");

    @Column(name = "variacao_pre_orcamento_max_pct", nullable = false, precision = 5, scale = 2)
    private BigDecimal variacaoPreOrcamentoMaxPct = new BigDecimal("15.00");

    @Column(name = "valor_visita_tecnica_centavos")
    private Long valorVisitaTecnicaCentavos;

    @Enumerated(EnumType.STRING)
    @Column(name = "regra_deslocamento", nullable = false, length = 20)
    private RegraDeslocamento regraDeslocamento = RegraDeslocamento.FIXO;

    @Column(name = "valor_deslocamento_centavos")
    private Long valorDeslocamentoCentavos;

    @Column(name = "validade_padrao_dias", nullable = false)
    private Integer validadePadraoDias = 7;

    @Column(name = "tamanho_maximo_chapa_largura_mm", nullable = false)
    private Integer tamanhoMaximoChapaLarguraMm = 3210;

    @Column(name = "tamanho_maximo_chapa_altura_mm", nullable = false)
    private Integer tamanhoMaximoChapaAlturaMm = 2250;

    @Column(name = "tolerancia_prumo_nivel_mm", nullable = false)
    private Short toleranciaPrumoNivelMm = 5;

    @Enumerated(EnumType.STRING)
    @Column(name = "regime_tributario", length = 20)
    private RegimeTributario regimeTributario;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    public ParametroCalculo() {
    }

    @PrePersist
    protected void aoCriar() {

        LocalDateTime agora = LocalDateTime.now();

        criadoEm = agora;
        atualizadoEm = agora;
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

    public ModoPrecificacao getModoPrecificacao() {
        return modoPrecificacao;
    }

    public void setModoPrecificacao(ModoPrecificacao modoPrecificacao) {
        this.modoPrecificacao = modoPrecificacao;
    }

    public Short getMultiploArredondamentoMm() {
        return multiploArredondamentoMm;
    }

    public void setMultiploArredondamentoMm(Short multiploArredondamentoMm) {
        this.multiploArredondamentoMm = multiploArredondamentoMm;
    }

    public BigDecimal getAreaMinimaM2() {
        return areaMinimaM2;
    }

    public void setAreaMinimaM2(BigDecimal areaMinimaM2) {
        this.areaMinimaM2 = areaMinimaM2;
    }

    public BigDecimal getPercentualPerdas() {
        return percentualPerdas;
    }

    public void setPercentualPerdas(BigDecimal percentualPerdas) {
        this.percentualPerdas = percentualPerdas;
    }

    public BigDecimal getPercentualImpostos() {
        return percentualImpostos;
    }

    public void setPercentualImpostos(BigDecimal percentualImpostos) {
        this.percentualImpostos = percentualImpostos;
    }

    public BigDecimal getPercentualComissao() {
        return percentualComissao;
    }

    public void setPercentualComissao(BigDecimal percentualComissao) {
        this.percentualComissao = percentualComissao;
    }

    public BigDecimal getPercentualMargemDesejada() {
        return percentualMargemDesejada;
    }

    public void setPercentualMargemDesejada(BigDecimal percentualMargemDesejada) {
        this.percentualMargemDesejada = percentualMargemDesejada;
    }

    public BigDecimal getPercentualMargemMinima() {
        return percentualMargemMinima;
    }

    public void setPercentualMargemMinima(BigDecimal percentualMargemMinima) {
        this.percentualMargemMinima = percentualMargemMinima;
    }

    public String getTaxaCartaoParcelas() {
        return taxaCartaoParcelas;
    }

    public void setTaxaCartaoParcelas(String taxaCartaoParcelas) {
        this.taxaCartaoParcelas = taxaCartaoParcelas;
    }

    public ArredondamentoComercial getArredondamentoComercial() {
        return arredondamentoComercial;
    }

    public void setArredondamentoComercial(ArredondamentoComercial arredondamentoComercial) {
        this.arredondamentoComercial = arredondamentoComercial;
    }

    public BigDecimal getVariacaoPreOrcamentoMinPct() {
        return variacaoPreOrcamentoMinPct;
    }

    public void setVariacaoPreOrcamentoMinPct(BigDecimal variacaoPreOrcamentoMinPct) {
        this.variacaoPreOrcamentoMinPct = variacaoPreOrcamentoMinPct;
    }

    public BigDecimal getVariacaoPreOrcamentoMaxPct() {
        return variacaoPreOrcamentoMaxPct;
    }

    public void setVariacaoPreOrcamentoMaxPct(BigDecimal variacaoPreOrcamentoMaxPct) {
        this.variacaoPreOrcamentoMaxPct = variacaoPreOrcamentoMaxPct;
    }

    public Long getValorVisitaTecnicaCentavos() {
        return valorVisitaTecnicaCentavos;
    }

    public void setValorVisitaTecnicaCentavos(Long valorVisitaTecnicaCentavos) {
        this.valorVisitaTecnicaCentavos = valorVisitaTecnicaCentavos;
    }

    public RegraDeslocamento getRegraDeslocamento() {
        return regraDeslocamento;
    }

    public void setRegraDeslocamento(RegraDeslocamento regraDeslocamento) {
        this.regraDeslocamento = regraDeslocamento;
    }

    public Long getValorDeslocamentoCentavos() {
        return valorDeslocamentoCentavos;
    }

    public void setValorDeslocamentoCentavos(Long valorDeslocamentoCentavos) {
        this.valorDeslocamentoCentavos = valorDeslocamentoCentavos;
    }

    public Integer getValidadePadraoDias() {
        return validadePadraoDias;
    }

    public void setValidadePadraoDias(Integer validadePadraoDias) {
        this.validadePadraoDias = validadePadraoDias;
    }

    public Integer getTamanhoMaximoChapaLarguraMm() {
        return tamanhoMaximoChapaLarguraMm;
    }

    public void setTamanhoMaximoChapaLarguraMm(Integer tamanhoMaximoChapaLarguraMm) {
        this.tamanhoMaximoChapaLarguraMm = tamanhoMaximoChapaLarguraMm;
    }

    public Integer getTamanhoMaximoChapaAlturaMm() {
        return tamanhoMaximoChapaAlturaMm;
    }

    public void setTamanhoMaximoChapaAlturaMm(Integer tamanhoMaximoChapaAlturaMm) {
        this.tamanhoMaximoChapaAlturaMm = tamanhoMaximoChapaAlturaMm;
    }

    public Short getToleranciaPrumoNivelMm() {
        return toleranciaPrumoNivelMm;
    }

    public void setToleranciaPrumoNivelMm(Short toleranciaPrumoNivelMm) {
        this.toleranciaPrumoNivelMm = toleranciaPrumoNivelMm;
    }

    public RegimeTributario getRegimeTributario() {
        return regimeTributario;
    }

    public void setRegimeTributario(RegimeTributario regimeTributario) {
        this.regimeTributario = regimeTributario;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }
}
