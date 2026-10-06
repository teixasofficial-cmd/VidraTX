package br.com.vidratx.service;

import br.com.vidratx.calculo.AlertaCalculo;
import br.com.vidratx.dto.OrcamentoTotaisResponse;
import br.com.vidratx.entity.Empresa;
import br.com.vidratx.entity.Orcamento;
import br.com.vidratx.entity.OrcamentoItem;
import br.com.vidratx.entity.OrcamentoLinha;
import br.com.vidratx.entity.ParametroCalculo;
import br.com.vidratx.entity.TabelaPreco;
import br.com.vidratx.enums.CorVidro;
import br.com.vidratx.enums.ModoPrecificacao;
import br.com.vidratx.enums.StatusOrcamento;
import br.com.vidratx.enums.TipoComponenteCusto;
import br.com.vidratx.enums.TipoVidro;
import br.com.vidratx.mapper.OrcamentoItemMapper;
import br.com.vidratx.mapper.TipologiaMapper;
import br.com.vidratx.repository.OrcamentoItemRepository;
import br.com.vidratx.repository.OrcamentoLinhaRepository;
import br.com.vidratx.repository.OrcamentoPecaRepository;
import br.com.vidratx.repository.OrcamentoRepository;
import br.com.vidratx.repository.TabelaPrecoRepository;
import br.com.vidratx.repository.TipologiaRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrcamentoCalculoServiceTest {

    private static final Long EMPRESA_ID = 1L;
    private static final Long ORCAMENTO_ID = 10L;

    private OrcamentoRepository orcamentoRepository;
    private OrcamentoLinhaRepository orcamentoLinhaRepository;
    private TabelaPrecoRepository tabelaPrecoRepository;
    private ParametroCalculoService parametroCalculoService;
    private OrcamentoCalculoService orcamentoCalculoService;
    private TabelaPreco vidroFume;

    @BeforeEach
    void montarCenario() {

        orcamentoRepository = mock(OrcamentoRepository.class);
        orcamentoLinhaRepository = mock(OrcamentoLinhaRepository.class);
        tabelaPrecoRepository = mock(TabelaPrecoRepository.class);
        parametroCalculoService = mock(ParametroCalculoService.class);

        when(orcamentoLinhaRepository.findAllByOrcamentoItemOrcamentoIdOrderByIdAsc(any()))
                .thenReturn(List.of());

        ParametroCalculoService conversorReal = new ParametroCalculoService(
                null, null, null, null, new ObjectMapper(), null
        );

        when(parametroCalculoService.paraMotor(any(), any()))
                .thenAnswer(invocacao -> conversorReal.paraMotor(invocacao.getArgument(0), invocacao.getArgument(1)));

        orcamentoCalculoService = new OrcamentoCalculoService(
                orcamentoRepository,
                mock(OrcamentoItemRepository.class),
                mock(OrcamentoPecaRepository.class),
                orcamentoLinhaRepository,
                mock(TipologiaRepository.class),
                tabelaPrecoRepository,
                new TipologiaMapper(new ObjectMapper()),
                new OrcamentoItemMapper(),
                parametroCalculoService,
                mock(HistoricoService.class),
                Clock.systemDefaultZone()
        );
    }

    @Test
    void orcamentoEnviadoDevolveOsValoresCongeladosSemReconsultarParametros() {

        Orcamento orcamento = novoOrcamento();
        orcamento.setStatus(StatusOrcamento.ENVIADO);
        orcamento.setCustoTotalCentavos(100_000L);
        orcamento.setPrecoSugeridoCentavos(150_000L);
        orcamento.setValorTotalCentavos(150_000L);
        orcamento.setAjusteComercialCentavos(0L);
        orcamento.setMargemRealPercentual(new BigDecimal("33.33"));

        when(orcamentoRepository.findByIdAndEmpresaId(ORCAMENTO_ID, EMPRESA_ID))
                .thenReturn(Optional.of(orcamento));

        OrcamentoTotaisResponse totais = orcamentoCalculoService.buscarTotais(EMPRESA_ID, ORCAMENTO_ID);

        assertEquals(new BigDecimal("1000.00"), totais.getCustoTotal());
        assertEquals(new BigDecimal("1500.00"), totais.getPrecoSugerido());
        assertEquals(new BigDecimal("1500.00"), totais.getValorFinal());
        assertEquals(new BigDecimal("33.33"), totais.getMargemReal());

        verify(parametroCalculoService, never()).buscarOuCriarPadrao(any());
    }

    @Test
    void orcamentoEditavelRecalculaAoVivoComOsParametrosAtuais() {

        Orcamento orcamento = novoOrcamento();
        orcamento.setStatus(StatusOrcamento.NOVO_CONTATO);
        orcamento.setAjusteComercialCentavos(0L);

        when(orcamentoRepository.findByIdAndEmpresaId(ORCAMENTO_ID, EMPRESA_ID))
                .thenReturn(Optional.of(orcamento));

        when(parametroCalculoService.buscarOuCriarPadrao(EMPRESA_ID))
                .thenReturn(new br.com.vidratx.entity.ParametroCalculo());

        orcamentoCalculoService.buscarTotais(EMPRESA_ID, ORCAMENTO_ID);

        verify(parametroCalculoService).buscarOuCriarPadrao(EMPRESA_ID);
    }

    @Test
    void semNenhumaLinhaCustoRealFicaVazioSemAvisoDeIncompleto() {

        Orcamento orcamento = novoOrcamento();
        orcamento.setStatus(StatusOrcamento.NOVO_CONTATO);
        orcamento.setAjusteComercialCentavos(0L);

        when(orcamentoRepository.findByIdAndEmpresaId(ORCAMENTO_ID, EMPRESA_ID))
                .thenReturn(Optional.of(orcamento));

        when(parametroCalculoService.buscarOuCriarPadrao(EMPRESA_ID))
                .thenReturn(new br.com.vidratx.entity.ParametroCalculo());

        OrcamentoTotaisResponse totais = orcamentoCalculoService.buscarTotais(EMPRESA_ID, ORCAMENTO_ID);

        assertEquals(null, totais.getCustoRealTotal());
        assertEquals(null, totais.getMargemSobreCustoReal());
        assertFalse(totais.isCustoRealIncompleto());
    }

    @Test
    void linhaSemTabelaPrecoVinculadaMarcaCustoRealComoIncompleto() {

        Orcamento orcamento = novoOrcamento();
        orcamento.setStatus(StatusOrcamento.NOVO_CONTATO);
        orcamento.setAjusteComercialCentavos(0L);

        when(orcamentoRepository.findByIdAndEmpresaId(ORCAMENTO_ID, EMPRESA_ID))
                .thenReturn(Optional.of(orcamento));

        when(parametroCalculoService.buscarOuCriarPadrao(EMPRESA_ID))
                .thenReturn(new br.com.vidratx.entity.ParametroCalculo());

        br.com.vidratx.entity.OrcamentoLinha linhaManual = new br.com.vidratx.entity.OrcamentoLinha();
        linhaManual.setValorSugeridoCentavos(5_000L);

        when(orcamentoLinhaRepository.findAllByOrcamentoItemOrcamentoIdOrderByIdAsc(any()))
                .thenReturn(List.of(linhaManual));

        OrcamentoTotaisResponse totais = orcamentoCalculoService.buscarTotais(EMPRESA_ID, ORCAMENTO_ID);

        assertEquals(null, totais.getMargemSobreCustoReal());
        assertEquals(true, totais.isCustoRealIncompleto());
    }

    @Test
    void vidroEPerdasUsamOCustoDoVidroDoItemNoModoPrecoDeVenda() {

        orcamentoEditavelModoVenda();

        OrcamentoTotaisResponse totais = orcamentoCalculoService.buscarTotais(EMPRESA_ID, ORCAMENTO_ID);

        assertFalse(totais.isCustoRealIncompleto());
        assertEquals(new BigDecimal("633.16"), totais.getCustoRealTotal());
        assertEquals(new BigDecimal("33.33"), totais.getMargemSobreCustoReal());

        assertEquals(new BigDecimal("33.33"), totais.getMargemReal());
        assertTrue(totais.getAlertas().stream()
                .noneMatch(a -> AlertaCalculo.ABAIXO_MARGEM_MINIMA.equals(a.getCodigo())));
    }

    @Test
    void precoFinalAbaixoDaMargemMinimaAlertaNoModoPrecoDeVenda() {

        Orcamento orcamento = orcamentoEditavelModoVenda();
        orcamento.setPrecoFinalManualCentavos(70_000L);

        OrcamentoTotaisResponse totais = orcamentoCalculoService.buscarTotais(EMPRESA_ID, ORCAMENTO_ID);

        assertEquals(new BigDecimal("3.55"), totais.getMargemReal());
        assertTrue(totais.getAlertas().stream()
                .anyMatch(a -> AlertaCalculo.ABAIXO_MARGEM_MINIMA.equals(a.getCodigo())));
    }

    @Test
    void vidroSemSeuCustoDeixaOCustoRealIncompleto() {

        orcamentoEditavelModoVenda();
        vidroFume.setCustoCentavos(null);

        OrcamentoTotaisResponse totais = orcamentoCalculoService.buscarTotais(EMPRESA_ID, ORCAMENTO_ID);

        assertTrue(totais.isCustoRealIncompleto());
        assertEquals(null, totais.getMargemSobreCustoReal());
        assertEquals(null, totais.getMargemReal());
    }

    private Orcamento orcamentoEditavelModoVenda() {

        Orcamento orcamento = novoOrcamento();
        orcamento.setStatus(StatusOrcamento.PRE_ORCAMENTO);
        orcamento.setAjusteComercialCentavos(0L);

        when(orcamentoRepository.findByIdAndEmpresaId(ORCAMENTO_ID, EMPRESA_ID))
                .thenReturn(Optional.of(orcamento));

        ParametroCalculo parametros = new ParametroCalculo();
        parametros.setModoPrecificacao(ModoPrecificacao.VENDA);

        when(parametroCalculoService.buscarOuCriarPadrao(EMPRESA_ID)).thenReturn(parametros);

        vidroFume = itemTabela(28_600L, 18_000L);
        TabelaPreco kit = itemTabela(26_000L, 14_000L);

        OrcamentoItem box = new OrcamentoItem();
        box.setTipoVidro(TipoVidro.TEMPERADO);
        box.setEspessuraMm((short) 8);
        box.setCor(CorVidro.FUME);

        when(tabelaPrecoRepository.findFirstByEmpresaIdAndAtivoTrueAndTipoVidroAndEspessuraMmAndCorAndAcabamento(
                EMPRESA_ID, TipoVidro.TEMPERADO, (short) 8, CorVidro.FUME, null))
                .thenReturn(Optional.of(vidroFume));

        when(orcamentoLinhaRepository.findAllByOrcamentoItemOrcamentoIdOrderByIdAsc(any()))
                .thenReturn(List.of(
                        linha(box, TipoComponenteCusto.VIDRO, 76_076L, null),
                        linha(box, TipoComponenteCusto.PERDAS, 2_282L, null),
                        linha(box, TipoComponenteCusto.KIT, 26_000L, kit)
                ));

        return orcamento;
    }

    private static TabelaPreco itemTabela(Long precoVendaCentavos, Long custoCentavos) {

        TabelaPreco tabela = new TabelaPreco();
        tabela.setPrecoVendaCentavos(precoVendaCentavos);
        tabela.setCustoCentavos(custoCentavos);

        return tabela;
    }

    private static OrcamentoLinha linha(
            OrcamentoItem item, TipoComponenteCusto tipo, long valorCentavos, TabelaPreco tabela) {

        OrcamentoLinha linha = new OrcamentoLinha();
        linha.setOrcamentoItem(item);
        linha.setTipoComponente(tipo);
        linha.setValorSugeridoCentavos(valorCentavos);
        linha.setTabelaPreco(tabela);

        return linha;
    }

    private Orcamento novoOrcamento() {

        Empresa empresa = new Empresa();
        ReflectionTestUtils.setField(empresa, "id", EMPRESA_ID);

        Orcamento orcamento = new Orcamento();
        orcamento.setEmpresa(empresa);
        ReflectionTestUtils.setField(orcamento, "id", ORCAMENTO_ID);

        return orcamento;
    }
}
