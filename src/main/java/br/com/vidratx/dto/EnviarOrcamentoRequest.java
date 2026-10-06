package br.com.vidratx.dto;

import br.com.vidratx.enums.DecisaoMedicaoPendente;
import java.math.BigDecimal;

public class EnviarOrcamentoRequest {

    private BigDecimal valorEsperado;

    private DecisaoMedicaoPendente medicaoPendente;

    public DecisaoMedicaoPendente getMedicaoPendente() {
        return medicaoPendente;
    }

    public void setMedicaoPendente(DecisaoMedicaoPendente medicaoPendente) {
        this.medicaoPendente = medicaoPendente;
    }

    public BigDecimal getValorEsperado() {
        return valorEsperado;
    }

    public void setValorEsperado(BigDecimal valorEsperado) {
        this.valorEsperado = valorEsperado;
    }
}
