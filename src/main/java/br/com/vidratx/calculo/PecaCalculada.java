package br.com.vidratx.calculo;

public record PecaCalculada(
        String descricao,
        int larguraCorteMm,
        int alturaCorteMm,
        int larguraCobradaMm,
        int alturaCobradaMm,
        int quantidade,
        boolean excedeTamanhoMaximo
) {

    public java.math.BigDecimal areaM2() {

        return java.math.BigDecimal.valueOf((long) larguraCobradaMm * alturaCobradaMm)
                .divide(java.math.BigDecimal.valueOf(1_000_000L), 6, java.math.RoundingMode.HALF_UP);
    }

    public java.math.BigDecimal perimetroM() {

        return java.math.BigDecimal.valueOf(2L * (larguraCobradaMm + alturaCobradaMm))
                .divide(java.math.BigDecimal.valueOf(1_000L), 6, java.math.RoundingMode.HALF_UP);
    }
}
