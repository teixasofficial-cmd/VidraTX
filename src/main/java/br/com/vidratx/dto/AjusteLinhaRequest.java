package br.com.vidratx.dto;

import jakarta.validation.constraints.DecimalMin;

import java.math.BigDecimal;

public class AjusteLinhaRequest {

    @DecimalMin(value = "0.00", message = "Valor final não pode ser negativo")
    private BigDecimal valorFinal;

    public BigDecimal getValorFinal() {
        return valorFinal;
    }

    public void setValorFinal(BigDecimal valorFinal) {
        this.valorFinal = valorFinal;
    }
}
