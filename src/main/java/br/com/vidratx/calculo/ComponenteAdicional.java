package br.com.vidratx.calculo;

import br.com.vidratx.enums.OrigemSugestao;
import br.com.vidratx.enums.TipoComponenteCusto;

import java.math.BigDecimal;

public record ComponenteAdicional(
        TipoComponenteCusto tipo,
        String descricao,
        BigDecimal quantidade,
        long valorUnitarioCentavos,
        OrigemSugestao origemSugestao,
        Long tabelaPrecoId
) {
}
