package br.com.vidratx.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class MaterialRequest {

    @NotBlank(message = "Nome é obrigatório")
    @Size(
            max = 150,
            message = "Nome deve ter no máximo 150 caracteres"
    )
    private String nome;

    @Size(
            max = 2000,
            message = "Descrição deve ter no máximo 2000 caracteres"
    )
    private String descricao;

    private Boolean ativo = true;

    public String getNome() {
        return nome;
    }

    public void setNome(String nome) {
        this.nome = nome;
    }

    public String getDescricao() {
        return descricao;
    }

    public void setDescricao(String descricao) {
        this.descricao = descricao;
    }

    public Boolean getAtivo() {
        return ativo;
    }

    public void setAtivo(Boolean ativo) {
        this.ativo = ativo;
    }
}
