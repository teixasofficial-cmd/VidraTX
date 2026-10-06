package br.com.vidratx.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class RedefinirSenhaSuperAdminRequest {

    @NotBlank(message = "Senha é obrigatória")
    @Size(
            min = 8,
            message = "Senha deve ter no mínimo 8 caracteres"
    )
    private String senha;

    public String getSenha() {
        return senha;
    }

    public void setSenha(String senha) {
        this.senha = senha;
    }
}
