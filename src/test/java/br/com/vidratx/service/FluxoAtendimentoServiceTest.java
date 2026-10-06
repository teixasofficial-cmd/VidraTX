package br.com.vidratx.service;

import br.com.vidratx.entity.AtendimentoWhatsapp;
import br.com.vidratx.entity.Cliente;
import br.com.vidratx.entity.Empresa;
import br.com.vidratx.entity.Orcamento;
import br.com.vidratx.entity.PerguntaFrequente;
import br.com.vidratx.entity.PerguntaPendente;
import br.com.vidratx.enums.EtapaFluxo;
import br.com.vidratx.enums.StatusAtendimento;
import br.com.vidratx.enums.StatusOrcamento;
import br.com.vidratx.enums.TipoPergunta;
import br.com.vidratx.event.SolicitacaoRecebidaPeloBotEvent;
import br.com.vidratx.repository.ClienteRepository;
import br.com.vidratx.repository.OrcamentoRepository;
import br.com.vidratx.repository.SolicitacaoOrcamentoRepository;
import br.com.vidratx.service.FluxoAtendimentoService.RespostaFluxo;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class FluxoAtendimentoServiceTest {

    private static final int MAX_TENTATIVAS_TESTE = 3;

    private ClienteRepository clienteRepository;
    private SolicitacaoOrcamentoRepository solicitacaoOrcamentoRepository;
    private OrcamentoRepository orcamentoRepository;
    private PerguntaPendenteService perguntaPendenteService;
    private PerguntaFrequenteService perguntaFrequenteService;
    private HorarioAtendimento horarioAtendimento;
    private ApplicationEventPublisher eventos;
    private FluxoAtendimentoService fluxo;
    private AtendimentoWhatsapp atendimento;

    @BeforeEach
    void montarCenario() {

        clienteRepository = mock(ClienteRepository.class);
        solicitacaoOrcamentoRepository = mock(SolicitacaoOrcamentoRepository.class);
        orcamentoRepository = mock(OrcamentoRepository.class);
        perguntaPendenteService = mock(PerguntaPendenteService.class);
        perguntaFrequenteService = mock(PerguntaFrequenteService.class);
        horarioAtendimento = mock(HorarioAtendimento.class);
        eventos = mock(ApplicationEventPublisher.class);

        when(perguntaFrequenteService.encontrar(any(), any())).thenReturn(Optional.empty());
        when(horarioAtendimento.avisoForaDoHorario(any())).thenReturn(Optional.empty());

        when(clienteRepository.save(any())).thenAnswer(invocacao -> invocacao.getArgument(0));
        when(solicitacaoOrcamentoRepository.save(any()))
                .thenAnswer(invocacao -> invocacao.getArgument(0));
        when(orcamentoRepository.findAllByEmpresaIdAndClienteIdOrderByCriadoEmDesc(any(), any()))
                .thenReturn(List.of());

        fluxo = new FluxoAtendimentoService(
                clienteRepository,
                solicitacaoOrcamentoRepository,
                orcamentoRepository,
                perguntaPendenteService,
                perguntaFrequenteService,
                horarioAtendimento,
                eventos,
                MAX_TENTATIVAS_TESTE
        );

        atendimento = new AtendimentoWhatsapp();
        atendimento.setEmpresa(new Empresa());
        atendimento.setTelefone("5511999999999");
    }

    @Test
    void clienteNovoEhPerguntadoPeloNomeAntesDoMenu() {

        String resposta = fluxo.iniciarConversa(atendimento);

        assertEquals(EtapaFluxo.COLETA_NOME, atendimento.getEtapaFluxo());
        assertTrue(resposta.toLowerCase().contains("nome"));
    }

    @Test
    void fluxoCompletoDeOrcamentoCriaSolicitacaoEEscalaParaAtendente() {

        fluxo.iniciarConversa(atendimento);

        fluxo.processarMensagem(atendimento, "Maria Silva");
        assertNotNull(atendimento.getCliente());
        assertEquals("Maria Silva", atendimento.getCliente().getNome());
        assertEquals(EtapaFluxo.MENU, atendimento.getEtapaFluxo());

        fluxo.processarMensagem(atendimento, "1");
        assertEquals(EtapaFluxo.COLETA_SERVICO, atendimento.getEtapaFluxo());

        fluxo.processarMensagem(atendimento, "Box de banheiro");
        assertEquals(EtapaFluxo.COLETA_DESCRICAO, atendimento.getEtapaFluxo());

        fluxo.processarMensagem(atendimento, "1,20m x 1,90m, bairro Centro");
        assertEquals(EtapaFluxo.CONFIRMACAO, atendimento.getEtapaFluxo());
        assertEquals(StatusAtendimento.EM_FLUXO_BOT, atendimento.getStatus());

        fluxo.processarMensagem(atendimento, "sim");

        assertEquals(StatusAtendimento.AGUARDANDO_ATENDENTE, atendimento.getStatus());
        assertNotNull(atendimento.getSolicitacaoOrcamento());
        assertTrue(atendimento.getSolicitacaoOrcamento().getDescricao().contains("Box de banheiro"));

        verify(clienteRepository, times(1)).save(any());
        verify(solicitacaoOrcamentoRepository, times(1)).save(any());
    }

    @Test
    void pedirAtendenteNoMenuEscalaImediatamente() {

        atendimento.setEtapaFluxo(EtapaFluxo.MENU);

        fluxo.processarMensagem(atendimento, "quero falar com atendente");

        assertEquals(StatusAtendimento.AGUARDANDO_ATENDENTE, atendimento.getStatus());
    }

    @Test
    void tresRespostasInvalidasNoMenuEscalaParaAtendente() {

        atendimento.setEtapaFluxo(EtapaFluxo.MENU);

        fluxo.processarMensagem(atendimento, "blablabla");
        assertEquals(StatusAtendimento.EM_FLUXO_BOT, atendimento.getStatus());
        assertEquals(1, atendimento.getTentativasErro());

        fluxo.processarMensagem(atendimento, "xyz");
        assertEquals(StatusAtendimento.EM_FLUXO_BOT, atendimento.getStatus());
        assertEquals(2, atendimento.getTentativasErro());

        fluxo.processarMensagem(atendimento, "???");

        assertEquals(StatusAtendimento.AGUARDANDO_ATENDENTE, atendimento.getStatus());
    }

    @Test
    void comandoGlobalNoveEscalaParaAtendenteEmQualquerEtapa() {

        atendimento.setEtapaFluxo(EtapaFluxo.COLETA_SERVICO);

        fluxo.processarMensagem(atendimento, "9");

        assertEquals(StatusAtendimento.AGUARDANDO_ATENDENTE, atendimento.getStatus());
    }

    @Test
    void comandoGlobalZeroVoltaAoMenuDurantePreenchimentoDeDados() {

        atendimento.setEtapaFluxo(EtapaFluxo.COLETA_DESCRICAO);
        atendimento.setDadosColetados("Serviço desejado: Box de banheiro");

        String resposta = fluxo.processarMensagem(atendimento, "0");

        assertEquals(EtapaFluxo.MENU, atendimento.getEtapaFluxo());
        assertTrue(resposta.toLowerCase().contains("menu"));
    }

    @Test
    void comandoGlobalZeroNoMenuNaoTemEfeitoEspecialCaiNoTratamentoNormal() {

        atendimento.setEtapaFluxo(EtapaFluxo.MENU);

        fluxo.processarMensagem(atendimento, "0");

        assertEquals(StatusAtendimento.EM_FLUXO_BOT, atendimento.getStatus());
        assertEquals(1, atendimento.getTentativasErro());
    }

    @Test
    void consultarOrcamentoSemClienteAvisaQueNaoEncontrou() {

        atendimento.setEtapaFluxo(EtapaFluxo.MENU);

        String resposta = fluxo.processarMensagem(atendimento, "2");

        assertTrue(resposta.toLowerCase().contains("não encontrei"));
        assertEquals(EtapaFluxo.MENU, atendimento.getEtapaFluxo());
    }

    @Test
    void consultarOrcamentoComOrcamentoEnviadoDevolveStatusEValor() {

        Cliente cliente = new Cliente();
        cliente.setNome("Maria Silva");
        atendimento.setCliente(cliente);
        atendimento.setEtapaFluxo(EtapaFluxo.MENU);

        Orcamento orcamento = new Orcamento();
        orcamento.setStatus(StatusOrcamento.ENVIADO);
        orcamento.setValorTotal(new BigDecimal("1850.00"));

        when(orcamentoRepository.findAllByEmpresaIdAndClienteIdOrderByCriadoEmDesc(any(), any()))
                .thenReturn(List.of(orcamento));

        String resposta = fluxo.processarMensagem(atendimento, "2");

        assertTrue(resposta.contains("aguardando sua aprovação"));
        assertTrue(resposta.contains("1.850,00"));
    }

    private Orcamento orcamento(long id, StatusOrcamento status, String valor) {

        Orcamento orcamento = new Orcamento();
        ReflectionTestUtils.setField(orcamento, "id", id);
        orcamento.setStatus(status);
        orcamento.setValorTotal(valor != null ? new BigDecimal(valor) : null);
        orcamento.setValidoAte(LocalDate.of(2026, 9, 30));

        return orcamento;
    }

    private PerguntaPendente aprovacao(long orcamentoId) {

        PerguntaPendente pergunta = new PerguntaPendente();
        pergunta.setTipo(TipoPergunta.APROVAR_ORCAMENTO);
        pergunta.setReferenciaId(orcamentoId);

        return pergunta;
    }

    @Test
    void consultarListaTodosOsOrcamentosEmAbertoEAnexaAAprovacaoDoEnviado() {

        atendimento.setCliente(new Cliente());
        atendimento.setEtapaFluxo(EtapaFluxo.MENU);

        PerguntaPendente aprovar11 = aprovacao(11L);

        when(orcamentoRepository.findAllByEmpresaIdAndClienteIdOrderByCriadoEmDesc(any(), any()))
                .thenReturn(List.of(orcamento(12L, StatusOrcamento.NOVO_CONTATO, null),
                        orcamento(11L, StatusOrcamento.ENVIADO, "1850.00"),
                        orcamento(9L, StatusOrcamento.PERDIDO, "900.00")));
        when(perguntaPendenteService.ativas(any(), any())).thenReturn(List.of(aprovar11));

        RespostaFluxo resposta = fluxo.responder(atendimento, "2");

        assertTrue(resposta.texto().contains("nº 12"), resposta.texto());
        assertTrue(resposta.texto().contains("nº 11"), resposta.texto());
        assertTrue(resposta.texto().contains("1.850,00"), resposta.texto());
        assertTrue(!resposta.texto().contains("nº 9"), "perdido não aparece com outros em aberto");
        assertEquals(aprovar11, resposta.pergunta());
        assertTrue(resposta.escolher().isEmpty());
    }

    @Test
    void consultarComDoisOrcamentosAguardandoAprovacaoPedeParaEscolher() {

        atendimento.setCliente(new Cliente());
        atendimento.setEtapaFluxo(EtapaFluxo.MENU);

        PerguntaPendente aprovar11 = aprovacao(11L);
        PerguntaPendente aprovar12 = aprovacao(12L);

        when(orcamentoRepository.findAllByEmpresaIdAndClienteIdOrderByCriadoEmDesc(any(), any()))
                .thenReturn(List.of(orcamento(12L, StatusOrcamento.ENVIADO, "500.00"),
                        orcamento(11L, StatusOrcamento.ENVIADO, "1850.00")));
        when(perguntaPendenteService.ativas(any(), any())).thenReturn(List.of(aprovar11, aprovar12));

        RespostaFluxo resposta = fluxo.responder(atendimento, "2");

        assertEquals(List.of(aprovar12, aprovar11), resposta.escolher());
        assertEquals(null, resposta.pergunta());
        assertTrue(resposta.texto().contains("500,00") && resposta.texto().contains("1.850,00"), resposta.texto());
    }

    @Test
    void pedidoDeOrcamentoNaPrimeiraMensagemPulaOMenuDepoisDoNome() {

        String saudacao = fluxo.iniciarConversa(atendimento, "Oi, quero orçamento de box");
        assertTrue(saudacao.contains("nome"));

        String resposta = fluxo.processarMensagem(atendimento, "Carlos Souza");

        assertEquals(EtapaFluxo.COLETA_DESCRICAO, atendimento.getEtapaFluxo());
        assertTrue(resposta.contains("box de banheiro"), resposta);
        assertTrue(resposta.contains("largura x altura"), resposta);
    }

    @Test
    void pedidoSemServicoNaPrimeiraMensagemPerguntaOServicoDepoisDoNome() {

        fluxo.iniciarConversa(atendimento, "bom dia, queria um orçamento");
        fluxo.processarMensagem(atendimento, "Ana");

        assertEquals(EtapaFluxo.COLETA_SERVICO, atendimento.getEtapaFluxo());
    }

    @Test
    void saudacaoSemPedidoContinuaNoMenu() {

        fluxo.iniciarConversa(atendimento, "oi");
        fluxo.processarMensagem(atendimento, "Ana");

        assertEquals(EtapaFluxo.MENU, atendimento.getEtapaFluxo());
    }

    @Test
    void duvidaCadastradaNaPrimeiraMensagemEhRespondidaAntesDoNome() {

        when(perguntaFrequenteService.encontrar(any(), any())).thenReturn(Optional.of(duvida("Fazemos sim!")));

        String resposta = fluxo.iniciarConversa(atendimento, "vocês fazem espelho sob medida?");

        assertTrue(resposta.contains("Fazemos sim!"), resposta);
        assertTrue(resposta.contains("nome"), resposta);
        assertEquals(EtapaFluxo.COLETA_NOME, atendimento.getEtapaFluxo());
    }

    @Test
    void duvidaNoMenuEhRespondidaEDuvidaSemRespostaVaiParaAtendente() {

        fluxo.iniciarConversa(atendimento);
        fluxo.processarMensagem(atendimento, "Ana");

        when(perguntaFrequenteService.encontrar(any(), any())).thenReturn(Optional.of(duvida("Zona sul e centro.")));
        String respondida = fluxo.processarMensagem(atendimento, "atendem no meu bairro?");

        assertTrue(respondida.contains("Zona sul e centro."), respondida);
        assertEquals(EtapaFluxo.MENU, atendimento.getEtapaFluxo());

        when(perguntaFrequenteService.encontrar(any(), any())).thenReturn(Optional.empty());
        fluxo.processarMensagem(atendimento, "4");
        assertEquals(EtapaFluxo.DUVIDA, atendimento.getEtapaFluxo());

        String semResposta = fluxo.processarMensagem(atendimento, "vocês parcelam?");

        assertEquals(StatusAtendimento.AGUARDANDO_ATENDENTE, atendimento.getStatus());
        assertTrue(semResposta.contains("equipe"), semResposta);
    }

    @Test
    void confirmacaoMostraAMedidaEntendidaEOPedidoViraRascunho() {

        fluxo.iniciarConversa(atendimento);
        fluxo.processarMensagem(atendimento, "Ana");
        fluxo.processarMensagem(atendimento, "1");
        fluxo.processarMensagem(atendimento, "box");

        String confirmacao = fluxo.processarMensagem(atendimento, "120x190, bairro Centro");

        assertTrue(confirmacao.contains("1,20 m × 1,90 m"), confirmacao);

        fluxo.processarMensagem(atendimento, "1");

        verify(eventos).publishEvent(any(SolicitacaoRecebidaPeloBotEvent.class));
    }

    @Test
    void pedirAtendenteForaDoHorarioAvisaQuandoVaiSerRespondido() {

        when(horarioAtendimento.avisoForaDoHorario(any())).thenReturn(Optional.of("Respondemos amanhã às 8h."));

        fluxo.iniciarConversa(atendimento);
        fluxo.processarMensagem(atendimento, "Ana");

        String resposta = fluxo.processarMensagem(atendimento, "3");

        assertTrue(resposta.contains("Respondemos amanhã às 8h."), resposta);
    }

    @Test
    void consultaDoOrcamentoNoMenuNaoAbrePedidoNovo() {

        fluxo.iniciarConversa(atendimento);
        fluxo.processarMensagem(atendimento, "Ana");
        fluxo.processarMensagem(atendimento, "e aquele orçamento?");

        assertEquals(EtapaFluxo.MENU, atendimento.getEtapaFluxo());
    }

    private static PerguntaFrequente duvida(String resposta) {

        PerguntaFrequente pergunta = new PerguntaFrequente();
        pergunta.setResposta(resposta);
        return pergunta;
    }
}
