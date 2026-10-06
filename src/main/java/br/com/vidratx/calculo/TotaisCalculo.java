package br.com.vidratx.calculo;

import java.math.BigDecimal;

public record TotaisCalculo(
        long custoTotalCentavos,
        long precoSugeridoCentavos,
        long precoExibidoCentavos
) {

    public BigDecimal margemReal(long precoFinalCentavos, ParametrosCalculoInput parametros) {
        return MotorCalculoOrcamento.calcularMargemReal(precoFinalCentavos, custoTotalCentavos, parametros);
    }
}
