package br.com.vidratx.service;

import br.com.vidratx.entity.Cliente;
import br.com.vidratx.entity.Empresa;
import br.com.vidratx.entity.Orcamento;
import br.com.vidratx.entity.PerguntaPendente;
import br.com.vidratx.enums.CategoriaMensagemSaida;
import br.com.vidratx.enums.StatusOrcamento;
import br.com.vidratx.enums.TipoPergunta;
import br.com.vidratx.repository.OrcamentoRepository;
import br.com.vidratx.service.WhatsappSaidaService.NovaMensagem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrcamentoFollowUpServiceTest {

    private static final long HORAS = 24;
    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 9, 22, 10, 0);

    private OrcamentoRepository orcamentoRepository;
    private PerguntaPendenteService perguntaPendenteService;
    private WhatsappSaidaService whatsappSaidaService;
    private OrcamentoFollowUpService followUpService;

    @BeforeEach
    void montarCenario() {

        orcamentoRepository = mock(OrcamentoRepository.class);
        perguntaPendenteService = mock(PerguntaPendenteService.class);
        whatsappSaidaService = mock(WhatsappSaidaService.class);

        followUpService = new OrcamentoFollowUpService(
                orcamentoRepository, perguntaPendenteService, whatsappSaidaService,
                mock(WhatsappContatoService.class), mock(HistoricoService.class),
                mock(PlatformTransactionManager.class), Clock.systemDefaultZone(), HORAS
        );
    }

    private Orcamento orcamentoEnviado(String whatsapp) {

        Empresa empresa = new Empresa();
        ReflectionTestUtils.setField(empresa, "id", 1L);

        Cliente cliente = new Cliente();
        cliente.setNome("Maria Souza");
        cliente.setWhatsapp(whatsapp);

        Orcamento orcamento = new Orcamento();
        ReflectionTestUtils.setField(orcamento, "id", 10L);
        orcamento.setEmpresa(empresa);
        orcamento.setCliente(cliente);
        orcamento.setStatus(StatusOrcamento.ENVIADO);
        orcamento.setEnviadoEm(AGORA.minusHours(30));
        orcamento.setRevisaoEnvio(1);
        orcamento.setValorTotal(new BigDecimal("1850.00"));

        when(orcamentoRepository.findAllByStatusAndEnviadoEmBeforeAndLembreteEnviadoEmIsNull(any(), any()))
                .thenReturn(List.of(orcamento));
        when(orcamentoRepository.findById(10L)).thenReturn(Optional.of(orcamento));

        return orcamento;
    }

    private PerguntaPendente perguntaAtiva(int versao) {

        PerguntaPendente pergunta = new PerguntaPendente();
        pergunta.setTipo(TipoPergunta.APROVAR_ORCAMENTO);
        pergunta.setReferenciaId(10L);
        pergunta.setVersao(versao);

        when(perguntaPendenteService.ativas(any(), any())).thenReturn(List.of(pergunta));

        return pergunta;
    }

    @Test
    void enviaLembreteComAPerguntaDeAprovacaoPelaCaixaDeSaida() {

        Orcamento orcamento = orcamentoEnviado("5511988887777");
        PerguntaPendente pergunta = perguntaAtiva(1);

        assertEquals(1, followUpService.processarLembretes(AGORA));
        assertNotNull(orcamento.getLembreteEnviadoEm());

        ArgumentCaptor<NovaMensagem> mensagem = ArgumentCaptor.forClass(NovaMensagem.class);
        verify(whatsappSaidaService).enfileirar(mensagem.capture());

        assertEquals(CategoriaMensagemSaida.LEMBRETE, mensagem.getValue().categoria());
        assertSame(pergunta, mensagem.getValue().pergunta());
        assertTrue(mensagem.getValue().conteudo().contains("1 - Aprovar"));
    }

    @Test
    void naoCobraQuandoAPerguntaDeAprovacaoNaoEstaMaisAtiva() {

        Orcamento orcamento = orcamentoEnviado("5511988887777");

        perguntaAtiva(2);

        assertEquals(0, followUpService.processarLembretes(AGORA));
        assertNull(orcamento.getLembreteEnviadoEm());
        verify(whatsappSaidaService, never()).enfileirar(any());
    }

    @Test
    void naoEnviaQuandoClienteNaoTemWhatsappCadastrado() {

        orcamentoEnviado(null);

        assertEquals(0, followUpService.processarLembretes(AGORA));
        verify(whatsappSaidaService, never()).enfileirar(any());
    }

    @Test
    void naoEnviaQuandoAVidracariaDesligouOLembrete() {

        Orcamento orcamento = orcamentoEnviado("5511988887777");
        perguntaAtiva(1);
        orcamento.getEmpresa().setLembreteOrcamentoAtivo(false);

        assertEquals(0, followUpService.processarLembretes(AGORA));
        verify(whatsappSaidaService, never()).enfileirar(any());
    }
}
