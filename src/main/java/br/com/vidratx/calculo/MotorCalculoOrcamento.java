package br.com.vidratx.calculo;

import br.com.vidratx.enums.TipoComponenteCusto;
import br.com.vidratx.util.DinheiroUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

public final class MotorCalculoOrcamento {

    private MotorCalculoOrcamento() {
    }

    public static ResultadoItemCalculo calcularItem(
            ItemCalculoInput item,
            ParametrosCalculoInput parametros) {

        List<AlertaCalculo> alertas = new ArrayList<>();

        List<PecaCalculada> pecas = gerarPecas(item, parametros, alertas);

        List<LinhaCalculada> linhas = new ArrayList<>();
        long custoItemCentavos = 0L;

        if (!pecas.isEmpty()) {

            BigDecimal areaTotalCobradaM2 = BigDecimal.ZERO;

            for (PecaCalculada peca : pecas) {

                BigDecimal areaPeca = peca.areaM2().multiply(BigDecimal.valueOf(peca.quantidade()));
                BigDecimal areaMinimaPeca = parametros.areaMinimaM2()
                        .multiply(BigDecimal.valueOf(peca.quantidade()));

                areaTotalCobradaM2 = areaTotalCobradaM2.add(areaPeca.max(areaMinimaPeca));
            }

            if (item.precoVidroM2Centavos() != null) {

                BigDecimal areaTodasUnidadesM2 = areaTotalCobradaM2
                        .multiply(BigDecimal.valueOf(item.quantidade()));

                long valorVidroCentavos = arredondar(
                        areaTodasUnidadesM2.multiply(BigDecimal.valueOf(item.precoVidroM2Centavos()))
                );

                String descricaoVidro = item.quantidade() > 1
                        ? "Vidro (" + areaTotalCobradaM2.setScale(2, RoundingMode.HALF_UP) + " m² × "
                        + item.quantidade() + " unidades)"
                        : "Vidro (" + areaTotalCobradaM2.setScale(2, RoundingMode.HALF_UP) + " m²)";

                linhas.add(new LinhaCalculada(
                        TipoComponenteCusto.VIDRO,
                        descricaoVidro,
                        areaTodasUnidadesM2,
                        item.precoVidroM2Centavos(),
                        valorVidroCentavos,
                        br.com.vidratx.enums.OrigemSugestao.TABELA,
                        null
                ));

                custoItemCentavos += valorVidroCentavos;

                long perdasCentavos = DinheiroUtils.aplicarPercentual(
                        valorVidroCentavos, parametros.percentualPerdas()
                );

                if (perdasCentavos > 0) {

                    linhas.add(new LinhaCalculada(
                            TipoComponenteCusto.PERDAS,
                            "Perdas/quebra (" + parametros.percentualPerdas() + "%)",
                            BigDecimal.ONE,
                            null,
                            perdasCentavos,
                            br.com.vidratx.enums.OrigemSugestao.DERIVADO,
                            null
                    ));

                    custoItemCentavos += perdasCentavos;
                }
            }
        }

        for (ComponenteAdicional componente : item.componentesAdicionais()) {

            boolean porUnidade = componente.tipo() != TipoComponenteCusto.DESLOCAMENTO && item.quantidade() > 1;

            BigDecimal quantidadeTotal = porUnidade
                    ? componente.quantidade().multiply(BigDecimal.valueOf(item.quantidade()))
                    : componente.quantidade();

            long valorSugerido = arredondar(
                    quantidadeTotal.multiply(BigDecimal.valueOf(componente.valorUnitarioCentavos()))
            );

            linhas.add(new LinhaCalculada(
                    componente.tipo(),
                    porUnidade
                            ? componente.descricao() + " (" + componente.quantidade().stripTrailingZeros().toPlainString()
                            + " × " + item.quantidade() + " unidades)"
                            : componente.descricao(),
                    quantidadeTotal,
                    componente.valorUnitarioCentavos(),
                    valorSugerido,
                    componente.origemSugestao(),
                    componente.tabelaPrecoId(),
                    componente.descricao(),
                    componente.quantidade()
            ));

            custoItemCentavos += valorSugerido;
        }

        avaliarAlertasNormativos(item, alertas);

        return new ResultadoItemCalculo(pecas, linhas, custoItemCentavos, List.copyOf(alertas));
    }

    public static TotaisCalculo fecharTotais(long custoTotalCentavos, ParametrosCalculoInput parametros) {

        long precoSugeridoCentavos = parametros.valoresSaoPrecoDeVenda()
                ? custoTotalCentavos
                : DinheiroUtils.dividirPorFatorRestante(custoTotalCentavos, parametros.somaPercentuaisPrecificacao());

        long precoExibidoCentavos = DinheiroUtils.arredondarComercial(
                precoSugeridoCentavos, parametros.arredondamentoComercial()
        );

        return new TotaisCalculo(custoTotalCentavos, precoSugeridoCentavos, precoExibidoCentavos);
    }

    public static BigDecimal calcularMargemReal(
            long precoFinalCentavos,
            long custoTotalCentavos,
            ParametrosCalculoInput parametros) {

        if (precoFinalCentavos <= 0) {
            return BigDecimal.ZERO;
        }

        long descontoCentavos = DinheiroUtils.aplicarPercentual(
                precoFinalCentavos, parametros.somaPercentuaisMargemReal()
        );

        BigDecimal margemCentavos = BigDecimal.valueOf(precoFinalCentavos - custoTotalCentavos - descontoCentavos);

        return margemCentavos
                .multiply(BigDecimal.valueOf(100))
                .divide(BigDecimal.valueOf(precoFinalCentavos), 4, RoundingMode.HALF_UP)
                .setScale(2, RoundingMode.HALF_UP);
    }

    public static java.util.Optional<AlertaCalculo> avaliarMargemMinima(
            BigDecimal margemRealPercentual,
            ParametrosCalculoInput parametros) {

        if (margemRealPercentual.compareTo(parametros.percentualMargemMinima()) < 0) {

            return java.util.Optional.of(new AlertaCalculo(
                    AlertaCalculo.ABAIXO_MARGEM_MINIMA,
                    "O preço final está com margem de " + margemRealPercentual
                            + "%, abaixo da sua margem mínima de "
                            + parametros.percentualMargemMinima() + "%."
            ));
        }

        return java.util.Optional.empty();
    }

    public static BigDecimal perimetroTotalM(List<PecaCalculada> pecas) {

        BigDecimal total = BigDecimal.ZERO;

        for (PecaCalculada peca : pecas) {
            total = total.add(peca.perimetroM().multiply(BigDecimal.valueOf(peca.quantidade())));
        }

        return total;
    }

    private static List<PecaCalculada> gerarPecas(
            ItemCalculoInput item,
            ParametrosCalculoInput parametros,
            List<AlertaCalculo> alertas) {

        TipologiaRegras tipologia = item.tipologia();

        if (tipologia.formulaPecas() == FormulaPecas.SEM_PECAS
                || item.larguraVaoMm() == null
                || item.alturaVaoMm() == null) {

            return List.of();
        }

        List<PecaCalculada> pecas = new ArrayList<>();

        switch (tipologia.formulaPecas()) {

            case PECA_UNICA -> pecas.add(criarPeca(
                    "Peça única",
                    item.larguraVaoMm() - tipologia.descontoLarguraMm(),
                    item.alturaVaoMm() - tipologia.descontoAlturaMm(),
                    1,
                    parametros,
                    alertas
            ));

            case DIVIDIR_LARGURA_IGUAL -> {

                int larguraFolha = (item.larguraVaoMm() - tipologia.descontoLarguraMm()
                        + tipologia.transpasseMm()) / tipologia.numeroFolhas();

                int alturaFolha = item.alturaVaoMm() - tipologia.descontoAlturaMm();

                for (int i = 1; i <= tipologia.numeroFolhas(); i++) {

                    pecas.add(criarPeca(
                            "Folha " + i, larguraFolha, alturaFolha, 1, parametros, alertas
                    ));
                }
            }

            case DOIS_VAOS_DIVIDIDOS -> {

                int folhasPorVao = Math.max(1, tipologia.numeroFolhas() / 2);

                Integer larguraVao2 = item.larguraVao2Mm() != null
                        ? item.larguraVao2Mm() : item.larguraVaoMm();
                Integer alturaVao2 = item.alturaVao2Mm() != null
                        ? item.alturaVao2Mm() : item.alturaVaoMm();

                int larguraFolha1 = (item.larguraVaoMm() - tipologia.descontoLarguraMm()
                        + tipologia.transpasseMm()) / folhasPorVao;
                int alturaFolha1 = item.alturaVaoMm() - tipologia.descontoAlturaMm();

                for (int i = 1; i <= folhasPorVao; i++) {
                    pecas.add(criarPeca(
                            "Frente " + i, larguraFolha1, alturaFolha1, 1, parametros, alertas
                    ));
                }

                int larguraFolha2 = (larguraVao2 - tipologia.descontoLarguraMm()
                        + tipologia.transpasseMm()) / folhasPorVao;
                int alturaFolha2 = alturaVao2 - tipologia.descontoAlturaMm();

                for (int i = 1; i <= folhasPorVao; i++) {
                    pecas.add(criarPeca(
                            "Lateral " + i, larguraFolha2, alturaFolha2, 1, parametros, alertas
                    ));
                }
            }

            case SEM_PECAS -> {
            }
        }

        return pecas;
    }

    private static PecaCalculada criarPeca(
            String descricao,
            int larguraMm,
            int alturaMm,
            int quantidade,
            ParametrosCalculoInput parametros,
            List<AlertaCalculo> alertas) {

        int larguraArred = DinheiroUtils.arredondarParaCimaMultiplo(
                larguraMm, parametros.multiploArredondamentoMm()
        );
        int alturaArred = DinheiroUtils.arredondarParaCimaMultiplo(
                alturaMm, parametros.multiploArredondamentoMm()
        );

        boolean excede = excedeTamanhoMaximo(larguraMm, alturaMm, parametros);

        if (excede) {

            alertas.add(new AlertaCalculo(
                    AlertaCalculo.PECA_EXCEDE_CHAPA,
                    "A peça \"" + descricao + "\" (" + larguraMm + " x " + alturaMm
                            + " mm) é maior que o tamanho máximo de chapa configurado."
            ));
        }

        return new PecaCalculada(descricao, larguraMm, alturaMm, larguraArred, alturaArred, quantidade, excede);
    }

    private static boolean excedeTamanhoMaximo(
            int larguraMm,
            int alturaMm,
            ParametrosCalculoInput parametros) {

        Integer maxLargura = parametros.tamanhoMaximoChapaLarguraMm();
        Integer maxAltura = parametros.tamanhoMaximoChapaAlturaMm();

        if (maxLargura == null || maxAltura == null) {
            return false;
        }

        boolean cabeSemGirar = larguraMm <= maxLargura && alturaMm <= maxAltura;
        boolean cabeGirando = larguraMm <= maxAltura && alturaMm <= maxLargura;

        return !cabeSemGirar && !cabeGirando;
    }

    private static void avaliarAlertasNormativos(ItemCalculoInput item, List<AlertaCalculo> alertas) {

        List<String> codigos = item.tipologia().alertasNormativos();

        if (codigos.contains(AlertaCalculo.REQUER_VIDRO_SEGURANCA) && !item.vidroSeguranca()) {

            alertas.add(new AlertaCalculo(
                    AlertaCalculo.REQUER_VIDRO_SEGURANCA,
                    "Esta tipologia exige vidro de segurança (temperado ou laminado) — NBR 14207/16259."
            ));
        }

        if (codigos.contains(AlertaCalculo.REQUER_LAMINADO) && !item.vidroLaminado()) {

            alertas.add(new AlertaCalculo(
                    AlertaCalculo.REQUER_LAMINADO,
                    "Guarda-corpo exige vidro laminado de segurança — NBR 14718."
            ));
        }
    }

    private static long arredondar(BigDecimal valor) {
        return valor.setScale(0, RoundingMode.HALF_UP).longValueExact();
    }
}
