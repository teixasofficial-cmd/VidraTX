package br.com.vidratx.entity;

import br.com.vidratx.enums.TipoFoto;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "foto")
public class Foto {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "orcamento_id",
            nullable = false,
            foreignKey = @ForeignKey(
                    name = "fk_foto_orcamento"
            )
    )
    private Orcamento orcamento;

    @Enumerated(EnumType.STRING)
    @Column(
            name = "tipo",
            nullable = false,
            length = 20
    )
    private TipoFoto tipo;

    @Column(
            name = "url",
            nullable = false,
            length = 500
    )
    private String url;

    @Column(
            name = "descricao",
            length = 500
    )
    private String descricao;

    @Column(
            name = "criado_em",
            nullable = false,
            updatable = false
    )
    private LocalDateTime criadoEm;

    public Foto() {
    }

    @PrePersist
    protected void aoCriar() {
        criadoEm = LocalDateTime.now();
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

    public TipoFoto getTipo() {
        return tipo;
    }

    public void setTipo(TipoFoto tipo) {
        this.tipo = tipo;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
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
}
