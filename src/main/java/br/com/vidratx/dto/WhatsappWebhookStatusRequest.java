package br.com.vidratx.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class WhatsappWebhookStatusRequest {

    @NotBlank(message = "Id da mensagem é obrigatório")
    @Size(max = 128, message = "Id da mensagem deve ter no máximo 128 caracteres")
    private String mensagemId;

    @NotBlank(message = "Status é obrigatório")
    @Size(max = 20, message = "Status inválido")
    private String status;

    public String getMensagemId() {
        return mensagemId;
    }

    public void setMensagemId(String mensagemId) {
        this.mensagemId = mensagemId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
