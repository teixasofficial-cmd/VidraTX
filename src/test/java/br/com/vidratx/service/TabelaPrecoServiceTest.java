package br.com.vidratx.service;

import br.com.vidratx.dto.TabelaPrecoRequest;
import br.com.vidratx.dto.TabelaPrecoResponse;
import br.com.vidratx.entity.Empresa;
import br.com.vidratx.entity.TabelaPreco;
import br.com.vidratx.enums.CategoriaItemPreco;
import br.com.vidratx.enums.UnidadeMedida;
import br.com.vidratx.exception.TabelaPrecoNaoEncontradaException;
import br.com.vidratx.mapper.TabelaPrecoMapper;
import br.com.vidratx.repository.EmpresaRepository;
import br.com.vidratx.repository.TabelaPrecoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TabelaPrecoServiceTest {

    private static final Long EMPRESA_ID = 1L;

    private TabelaPrecoRepository tabelaPrecoRepository;
    private EmpresaRepository empresaRepository;
    private HistoricoService historicoService;
    private TabelaPrecoService tabelaPrecoService;

    @BeforeEach
    void montarCenario() {

        tabelaPrecoRepository = mock(TabelaPrecoRepository.class);
        empresaRepository = mock(EmpresaRepository.class);
        historicoService = mock(HistoricoService.class);

        tabelaPrecoService = new TabelaPrecoService(
                tabelaPrecoRepository, empresaRepository, new TabelaPrecoMapper(), historicoService
        );
    }

    private TabelaPrecoRequest montarRequest() {

        TabelaPrecoRequest request = new TabelaPrecoRequest();
        request.setCategoria(CategoriaItemPreco.VIDRO);
        request.setDescricao("Temperado 8mm incolor");
        request.setUnidade(UnidadeMedida.M2);
        request.setPrecoVenda(new BigDecimal("150.00"));
        request.setCusto(new BigDecimal("90.50"));

        return request;
    }

    @Test
    void salvaConvertendoReaisParaCentavos() {

        when(empresaRepository.findById(EMPRESA_ID)).thenReturn(Optional.of(new Empresa()));
        when(tabelaPrecoRepository.save(any())).thenAnswer(invocacao -> invocacao.getArgument(0));

        TabelaPrecoResponse response = tabelaPrecoService.salvar(EMPRESA_ID, montarRequest());

        assertEquals(0, new BigDecimal("150.00").compareTo(response.getPrecoVenda()));
        assertEquals(0, new BigDecimal("90.50").compareTo(response.getCusto()));
    }

    @Test
    void buscarPorIdLancaExcecaoQuandoItemNaoPertenceAEmpresa() {

        when(tabelaPrecoRepository.findByIdAndEmpresaId(eq(99L), eq(EMPRESA_ID)))
                .thenReturn(Optional.empty());

        assertThrows(
                TabelaPrecoNaoEncontradaException.class,
                () -> tabelaPrecoService.buscarPorId(EMPRESA_ID, 99L)
        );
    }

    @Test
    void buscaPorIdConverteCentavosParaReaisNaResposta() {

        TabelaPreco entidade = new TabelaPreco();
        entidade.setEmpresa(new Empresa());
        entidade.setCategoria(CategoriaItemPreco.VIDRO);
        entidade.setDescricao("Temperado 10mm incolor");
        entidade.setUnidade(UnidadeMedida.M2);
        entidade.setPrecoVendaCentavos(18000L);
        entidade.setCustoCentavos(11000L);

        when(tabelaPrecoRepository.findByIdAndEmpresaId(eq(5L), eq(EMPRESA_ID)))
                .thenReturn(Optional.of(entidade));

        TabelaPrecoResponse response = tabelaPrecoService.buscarPorId(EMPRESA_ID, 5L);

        assertEquals(0, new BigDecimal("180.00").compareTo(response.getPrecoVenda()));
        assertEquals(0, new BigDecimal("110.00").compareTo(response.getCusto()));
    }
}
