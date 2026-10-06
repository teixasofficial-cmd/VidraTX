package br.com.vidratx.util;

import br.com.vidratx.enums.ArredondamentoComercial;

import java.math.BigDecimal;
import java.math.RoundingMode;

public final class DinheiroUtils {

    private DinheiroUtils() {
    }

    public static long paraCentavos(BigDecimal reais) {

        if (reais == null) {
            return 0L;
        }

        return reais.setScale(2, RoundingMode.HALF_UP)
                .movePointRight(2)
                .longValueExact();
    }

    public static BigDecimal paraReais(Long centavos) {

        if (centavos == null) {
            return BigDecimal.ZERO;
        }

        return BigDecimal.valueOf(centavos)
                .movePointLeft(2)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public static long aplicarPercentual(long baseCentavos, BigDecimal percentual) {

        if (percentual == null || percentual.signum() == 0) {
            return 0L;
        }

        BigDecimal resultado = BigDecimal.valueOf(baseCentavos)
                .multiply(percentual)
                .divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP);

        return arredondarParaLong(resultado);
    }

    public static long dividirPorFatorRestante(long baseCentavos, BigDecimal somaPercentuais) {

        BigDecimal fatorRestante = BigDecimal.ONE.subtract(
                somaPercentuais.divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
        );

        if (fatorRestante.signum() <= 0) {

            throw new IllegalArgumentException(
                    "A soma de impostos, taxa de cartão, comissão e margem não pode chegar a 100% ou mais"
            );
        }

        BigDecimal resultado = BigDecimal.valueOf(baseCentavos)
                .divide(fatorRestante, 6, RoundingMode.HALF_UP);

        return arredondarParaLong(resultado);
    }

    public static long arredondarComercial(long centavos, ArredondamentoComercial modo) {

        return switch (modo) {

            case NENHUM -> centavos;

            case PROXIMA_DEZENA -> arredondarParaCimaMultiplo(centavos, 1_000L);

            case PROXIMA_CENTENA -> arredondarParaCimaMultiplo(centavos, 10_000L);

            case TERMINAR_90 -> {

                long baseDezena = arredondarParaCimaMultiplo(centavos + 10L, 1_000L);
                yield baseDezena - 10L;
            }
        };
    }

    public static int arredondarParaCimaMultiplo(int valor, int multiplo) {
        return (int) arredondarParaCimaMultiplo((long) valor, (long) multiplo);
    }

    public static long arredondarParaCimaMultiplo(long valor, long multiplo) {

        if (multiplo <= 0) {
            return valor;
        }

        long resto = valor % multiplo;

        if (resto == 0) {
            return valor;
        }

        return valor + (multiplo - resto);
    }

    private static long arredondarParaLong(BigDecimal valor) {
        return valor.setScale(0, RoundingMode.HALF_UP).longValueExact();
    }
}
