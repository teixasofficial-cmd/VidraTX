package br.com.vidratx.dto;

import br.com.vidratx.enums.MotivoPerda;
import br.com.vidratx.enums.StatusOrcamento;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class MoverPipelineRequest {

    @NotNull(message = "Novo status é obrigatório")
    private StatusOrcamento status;

    private MotivoPerda motivoPerda;

    @Size(max = 255, message = "Motivo deve ter no máximo 255 caracteres")
    private String motivoPerdaOutro;

    public StatusOrcamento getStatus() {
        return status;
    }

    public void setStatus(StatusOrcamento status) {
        this.status = status;
    }

    public MotivoPerda getMotivoPerda() {
        return motivoPerda;
    }

    public void setMotivoPerda(MotivoPerda motivoPerda) {
        this.motivoPerda = motivoPerda;
    }

    public String getMotivoPerdaOutro() {
        return motivoPerdaOutro;
    }

    public void setMotivoPerdaOutro(String motivoPerdaOutro) {
        this.motivoPerdaOutro = motivoPerdaOutro;
    }
}
