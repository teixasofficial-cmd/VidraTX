package br.com.vidratx.entity;

import br.com.vidratx.enums.AcabamentoVidro;
import br.com.vidratx.enums.CorVidro;
import br.com.vidratx.enums.TipoVidro;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "orcamento_item")
public class OrcamentoItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "orcamento_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_orcamento_item_orcamento")
    )
    private Orcamento orcamento;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "tipologia_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_orcamento_item_tipologia")
    )
    private Tipologia tipologia;

    @Column(name = "ambiente", length = 100)
    private String ambiente;

    @Column(name = "largura_vao_mm")
    private Integer larguraVaoMm;

    @Column(name = "altura_vao_mm")
    private Integer alturaVaoMm;

    @Column(name = "largura_vao_2_mm")
    private Integer larguraVao2Mm;

    @Column(name = "altura_vao_2_mm")
    private Integer alturaVao2Mm;

    @Column(name = "medida_texto_original", length = 100)
    private String medidaTextoOriginal;

    @Column(name = "medida_aproximada", nullable = false)
    private Boolean medidaAproximada = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_vidro", length = 20)
    private TipoVidro tipoVidro;

    @Column(name = "espessura_mm")
    private Short espessuraMm;

    @Enumerated(EnumType.STRING)
    @Column(name = "cor", length = 20)
    private CorVidro cor;

    @Enumerated(EnumType.STRING)
    @Column(name = "acabamento", length = 20)
    private AcabamentoVidro acabamento;

    @Column(name = "cor_ferragem", length = 30)
    private String corFerragem;

    @Column(name = "quantidade", nullable = false)
    private Integer quantidade = 1;

    @Column(name = "observacoes", columnDefinition = "TEXT")
    private String observacoes;

    @Column(name = "ordem", nullable = false)
    private Integer ordem = 0;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    public OrcamentoItem() {
    }

    @PrePersist
    protected void aoCriar() {

        LocalDateTime agora = LocalDateTime.now();

        criadoEm = agora;
        atualizadoEm = agora;

        if (medidaAproximada == null) {
            medidaAproximada = false;
        }

        if (quantidade == null) {
            quantidade = 1;
        }

        if (ordem == null) {
            ordem = 0;
        }
    }

    @PreUpdate
    protected void aoAtualizar() {
        atualizadoEm = LocalDateTime.now();
    }

    public Long getId() {
        return id;
    }

    public Orcamento getOrcamento() {
        return orcamento;
    }

    public void setOrcamento(Orcamento orcamento) {
        this.orcamento = orcamento;
    }

    public Tipologia getTipologia() {
        return tipologia;
    }

    public void setTipologia(Tipologia tipologia) {
        this.tipologia = tipologia;
    }

    public String getAmbiente() {
        return ambiente;
    }

    public void setAmbiente(String ambiente) {
        this.ambiente = ambiente;
    }

    public Integer getLarguraVaoMm() {
        return larguraVaoMm;
    }

    public void setLarguraVaoMm(Integer larguraVaoMm) {
        this.larguraVaoMm = larguraVaoMm;
    }

    public Integer getAlturaVaoMm() {
        return alturaVaoMm;
    }

    public void setAlturaVaoMm(Integer alturaVaoMm) {
        this.alturaVaoMm = alturaVaoMm;
    }

    public Integer getLarguraVao2Mm() {
        return larguraVao2Mm;
    }

    public void setLarguraVao2Mm(Integer larguraVao2Mm) {
        this.larguraVao2Mm = larguraVao2Mm;
    }

    public Integer getAlturaVao2Mm() {
        return alturaVao2Mm;
    }

    public void setAlturaVao2Mm(Integer alturaVao2Mm) {
        this.alturaVao2Mm = alturaVao2Mm;
    }

    public String getMedidaTextoOriginal() {
        return medidaTextoOriginal;
    }

    public void setMedidaTextoOriginal(String medidaTextoOriginal) {
        this.medidaTextoOriginal = medidaTextoOriginal;
    }

    public Boolean getMedidaAproximada() {
        return medidaAproximada;
    }

    public void setMedidaAproximada(Boolean medidaAproximada) {
        this.medidaAproximada = medidaAproximada;
    }

    public TipoVidro getTipoVidro() {
        return tipoVidro;
    }

    public void setTipoVidro(TipoVidro tipoVidro) {
        this.tipoVidro = tipoVidro;
    }

    public Short getEspessuraMm() {
        return espessuraMm;
    }

    public void setEspessuraMm(Short espessuraMm) {
        this.espessuraMm = espessuraMm;
    }

    public CorVidro getCor() {
        return cor;
    }

    public void setCor(CorVidro cor) {
        this.cor = cor;
    }

    public AcabamentoVidro getAcabamento() {
        return acabamento;
    }

    public void setAcabamento(AcabamentoVidro acabamento) {
        this.acabamento = acabamento;
    }

    public String getCorFerragem() {
        return corFerragem;
    }

    public void setCorFerragem(String corFerragem) {
        this.corFerragem = corFerragem;
    }

    public Integer getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(Integer quantidade) {
        this.quantidade = quantidade;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }

    public Integer getOrdem() {
        return ordem;
    }

    public void setOrdem(Integer ordem) {
        this.ordem = ordem;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }
}
