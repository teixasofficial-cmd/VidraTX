package br.com.vidratx.entity;

import br.com.vidratx.enums.CategoriaTipologia;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "tipologia",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_tipologia_empresa_codigo",
                        columnNames = { "empresa_id", "codigo" }
                )
        }
)
public class Tipologia {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "empresa_id",
            nullable = false,
            foreignKey = @ForeignKey(name = "fk_tipologia_empresa")
    )
    private Empresa empresa;

    @Column(name = "codigo", nullable = false, length = 40)
    private String codigo;

    @Column(name = "nome", nullable = false, length = 100)
    private String nome;

    @Enumerated(EnumType.STRING)
    @Column(name = "categoria", nullable = false, length = 30)
    private CategoriaTipologia categoria;

    @Column(name = "regras_json", nullable = false, columnDefinition = "json")
    private String regrasJson;

    @Column(name = "ativo", nullable = false)
    private Boolean ativo = true;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    public Tipologia() {
    }

    @PrePersist
    protected void aoCriar() {

        LocalDateTime agora = LocalDateTime.now();

        criadoEm = agora;
        atualizadoEm = agora;

        if (ativo == null) {
            ativo = true;
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

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public CategoriaTipologia getCategoria() {
        return categoria;
    }

    public void setCategoria(CategoriaTipologia categoria) {
        this.categoria = categoria;
    }

    public String getRegrasJson() {
        return regrasJson;
    }

    public void setRegrasJson(String regrasJson) {
        this.regrasJson = regrasJson;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }
}
