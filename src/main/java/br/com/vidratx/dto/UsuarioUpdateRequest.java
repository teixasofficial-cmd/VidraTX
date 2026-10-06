package br.com.vidratx.dto;

import br.com.vidratx.enums.Perfil;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class UsuarioUpdateRequest {

    @NotBlank(message = "Nome é obrigatório")
    @Size(
            max = 150,
            message = "Nome deve ter no máximo 150 caracteres"
    )
    private String nome;

    @NotBlank(message = "E-mail é obrigatório")
    @Email(message = "E-mail inválido")
    @Size(
            max = 150,
            message = "E-mail deve ter no máximo 150 caracteres"
    )
    private String email;

    @NotNull(message = "Perfil é obrigatório")
    private Perfil perfil;

    @NotNull(message = "Status do usuário é obrigatório")
    private Boolean ativo;

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

    public Perfil getPerfil() {
        return perfil;
    }

    public void setPerfil(Perfil perfil) {
        this.perfil = perfil;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
}
