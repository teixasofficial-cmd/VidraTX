package br.com.vidratx.calculo;

import br.com.vidratx.enums.TipoVidro;

import java.util.List;

public record ItemCalculoInput(
        TipologiaRegras tipologia,
        Integer larguraVaoMm,
        Integer alturaVaoMm,
        Integer larguraVao2Mm,
        Integer alturaVao2Mm,
        int quantidade,
        Long precoVidroM2Centavos,
        Long custoVidroM2Centavos,
        TipoVidro tipoVidro,
        List<ComponenteAdicional> componentesAdicionais
) {

    public ItemCalculoInput {
        componentesAdicionais = componentesAdicionais == null ? List.of() : List.copyOf(componentesAdicionais);
    }

    public boolean vidroSeguranca() {
        return tipoVidro == TipoVidro.TEMPERADO || tipoVidro == TipoVidro.LAMINADO;
    }

    public boolean vidroLaminado() {
        return tipoVidro == TipoVidro.LAMINADO;
    }
}
