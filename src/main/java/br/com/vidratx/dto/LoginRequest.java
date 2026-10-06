package br.com.vidratx.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LoginRequest {

    @NotBlank(message = "Identificador da empresa é obrigatório")
    @Size(
            max = 60,
            message = "Identificador da empresa inválido"
    )
    private String empresaSlug;

    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail inválido")
    private String email;

    @NotBlank(message = "Senha é obrigatória")
    private String senha;

    public String getEmpresaSlug() {
        return empresaSlug;
    }

    public void setEmpresaSlug(String empresaSlug) {
        this.empresaSlug = empresaSlug;
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
