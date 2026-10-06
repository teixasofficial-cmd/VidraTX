package br.com.vidratx.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class InstalacaoReagendarRequest {

    @NotNull(message = "Data agendada é obrigatória")
    private LocalDateTime dataAgendada;

    @Size(
            max = 150,
            message = "Equipe/responsável deve ter no máximo 150 caracteres"
    )
    private String equipeResponsavel;

    public LocalDateTime getDataAgendada() {
        return dataAgendada;
    }

    public void setDataAgendada(LocalDateTime dataAgendada) {
        this.dataAgendada = dataAgendada;
    }

    public String getEquipeResponsavel() {
        return equipeResponsavel;
    }

    public void setEquipeResponsavel(String equipeResponsavel) {
        this.equipeResponsavel = equipeResponsavel;
    }
}
