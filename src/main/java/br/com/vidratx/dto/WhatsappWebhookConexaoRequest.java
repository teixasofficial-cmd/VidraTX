package br.com.vidratx.dto;

import br.com.vidratx.enums.StatusInstanciaWhatsapp;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class WhatsappWebhookConexaoRequest {

    @NotNull(message = "Status é obrigatório")
    private StatusInstanciaWhatsapp status;

    @Size(
            max = 20,
            message = "Número deve ter no máximo 20 caracteres"
    )
    private String numero;

    public StatusInstanciaWhatsapp getStatus() {
        return status;
    }

    public void setStatus(StatusInstanciaWhatsapp status) {
        this.status = status;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }
}
