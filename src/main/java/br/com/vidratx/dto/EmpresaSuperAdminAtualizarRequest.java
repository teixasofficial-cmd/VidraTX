package br.com.vidratx.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class EmpresaSuperAdminAtualizarRequest {

    @NotBlank(message = "Nome é obrigatório")
    @Size(
            max = 150,
            message = "Nome deve ter no máximo 150 caracteres"
    )
    private String nome;

    @NotBlank(message = "CNPJ é obrigatório")
    @Size(
            min = 14,
            max = 18,
            message = "CNPJ inválido"
    )
    private String cnpj;

    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail inválido")
    @Size(
            max = 150,
            message = "E-mail deve ter no máximo 150 caracteres"
    )
    private String email;

    @NotBlank(message = "Número é obrigatório")
    @Size(
            max = 20,
            message = "Número deve ter no máximo 20 caracteres"
    )
    private String telefone;

    @NotBlank(message = "Endereço é obrigatório")
    @Size(
            max = 255,
            message = "Endereço deve ter no máximo 255 caracteres"
    )
    private String endereco;

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
}
