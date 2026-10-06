package br.com.vidratx.service;

import br.com.vidratx.dto.DashboardResumoResponse;
import br.com.vidratx.dto.ProximaAcaoItemResponse;
import br.com.vidratx.entity.Cliente;
import br.com.vidratx.entity.Empresa;
import br.com.vidratx.entity.Orcamento;
import br.com.vidratx.entity.OrdemServico;
import br.com.vidratx.enums.StatusOrcamento;
import br.com.vidratx.enums.StatusProducao;
import br.com.vidratx.repository.AtendimentoWhatsappRepository;
import br.com.vidratx.repository.EmpresaRepository;
import br.com.vidratx.repository.InstalacaoRepository;
import br.com.vidratx.repository.MedicaoRepository;
import br.com.vidratx.repository.MensagemSaidaRepository;
import br.com.vidratx.repository.OrcamentoRepository;
import br.com.vidratx.repository.OrdemServicoRepository;
import br.com.vidratx.repository.SolicitacaoOrcamentoRepository;
import br.com.vidratx.repository.WhatsappInstanciaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DashboardServiceTest {

    private static final Long EMPRESA_ID = 1L;

    private OrcamentoRepository orcamentoRepository;
    private OrdemServicoRepository ordemServicoRepository;
    private DashboardService dashboardService;

    @BeforeEach
    void montarCenario() {

        orcamentoRepository = mock(OrcamentoRepository.class);
        ordemServicoRepository = mock(OrdemServicoRepository.class);

        AtendimentoWhatsappService atendimentoWhatsappService = mock(AtendimentoWhatsappService.class);

        dashboardService = new DashboardService(
                orcamentoRepository, atendimentoWhatsappService, mock(AtendimentoWhatsappRepository.class),
                mock(MedicaoRepository.class), mock(InstalacaoRepository.class), ordemServicoRepository,
                mock(SolicitacaoOrcamentoRepository.class), mock(MensagemSaidaRepository.class),
                mock(EmpresaRepository.class), mock(WhatsappInstanciaRepository.class),
                mock(NegociacaoAgendamentoService.class), Clock.systemDefaultZone()
        );

        when(orcamentoRepository.somarValorPorEmpresaEStatus(any(), any())).thenReturn(BigDecimal.ZERO);
        when(orcamentoRepository.somarValorEnviado(any())).thenReturn(BigDecimal.ZERO);
        when(atendimentoWhatsappService.contarPendentes(any())).thenReturn(0L);
    }

    @Test
    void calculaTaxaDeConversaoSobreOrcamentosDeFatoEnviados() {

        when(orcamentoRepository.countByEmpresaIdAndStatus(eq(EMPRESA_ID), eq(StatusOrcamento.APROVADO))).thenReturn(21L);
        when(orcamentoRepository.countByEmpresaIdAndStatus(eq(EMPRESA_ID), eq(StatusOrcamento.PERDIDO))).thenReturn(26L);

        when(orcamentoRepository.countByEmpresaIdAndEnviadoEmIsNotNull(EMPRESA_ID)).thenReturn(47L);

        DashboardResumoResponse resumo = dashboardService.resumo(EMPRESA_ID);

        assertEquals(21L, resumo.getOrcamentosAprovados());
        assertEquals(47L, resumo.getTotalOrcamentosEnviados());
        assertEquals(44.7, resumo.getTaxaConversaoPercentual());
    }

    @Test
    void taxaDeConversaoEhZeroQuandoNaoHaOrcamentosEnviados() {

        when(orcamentoRepository.countByEmpresaIdAndStatus(eq(EMPRESA_ID), any())).thenReturn(0L);
        when(orcamentoRepository.countByEmpresaIdAndEnviadoEmIsNotNull(EMPRESA_ID)).thenReturn(0L);

        assertEquals(0.0, dashboardService.resumo(EMPRESA_ID).getTaxaConversaoPercentual());
    }

    @Test
    void ordemDeServicoSemInstalacaoApareceComoAcaoDaEmpresa() {

        Cliente cliente = new Cliente();
        cliente.setNome("Ana");

        Orcamento orcamento = new Orcamento();
        ReflectionTestUtils.setField(orcamento, "id", 7L);
        orcamento.setEmpresa(new Empresa());
        orcamento.setCliente(cliente);
        orcamento.setStatus(StatusOrcamento.APROVADO);

        OrdemServico conferida = new OrdemServico();
        ReflectionTestUtils.setField(conferida, "id", 3L);
        conferida.setOrcamento(orcamento);
        conferida.setNecessitaProducao(true);
        conferida.setStatusProducao(StatusProducao.CONFERIDO);

        when(ordemServicoRepository.findSemInstalacao(EMPRESA_ID)).thenReturn(List.of(conferida));

        List<ProximaAcaoItemResponse> acoes = dashboardService.proximasAcoes(EMPRESA_ID);

        assertEquals(1, acoes.size());
        assertEquals("EMPRESA", acoes.get(0).responsavel());
        assertTrue(acoes.get(0).descricao().contains("Agendar a instalação"));
    }

    @Test
    void agendaRecusaPeriodoInvertido() {

        LocalDate hoje = LocalDate.now();

        org.junit.jupiter.api.Assertions.assertThrows(IllegalArgumentException.class,
                () -> dashboardService.agenda(EMPRESA_ID, hoje, hoje.minusDays(1)));
    }
}
