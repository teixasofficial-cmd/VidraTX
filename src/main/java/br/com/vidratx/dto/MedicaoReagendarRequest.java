package br.com.vidratx.dto;

import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

public class MedicaoReagendarRequest {

    @NotNull(message = "Data agendada é obrigatória")
    private LocalDateTime dataAgendada;

    public LocalDateTime getDataAgendada() {
        return dataAgendada;
    }

    public void setDataAgendada(LocalDateTime dataAgendada) {
        this.dataAgendada = dataAgendada;
    }
}
