package br.com.vidratx.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Entity
@Table(
        name = "whatsapp_contato",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_whatsapp_contato_empresa_telefone",
                columnNames = {"empresa_id", "telefone"}
        )
)
public class WhatsappContato {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "empresa_id", nullable = false,
            foreignKey = @ForeignKey(name = "fk_whatsapp_contato_empresa"))
    private Empresa empresa;

    @Column(name = "telefone", nullable = false, length = 20)
    private String telefone;

    @Column(name = "pergunta_foco_id")
    private Long perguntaFocoId;

    @Column(name = "escolha_pendente", nullable = false)
    private Boolean escolhaPendente = false;

    @Column(name = "tentativas_escolha", nullable = false)
    private Integer tentativasEscolha = 0;

    @Column(name = "escolha_ids", length = 255)
    private String escolhaIds;

    @Column(name = "criado_em", nullable = false, updatable = false)
    private LocalDateTime criadoEm;

    @Column(name = "atualizado_em", nullable = false)
    private LocalDateTime atualizadoEm;

    public WhatsappContato() {
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

    public void limparEscolha() {
        perguntaFocoId = null;
        escolhaPendente = false;
        tentativasEscolha = 0;
        escolhaIds = null;
    }

    public void definirEscolha(List<Long> idsNaOrdem) {
        perguntaFocoId = null;
        escolhaPendente = true;
        escolhaIds = idsNaOrdem.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    public List<Long> idsDaEscolha() {

        if (escolhaIds == null || escolhaIds.isBlank()) {
            return List.of();
        }

        return Arrays.stream(escolhaIds.split(",")).map(String::trim).map(Long::valueOf).toList();
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

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public Long getPerguntaFocoId() {
        return perguntaFocoId;
    }

    public void setPerguntaFocoId(Long perguntaFocoId) {
        this.perguntaFocoId = perguntaFocoId;
    }

    public Boolean getEscolhaPendente() {
        return escolhaPendente;
    }

    public void setEscolhaPendente(Boolean escolhaPendente) {
        this.escolhaPendente = escolhaPendente;
    }

    public Integer getTentativasEscolha() {
        return tentativasEscolha;
    }

    public void setTentativasEscolha(Integer tentativasEscolha) {
        this.tentativasEscolha = tentativasEscolha;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public LocalDateTime getAtualizadoEm() {
        return atualizadoEm;
    }
}
