package br.com.vidratx.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class OnboardingRequest {

    @NotBlank(message = "Razão social é obrigatória")
    @Size(
            max = 150,
            message = "Razão social deve ter no máximo 150 caracteres"
    )
    private String razaoSocial;

    @NotBlank(message = "Nome fantasia é obrigatório")
    @Size(
            max = 150,
            message = "Nome fantasia deve ter no máximo 150 caracteres"
    )
    private String nomeFantasia;

    @NotBlank(message = "CNPJ é obrigatório")
    @Pattern(
            regexp = "^(\\d{14}|\\d{2}\\.\\d{3}\\.\\d{3}/\\d{4}-\\d{2})$",
            message = "CNPJ inválido"
    )
    private String cnpj;

    @Size(
            max = 60,
            message = "Identificador deve ter no máximo 60 caracteres"
    )
    private String slug;

    @Email(message = "E-mail da empresa inválido")
    @Size(
            max = 150,
            message = "E-mail deve ter no máximo 150 caracteres"
    )
    private String emailEmpresa;

    @Size(
            max = 20,
            message = "Telefone deve ter no máximo 20 caracteres"
    )
    private String telefone;

    @Valid
    private AdministradorRequest administrador;

    public String getRazaoSocial() {
        return razaoSocial;
    }

    public void setRazaoSocial(String razaoSocial) {
        this.razaoSocial = razaoSocial;
    }

    public String getNomeFantasia() {
        return nomeFantasia;
    }

    public void setNomeFantasia(String nomeFantasia) {
        this.nomeFantasia = nomeFantasia;
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

    public String getEmailEmpresa() {
        return emailEmpresa;
    }

    public void setEmailEmpresa(String emailEmpresa) {
        this.emailEmpresa = emailEmpresa;
    }

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public AdministradorRequest getAdministrador() {
        return administrador;
    }

    public void setAdministrador(
            AdministradorRequest administrador) {

        this.administrador = administrador;
    }

}
