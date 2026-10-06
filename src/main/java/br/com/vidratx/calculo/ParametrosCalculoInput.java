package br.com.vidratx.calculo;

import br.com.vidratx.enums.ArredondamentoComercial;
import br.com.vidratx.enums.ModoPrecificacao;

import java.math.BigDecimal;

public record ParametrosCalculoInput(
        int multiploArredondamentoMm,
        BigDecimal areaMinimaM2,
        BigDecimal percentualPerdas,
        BigDecimal percentualImpostos,
        BigDecimal percentualTaxaCartao,
        BigDecimal percentualComissao,
        BigDecimal percentualMargemDesejada,
        BigDecimal percentualMargemMinima,
        ArredondamentoComercial arredondamentoComercial,
        Integer tamanhoMaximoChapaLarguraMm,
        Integer tamanhoMaximoChapaAlturaMm,
        ModoPrecificacao modoPrecificacao
) {

    public ParametrosCalculoInput(
            int multiploArredondamentoMm,
            BigDecimal areaMinimaM2,
            BigDecimal percentualPerdas,
            BigDecimal percentualImpostos,
            BigDecimal percentualTaxaCartao,
            BigDecimal percentualComissao,
            BigDecimal percentualMargemDesejada,
            BigDecimal percentualMargemMinima,
            ArredondamentoComercial arredondamentoComercial,
            Integer tamanhoMaximoChapaLarguraMm,
            Integer tamanhoMaximoChapaAlturaMm) {

        this(multiploArredondamentoMm, areaMinimaM2, percentualPerdas, percentualImpostos, percentualTaxaCartao,
                percentualComissao, percentualMargemDesejada, percentualMargemMinima, arredondamentoComercial,
                tamanhoMaximoChapaLarguraMm, tamanhoMaximoChapaAlturaMm, ModoPrecificacao.CUSTO);
    }

    public boolean valoresSaoPrecoDeVenda() {
        return modoPrecificacao == ModoPrecificacao.VENDA;
    }

    public BigDecimal somaPercentuaisPrecificacao() {

        return percentualImpostos
                .add(percentualTaxaCartao)
                .add(percentualComissao)
                .add(percentualMargemDesejada);
    }

    public BigDecimal somaPercentuaisMargemReal() {

        return percentualImpostos
                .add(percentualTaxaCartao)
                .add(percentualComissao);
    }
}
