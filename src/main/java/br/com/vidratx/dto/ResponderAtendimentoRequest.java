package br.com.vidratx.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class ResponderAtendimentoRequest {

    @NotBlank(message = "Mensagem é obrigatória")
    @Size(
            max = 2000,
            message = "Mensagem deve ter no máximo 2000 caracteres"
    )
    private String mensagem;

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }
}
