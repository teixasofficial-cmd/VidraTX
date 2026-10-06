package br.com.vidratx.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ClienteRequest {

    @NotBlank(message = "Nome é obrigatório")
    @Size(
            max = 150,
            message = "Nome deve ter no máximo 150 caracteres"
    )
    private String nome;

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

    @Email(message = "E-mail inválido")
    @Size(
            max = 150,
            message = "E-mail deve ter no máximo 150 caracteres"
    )
    private String email;

    @Size(
            max = 18,
            message = "CPF/CNPJ inválido"
    )
    private String cpfCnpj;

    @Size(
            max = 255,
            message = "Endereço deve ter no máximo 255 caracteres"
    )
    private String endereco;

    @Size(
            max = 2000,
            message = "Observações devem ter no máximo 2000 caracteres"
    )
    private String observacoes;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getCpfCnpj() {
        return cpfCnpj;
    }

    public void setCpfCnpj(String cpfCnpj) {
        this.cpfCnpj = cpfCnpj;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
}
