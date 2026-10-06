package br.com.vidratx.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class RecusarContrapropostaRequest {

    @NotNull(message = "Informe a nova data proposta")
    private LocalDateTime dataAgendada;

    @Size(max = 300, message = "Motivo deve ter no máximo 300 caracteres")
    private String motivo;

    @Size(max = 150, message = "Equipe deve ter no máximo 150 caracteres")
    private String equipeResponsavel;

    public LocalDateTime getDataAgendada() {
        return dataAgendada;
    }

    public void setDataAgendada(LocalDateTime dataAgendada) {
        this.dataAgendada = dataAgendada;
    }

    public String getMotivo() {
        return motivo;
    }

    public void setMotivo(String motivo) {
        this.motivo = motivo;
    }

    public String getEquipeResponsavel() {
        return equipeResponsavel;
    }

    public void setEquipeResponsavel(String equipeResponsavel) {
        this.equipeResponsavel = equipeResponsavel;
    }
}
