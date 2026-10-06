package br.com.vidratx.dto;

import br.com.vidratx.enums.MotivoPerda;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class PerderOrcamentoRequest {

    @NotNull(message = "Motivo da perda é obrigatório")
    private MotivoPerda motivoPerda;

    @Size(max = 255, message = "Motivo deve ter no máximo 255 caracteres")
    private String motivoPerdaOutro;

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
