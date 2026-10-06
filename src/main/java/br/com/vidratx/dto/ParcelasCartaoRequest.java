package br.com.vidratx.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

public class ParcelasCartaoRequest {

    @Min(value = 1, message = "Parcelas devem ser pelo menos 1")
    @Max(value = 24, message = "Parcelas devem ser no máximo 24")
    private Integer parcelas;

    public Integer getParcelas() {
        return parcelas;
    }

    public void setParcelas(Integer parcelas) {
        this.parcelas = parcelas;
    }
}
