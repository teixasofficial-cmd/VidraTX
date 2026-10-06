package br.com.vidratx.calculo;

import br.com.vidratx.enums.ArredondamentoComercial;
import br.com.vidratx.enums.OrigemSugestao;
import br.com.vidratx.enums.TipoComponenteCusto;
import br.com.vidratx.enums.TipoVidro;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MotorCalculoOrcamentoTest {

    private static ParametrosCalculoInput parametrosTeste1() {

        return new ParametrosCalculoInput(
                50,
                new BigDecimal("0.25"),
                new BigDecimal("3.00"),
                new BigDecimal("6.00"),
                new BigDecimal("3.00"),
                new BigDecimal("0.00"),
                new BigDecimal("25.00"),
                new BigDecimal("15.00"),
                ArredondamentoComercial.PROXIMA_DEZENA,
                null,
                null
        );
    }

    private static TipologiaRegras boxFrontal2Folhas() {

        return new TipologiaRegras(
                2, FormulaPecas.DIVIDIR_LARGURA_IGUAL, 12, 0, 0, List.of()
        );
    }

    @Test
    void boxFrontal2FolhasCalculaPecasCustoPrecoEMargemComoNoEnunciado() {

        ParametrosCalculoInput parametros = parametrosTeste1();

        ItemCalculoInput item = new ItemCalculoInput(
                boxFrontal2Folhas(),
                1200, 1900, null, null,
                1,
                15_000L,
                null,
                TipoVidro.TEMPERADO,
                List.of(
                        new ComponenteAdicional(
                                TipoComponenteCusto.KIT, "Kit box", BigDecimal.ONE, 12_000L,
                                OrigemSugestao.TABELA, null
                        ),
                        new ComponenteAdicional(
                                TipoComponenteCusto.MAO_DE_OBRA, "Mão de obra", BigDecimal.ONE, 10_000L,
                                OrigemSugestao.TABELA, null
                        ),
                        new ComponenteAdicional(
                                TipoComponenteCusto.DESLOCAMENTO, "Deslocamento", BigDecimal.ONE, 3_000L,
                                OrigemSugestao.TABELA, null
                        )
                )
        );

        ResultadoItemCalculo resultado = MotorCalculoOrcamento.calcularItem(item, parametros);

        assertEquals(2, resultado.pecas().size());
        assertEquals(594, resultado.pecas().get(0).larguraCorteMm());
        assertEquals(1900, resultado.pecas().get(0).alturaCorteMm());
        assertEquals(600, resultado.pecas().get(0).larguraCobradaMm());
        assertEquals(1900, resultado.pecas().get(0).alturaCobradaMm());
        assertEquals(594, resultado.pecas().get(1).larguraCorteMm());
        assertEquals(600, resultado.pecas().get(1).larguraCobradaMm());
        assertFalse(resultado.pecas().get(0).excedeTamanhoMaximo());

        assertEquals(60_226L, resultado.custoItemCentavos());

        TotaisCalculo totais = MotorCalculoOrcamento.fecharTotais(resultado.custoItemCentavos(), parametros);

        assertEquals(91_252L, totais.precoSugeridoCentavos());
        assertEquals(92_000L, totais.precoExibidoCentavos());

        BigDecimal margemReal = MotorCalculoOrcamento.calcularMargemReal(
                totais.precoExibidoCentavos(), totais.custoTotalCentavos(), parametros
        );

        assertEquals(0, new BigDecimal("25.54").compareTo(margemReal));
    }

    @Test
    void espelhoAbaixoDaAreaMinimaCobraAreaMinimaPorPeca() {

        TipologiaRegras espelho = new TipologiaRegras(
                1, FormulaPecas.PECA_UNICA, 0, 0, 0, List.of()
        );

        ItemCalculoInput item = new ItemCalculoInput(
                espelho,
                300, 400, null, null,
                1,
                10_000L,
                null,
                TipoVidro.COMUM,
                List.of()
        );

        ParametrosCalculoInput parametros = new ParametrosCalculoInput(
                50, new BigDecimal("0.25"), BigDecimal.ZERO,
                BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO,
                ArredondamentoComercial.NENHUM, null, null
        );

        ResultadoItemCalculo resultado = MotorCalculoOrcamento.calcularItem(item, parametros);

        LinhaCalculada linhaVidro = resultado.linhas().stream()
                .filter(l -> l.tipo() == TipoComponenteCusto.VIDRO)
                .findFirst()
                .orElseThrow();

        assertEquals(0, new BigDecimal("0.25").compareTo(linhaVidro.quantidade()));
        assertEquals(2_500L, linhaVidro.valorSugeridoCentavos());
    }

    @Test
    void arredondaMedidaParaCimaNoMultiploConfigurado() {

        assertEquals(1500, br.com.vidratx.util.DinheiroUtils.arredondarParaCimaMultiplo(1460, 50));
        assertEquals(1900, br.com.vidratx.util.DinheiroUtils.arredondarParaCimaMultiplo(1900, 50));
        assertEquals(600, br.com.vidratx.util.DinheiroUtils.arredondarParaCimaMultiplo(594, 50));
    }

    @Test
    void alertaNormativoDisparaQuandoVidroNaoESeguranca() {

        TipologiaRegras boxComAlerta = new TipologiaRegras(
                1, FormulaPecas.PECA_UNICA, 0, 0, 0,
                List.of(AlertaCalculo.REQUER_VIDRO_SEGURANCA)
        );

        ItemCalculoInput itemComum = new ItemCalculoInput(
                boxComAlerta, 1000, 1000, null, null, 1,
                10_000L, null, TipoVidro.COMUM, List.of()
        );

        ParametrosCalculoInput parametros = parametrosTeste1();

        ResultadoItemCalculo resultado = MotorCalculoOrcamento.calcularItem(itemComum, parametros);

        assertTrue(resultado.alertas().stream()
                .anyMatch(a -> a.codigo().equals(AlertaCalculo.REQUER_VIDRO_SEGURANCA)));

        ItemCalculoInput itemTemperado = new ItemCalculoInput(
                boxComAlerta, 1000, 1000, null, null, 1,
                10_000L, null, TipoVidro.TEMPERADO, List.of()
        );

        ResultadoItemCalculo resultadoOk = MotorCalculoOrcamento.calcularItem(itemTemperado, parametros);

        assertFalse(resultadoOk.alertas().stream()
                .anyMatch(a -> a.codigo().equals(AlertaCalculo.REQUER_VIDRO_SEGURANCA)));
    }

    @Test
    void quantidadeMultiplicaVidroEComponentesMasNaoODeslocamento() {

        ItemCalculoInput item = new ItemCalculoInput(
                boxFrontal2Folhas(),
                1200, 1900, null, null,
                3,
                15_000L,
                null,
                TipoVidro.TEMPERADO,
                List.of(
                        new ComponenteAdicional(TipoComponenteCusto.KIT, "Kit box", BigDecimal.ONE, 12_000L,
                                OrigemSugestao.TABELA, null),
                        new ComponenteAdicional(TipoComponenteCusto.DESLOCAMENTO, "Deslocamento", BigDecimal.ONE, 3_000L,
                                OrigemSugestao.TABELA, null)
                )
        );

        ResultadoItemCalculo resultado = MotorCalculoOrcamento.calcularItem(item, parametrosTeste1());

        LinhaCalculada vidro = resultado.linhas().stream()
                .filter(l -> l.tipo() == TipoComponenteCusto.VIDRO).findFirst().orElseThrow();
        LinhaCalculada kit = resultado.linhas().stream()
                .filter(l -> l.tipo() == TipoComponenteCusto.KIT).findFirst().orElseThrow();
        LinhaCalculada deslocamento = resultado.linhas().stream()
                .filter(l -> l.tipo() == TipoComponenteCusto.DESLOCAMENTO).findFirst().orElseThrow();

        assertEquals(vidro.valorSugeridoCentavos(),
                vidro.quantidade().multiply(BigDecimal.valueOf(vidro.valorUnitarioCentavos()))
                        .setScale(0, java.math.RoundingMode.HALF_UP).longValueExact());

        assertEquals(36_000L, kit.valorSugeridoCentavos());
        assertEquals("Kit box", kit.descricaoComponente());
        assertEquals(0, BigDecimal.ONE.compareTo(kit.quantidadeComponente()));
        assertEquals(3_000L, deslocamento.valorSugeridoCentavos());
    }

    @Test
    void modoVendaNaoReaplicaMargemSobrePrecoDeVenda() {

        ParametrosCalculoInput base = parametrosTeste1();

        ParametrosCalculoInput venda = new ParametrosCalculoInput(
                base.multiploArredondamentoMm(), base.areaMinimaM2(), base.percentualPerdas(),
                base.percentualImpostos(), base.percentualTaxaCartao(), base.percentualComissao(),
                base.percentualMargemDesejada(), base.percentualMargemMinima(),
                br.com.vidratx.enums.ArredondamentoComercial.NENHUM,
                null, null, br.com.vidratx.enums.ModoPrecificacao.VENDA
        );

        TotaisCalculo totais = MotorCalculoOrcamento.fecharTotais(100_000L, venda);

        assertEquals(100_000L, totais.precoSugeridoCentavos());
        assertTrue(MotorCalculoOrcamento.fecharTotais(100_000L, base).precoSugeridoCentavos() > 100_000L);
    }

    @Test
    void transpasseAlargaAsFolhasDoBoxDeCorrer() {

        ItemCalculoInput item = new ItemCalculoInput(
                new TipologiaRegras(2, FormulaPecas.DIVIDIR_LARGURA_IGUAL, 12, 0, 50, List.of()),
                1200, 1900, null, null, 1, 15_000L, null, TipoVidro.TEMPERADO, List.of()
        );

        ResultadoItemCalculo resultado = MotorCalculoOrcamento.calcularItem(item, parametrosTeste1());

        assertEquals(619, resultado.pecas().get(0).larguraCorteMm());
        assertEquals(650, resultado.pecas().get(0).larguraCobradaMm());
        assertEquals(new BigDecimal("2.470000"),
                resultado.pecas().get(0).areaM2().add(resultado.pecas().get(1).areaM2()));
    }

    @Test
    void boxDeCantoDivideFrenteELateralComTranspasse() {

        ItemCalculoInput item = new ItemCalculoInput(
                new TipologiaRegras(4, FormulaPecas.DOIS_VAOS_DIVIDIDOS, 12, 0, 50, List.of()),
                1000, 1900, 800, 1900, 1, 15_000L, null, TipoVidro.TEMPERADO, List.of()
        );

        List<PecaCalculada> pecas = MotorCalculoOrcamento.calcularItem(item, parametrosTeste1()).pecas();

        assertEquals(4, pecas.size());
        assertEquals(519, pecas.get(0).larguraCorteMm());
        assertEquals(519, pecas.get(1).larguraCorteMm());
        assertEquals(419, pecas.get(2).larguraCorteMm());
        assertEquals(419, pecas.get(3).larguraCorteMm());
        assertEquals(550, pecas.get(0).larguraCobradaMm());
        assertEquals(450, pecas.get(2).larguraCobradaMm());
    }

    @Test
    void limiteDaChapaUsaAMedidaDeCorte() {

        ParametrosCalculoInput base = parametrosTeste1();
        ParametrosCalculoInput comChapa = new ParametrosCalculoInput(
                base.multiploArredondamentoMm(), base.areaMinimaM2(), base.percentualPerdas(),
                base.percentualImpostos(), base.percentualTaxaCartao(), base.percentualComissao(),
                base.percentualMargemDesejada(), base.percentualMargemMinima(),
                base.arredondamentoComercial(), 2000, 2000
        );

        ItemCalculoInput cabe = new ItemCalculoInput(
                new TipologiaRegras(1, FormulaPecas.PECA_UNICA, 0, 0, 0, List.of()),
                1990, 1990, null, null, 1, 15_000L, null, TipoVidro.TEMPERADO, List.of()
        );
        ItemCalculoInput naoCabe = new ItemCalculoInput(
                new TipologiaRegras(1, FormulaPecas.PECA_UNICA, 0, 0, 0, List.of()),
                2010, 1990, null, null, 1, 15_000L, null, TipoVidro.TEMPERADO, List.of()
        );

        assertFalse(MotorCalculoOrcamento.calcularItem(cabe, comChapa).pecas().get(0).excedeTamanhoMaximo());
        assertTrue(MotorCalculoOrcamento.calcularItem(naoCabe, comChapa).pecas().get(0).excedeTamanhoMaximo());
    }
}
