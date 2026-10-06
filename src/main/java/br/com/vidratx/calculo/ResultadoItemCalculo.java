package br.com.vidratx.calculo;

import java.util.List;

public record ResultadoItemCalculo(
        List<PecaCalculada> pecas,
        List<LinhaCalculada> linhas,
        long custoItemCentavos,
        List<AlertaCalculo> alertas
) {
}
