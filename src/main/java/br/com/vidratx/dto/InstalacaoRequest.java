package br.com.vidratx.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

public class InstalacaoRequest {

    @NotNull(message = "Data agendada é obrigatória")
    private LocalDateTime dataAgendada;

    @Size(
            max = 255,
            message = "Endereço deve ter no máximo 255 caracteres"
    )
    private String endereco;

    @Size(
            max = 150,
            message = "Equipe/responsável deve ter no máximo 150 caracteres"
    )
    private String equipeResponsavel;

    @Size(
            max = 2000,
            message = "Observações devem ter no máximo 2000 caracteres"
    )
    private String observacoes;

    public LocalDateTime getDataAgendada() {
        return dataAgendada;
    }

    public void setDataAgendada(LocalDateTime dataAgendada) {
        this.dataAgendada = dataAgendada;
    }

    public String getEndereco() {
        return endereco;
    }

    public void setEndereco(String endereco) {
        this.endereco = endereco;
    }

    public String getEquipeResponsavel() {
        return equipeResponsavel;
    }

    public void setEquipeResponsavel(String equipeResponsavel) {
        this.equipeResponsavel = equipeResponsavel;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
}
