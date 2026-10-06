package br.com.vidratx.calculo;

import br.com.vidratx.enums.OrigemSugestao;
import br.com.vidratx.enums.TipoComponenteCusto;

import java.math.BigDecimal;

public record LinhaCalculada(
        TipoComponenteCusto tipo,
        String descricao,
        BigDecimal quantidade,
        Long valorUnitarioCentavos,
        long valorSugeridoCentavos,
        OrigemSugestao origemSugestao,
        Long tabelaPrecoId,
        String descricaoComponente,
        BigDecimal quantidadeComponente
) {

    public LinhaCalculada(
            TipoComponenteCusto tipo,
            String descricao,
            BigDecimal quantidade,
            Long valorUnitarioCentavos,
            long valorSugeridoCentavos,
            OrigemSugestao origemSugestao,
            Long tabelaPrecoId) {

        this(tipo, descricao, quantidade, valorUnitarioCentavos, valorSugeridoCentavos,
                origemSugestao, tabelaPrecoId, null, null);
    }
}
