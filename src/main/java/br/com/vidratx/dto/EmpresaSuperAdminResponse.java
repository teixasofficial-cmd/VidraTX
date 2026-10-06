package br.com.vidratx.dto;

import java.time.LocalDateTime;

public class EmpresaSuperAdminResponse {

    private Long id;
    private String nome;
    private String cnpj;
    private String slug;
    private String email;
    private String telefone;
    private String endereco;
    private Boolean ativa;
    private long totalUsuarios;
    private LocalDateTime criadoEm;
    private String whatsappStatus;
    private LocalDateTime whatsappDesconectadoDesde;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getCnpj() {
        return cnpj;
    }

    public void setCnpj(String cnpj) {
        this.cnpj = cnpj;
    }

    public String getSlug() {
        return slug;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public Boolean getAtiva() {
        return ativa;
    }

    public void setAtiva(Boolean ativa) {
        this.ativa = ativa;
    }

    public long getTotalUsuarios() {
        return totalUsuarios;
    }

    public void setTotalUsuarios(long totalUsuarios) {
        this.totalUsuarios = totalUsuarios;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }

    public String getWhatsappStatus() {
        return whatsappStatus;
    }

    public void setWhatsappStatus(String whatsappStatus) {
        this.whatsappStatus = whatsappStatus;
    }

    public LocalDateTime getWhatsappDesconectadoDesde() {
        return whatsappDesconectadoDesde;
    }

    public void setWhatsappDesconectadoDesde(LocalDateTime whatsappDesconectadoDesde) {
        this.whatsappDesconectadoDesde = whatsappDesconectadoDesde;
    }
}
