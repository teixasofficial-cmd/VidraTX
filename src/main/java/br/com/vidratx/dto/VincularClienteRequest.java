package br.com.vidratx.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

public class VincularClienteRequest {

    @NotNull(message = "Informe o cliente")
    @Positive(message = "Cliente inválido")
    private Long clienteId;

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }
}
