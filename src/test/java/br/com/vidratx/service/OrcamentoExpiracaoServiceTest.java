package br.com.vidratx.service;

import br.com.vidratx.entity.Empresa;
import br.com.vidratx.entity.Orcamento;
import br.com.vidratx.enums.StatusOrcamento;
import br.com.vidratx.exception.TransicaoInvalidaException;
import br.com.vidratx.repository.OrcamentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrcamentoExpiracaoServiceTest {

    private static final long DIAS_LEGADO = 15;

    private OrcamentoRepository orcamentoRepository;
    private OrcamentoService orcamentoService;
    private OrcamentoExpiracaoService expiracaoService;

    @BeforeEach
    void montarCenario() {

        orcamentoRepository = mock(OrcamentoRepository.class);
        orcamentoService = mock(OrcamentoService.class);

        when(orcamentoRepository.findAllByStatusAndValidoAteBefore(any(), any())).thenReturn(List.of());
        when(orcamentoRepository.findAllByStatusAndValidoAteIsNullAndEnviadoEmBefore(any(), any())).thenReturn(List.of());

        doAnswer(invocacao -> {
            ((Orcamento) invocacao.getArgument(0)).setStatus(StatusOrcamento.EXPIRADO);
            return null;
        }).when(orcamentoService).expirarAutomaticamente(any());

        expiracaoService = new OrcamentoExpiracaoService(
                orcamentoRepository, orcamentoService, mock(PlatformTransactionManager.class),
                Clock.systemDefaultZone(), DIAS_LEGADO
        );
    }

    private Orcamento enviado(long id, LocalDate validoAte) {

        Orcamento orcamento = new Orcamento();
        ReflectionTestUtils.setField(orcamento, "id", id);
        orcamento.setEmpresa(new Empresa());
        orcamento.setStatus(StatusOrcamento.ENVIADO);
        orcamento.setValidoAte(validoAte);

        when(orcamentoRepository.findById(id)).thenReturn(Optional.of(orcamento));

        return orcamento;
    }

    @Test
    void expiraOrcamentoComValidadeVencida() {

        LocalDateTime agora = LocalDateTime.of(2026, 9, 22, 10, 0);
        Orcamento orcamento = enviado(1L, LocalDate.of(2026, 9, 21));

        when(orcamentoRepository.findAllByStatusAndValidoAteBefore(StatusOrcamento.ENVIADO, agora.toLocalDate().plusDays(1)))
                .thenReturn(List.of(orcamento));

        assertEquals(1, expiracaoService.processarExpiracoes(agora));
        verify(orcamentoService).expirarAutomaticamente(orcamento);
    }

    @Test
    void validoAteHojeContinuaValendo() {

        LocalDateTime agora = LocalDateTime.of(2026, 9, 22, 23, 50);
        Orcamento orcamento = enviado(6L, LocalDate.of(2026, 9, 22));

        when(orcamentoRepository.findAllByStatusAndValidoAteBefore(any(), any())).thenReturn(List.of(orcamento));

        assertEquals(0, expiracaoService.processarExpiracoes(agora));
        verify(orcamentoService, never()).expirarAutomaticamente(any());
    }

    @Test
    void validadeSegueOCalendarioDaEmpresa() {

        ZoneId brasilia = ZoneId.of("America/Sao_Paulo");
        LocalDateTime agora = LocalDateTime.of(2026, 9, 22, 23, 30);
        Clock relogio = Clock.fixed(agora.atZone(brasilia).toInstant(), brasilia);

        OrcamentoExpiracaoService servico = new OrcamentoExpiracaoService(
                orcamentoRepository, orcamentoService, mock(PlatformTransactionManager.class), relogio, DIAS_LEGADO
        );

        Orcamento noronha = enviado(7L, LocalDate.of(2026, 9, 22));
        noronha.getEmpresa().setFusoHorario("America/Noronha");

        Orcamento acre = enviado(8L, LocalDate.of(2026, 9, 22));
        acre.getEmpresa().setFusoHorario("America/Rio_Branco");

        when(orcamentoRepository.findAllByStatusAndValidoAteBefore(any(), any())).thenReturn(List.of(noronha, acre));

        assertEquals(1, servico.processarExpiracoes(agora));
        verify(orcamentoService).expirarAutomaticamente(noronha);
        verify(orcamentoService, never()).expirarAutomaticamente(acre);
    }

    @Test
    void prazoFixoSoValeParaOrcamentoAntigoSemValidade() {

        LocalDateTime agora = LocalDateTime.of(2026, 9, 22, 10, 0);
        Orcamento antigo = enviado(2L, null);

        when(orcamentoRepository.findAllByStatusAndValidoAteIsNullAndEnviadoEmBefore(
                eq(StatusOrcamento.ENVIADO), eq(agora.minusDays(DIAS_LEGADO))))
                .thenReturn(List.of(antigo));

        assertEquals(1, expiracaoService.processarExpiracoes(agora));
    }

    @Test
    void naoExpiraOrcamentoQueFoiRespondidoDepoisDaConsulta() {

        LocalDateTime agora = LocalDateTime.of(2026, 9, 22, 10, 0);
        Orcamento orcamento = enviado(3L, LocalDate.of(2026, 9, 1));

        when(orcamentoRepository.findAllByStatusAndValidoAteBefore(any(), any())).thenReturn(List.of(orcamento));

        orcamento.setStatus(StatusOrcamento.APROVADO);

        assertEquals(0, expiracaoService.processarExpiracoes(agora));
        verify(orcamentoService, never()).expirarAutomaticamente(any());
    }

    @Test
    void continuaProcessandoOsDemaisQuandoUmOrcamentoFalha() {

        LocalDateTime agora = LocalDateTime.of(2026, 9, 22, 10, 0);
        Orcamento falho = enviado(4L, LocalDate.of(2026, 9, 1));
        Orcamento ok = enviado(5L, LocalDate.of(2026, 9, 1));

        when(orcamentoRepository.findAllByStatusAndValidoAteBefore(any(), any())).thenReturn(List.of(falho, ok));

        doAnswer(invocacao -> {

            Orcamento alvo = invocacao.getArgument(0);

            if (alvo == falho) {
                throw new TransicaoInvalidaException("já respondido");
            }

            alvo.setStatus(StatusOrcamento.EXPIRADO);
            return null;

        }).when(orcamentoService).expirarAutomaticamente(any());

        assertEquals(1, expiracaoService.processarExpiracoes(agora));
        verify(orcamentoService, times(2)).expirarAutomaticamente(any());
    }
}
