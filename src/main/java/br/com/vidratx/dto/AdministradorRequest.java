package br.com.vidratx.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class AdministradorRequest {

    @NotBlank(message = "Nome do administrador é obrigatório")
    @Size(
            max = 150,
            message = "Nome deve ter no máximo 150 caracteres"
    )
    private String nome;

    @NotBlank(message = "E-mail do administrador é obrigatório")
    @Email(message = "E-mail do administrador inválido")
    @Size(
            max = 150,
            message = "E-mail deve ter no máximo 150 caracteres"
    )
    private String email;

    @NotBlank(message = "Senha do administrador é obrigatória")
    @Size(
            min = 8,
            max = 100,
            message = "Senha deve ter entre 8 e 100 caracteres"
    )
    private String senha;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }

}
