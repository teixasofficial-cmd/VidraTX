package br.com.vidratx.dto;

import jakarta.validation.constraints.Size;

public class InstalacaoRealizarRequest {

    @Size(
            max = 2000,
            message = "Checklist deve ter no máximo 2000 caracteres"
    )
    private String checklist;

    @Size(
            max = 2000,
            message = "Observações devem ter no máximo 2000 caracteres"
    )
    private String observacoes;

    public String getChecklist() {
        return checklist;
    }

    public void setChecklist(String checklist) {
        this.checklist = checklist;
    }

    public String getObservacoes() {
        return observacoes;
    }

    public void setObservacoes(String observacoes) {
        this.observacoes = observacoes;
    }
}
