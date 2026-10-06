package br.com.vidratx.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class EmpresaRequest {

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
    @Size(
            min = 14,
            max = 18,
            message = "CNPJ inválido"
    )
    private String cnpj;

    @Email(message = "E-mail inválido")
    @Size(
            max = 150,
            message = "E-mail deve ter no máximo 150 caracteres"
    )
    private String email;

    @Size(
            max = 20,
            message = "Telefone deve ter no máximo 20 caracteres"
    )
    private String telefone;

    @Size(
            max = 20,
            message = "WhatsApp deve ter no máximo 20 caracteres"
    )
    private String whatsapp;

    @Size(
            max = 500,
            message = "URL do logo deve ter no máximo 500 caracteres"
    )
    private String logoUrl;

    @Pattern(
            regexp = "^#[0-9A-Fa-f]{6}$",
            message = "Cor primária deve estar no formato hexadecimal, ex: #1A73E8"
    )
    private String corPrimaria;

    @Pattern(
            regexp = "^#[0-9A-Fa-f]{6}$",
            message = "Cor secundária deve estar no formato hexadecimal, ex: #1A73E8"
    )
    private String corSecundaria;

    @Size(
            max = 255,
            message = "Endereço deve ter no máximo 255 caracteres"
    )
    private String endereco;

    @Size(
            max = 2000,
            message = "Texto sobre a empresa deve ter no máximo 2000 caracteres"
    )
    private String sobre;

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

    public String getWhatsapp() {
        return whatsapp;
    }

    public void setWhatsapp(String whatsapp) {
        this.whatsapp = whatsapp;
    }

    public String getLogoUrl() {
        return logoUrl;
    }

    public void setLogoUrl(String logoUrl) {
        this.logoUrl = logoUrl;
    }

    public String getCorPrimaria() {
        return corPrimaria;
    }

    public void setCorPrimaria(String corPrimaria) {
        this.corPrimaria = corPrimaria;
    }

    public String getCorSecundaria() {
        return corSecundaria;
    }

    public void setCorSecundaria(String corSecundaria) {
        this.corSecundaria = corSecundaria;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getSobre() {
        return sobre;
    }

    public void setSobre(String sobre) {
        this.sobre = sobre;
    }
}
