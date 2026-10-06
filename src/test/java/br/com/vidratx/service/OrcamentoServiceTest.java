package br.com.vidratx.service;

import br.com.vidratx.entity.Cliente;
import br.com.vidratx.entity.Empresa;
import br.com.vidratx.entity.Orcamento;
import br.com.vidratx.entity.SolicitacaoOrcamento;
import br.com.vidratx.entity.Usuario;
import br.com.vidratx.exception.SolicitacaoOrcamentoNaoEncontradaException;
import br.com.vidratx.mapper.AtendimentoWhatsappMapper;
import br.com.vidratx.mapper.OrcamentoMapper;
import br.com.vidratx.repository.AtendimentoWhatsappRepository;
import br.com.vidratx.repository.ClienteRepository;
import br.com.vidratx.repository.EmpresaRepository;
import br.com.vidratx.repository.MedicaoRepository;
import br.com.vidratx.repository.MensagemAtendimentoRepository;
import br.com.vidratx.repository.OrcamentoItemRepository;
import br.com.vidratx.repository.OrcamentoRepository;
import br.com.vidratx.repository.OrdemServicoRepository;
import br.com.vidratx.repository.SolicitacaoOrcamentoRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class OrcamentoServiceTest {

    private static final Long EMPRESA_ID = 1L;
    private static final Long SOLICITACAO_ID = 42L;

    private OrcamentoRepository orcamentoRepository;
    private SolicitacaoOrcamentoRepository solicitacaoOrcamentoRepository;
    private EmpresaRepository empresaRepository;
    private HistoricoService historicoService;
    private OrcamentoService orcamentoService;

    @BeforeEach
    void montarCenario() {

        orcamentoRepository = mock(OrcamentoRepository.class);
        solicitacaoOrcamentoRepository = mock(SolicitacaoOrcamentoRepository.class);
        empresaRepository = mock(EmpresaRepository.class);
        historicoService = mock(HistoricoService.class);

        orcamentoService = new OrcamentoService(
                orcamentoRepository,
                empresaRepository,
                mock(ClienteRepository.class),
                solicitacaoOrcamentoRepository,
                mock(AtendimentoWhatsappRepository.class),
                mock(MensagemAtendimentoRepository.class),
                mock(OrcamentoItemRepository.class),
                mock(MedicaoRepository.class),
                mock(OrdemServicoRepository.class),
                new OrcamentoMapper(),
                new AtendimentoWhatsappMapper(),
                historicoService,
                mock(OrcamentoCalculoService.class),
                mock(ParametroCalculoService.class),
                mock(PerguntaPendenteService.class),
                mock(WhatsappSaidaService.class),
                mock(WhatsappContatoService.class),
                mock(ConversaWhatsappService.class),
                mock(NegociacaoAgendamentoService.class),
                mock(MedicaoService.class),
                mock(OrdemServicoService.class),
                mock(PipelineOrcamentoService.class),
                Clock.systemDefaultZone()
        );
    }

    @Test
    void naoVazaOrcamentoDeOutraEmpresaQuandoSolicitacaoNaoPertenceAoChamador() {

        Empresa outraEmpresa = new Empresa();

        Orcamento orcamentoDeOutraEmpresa = new Orcamento();
        orcamentoDeOutraEmpresa.setEmpresa(outraEmpresa);

        when(solicitacaoOrcamentoRepository.findByIdAndEmpresaId(SOLICITACAO_ID, EMPRESA_ID))
                .thenReturn(Optional.empty());

        when(orcamentoRepository.findBySolicitacaoOrcamentoId(SOLICITACAO_ID))
                .thenReturn(Optional.of(orcamentoDeOutraEmpresa));

        assertThrows(
                SolicitacaoOrcamentoNaoEncontradaException.class,
                () -> orcamentoService.criarAPartirDeSolicitacao(
                        EMPRESA_ID, SOLICITACAO_ID, mock(Usuario.class)
                )
        );

        verify(orcamentoRepository, never()).findBySolicitacaoOrcamentoId(any());
    }

    @Test
    void reaproveitaOrcamentoExistenteQuandoSolicitacaoPertenceAoChamador() {

        Empresa empresa = new Empresa();

        SolicitacaoOrcamento solicitacao = new SolicitacaoOrcamento();

        Orcamento orcamentoExistente = new Orcamento();
        orcamentoExistente.setEmpresa(empresa);

        when(solicitacaoOrcamentoRepository.findByIdAndEmpresaId(SOLICITACAO_ID, EMPRESA_ID))
                .thenReturn(Optional.of(solicitacao));

        when(orcamentoRepository.findBySolicitacaoOrcamentoId(SOLICITACAO_ID))
                .thenReturn(Optional.of(orcamentoExistente));

        orcamentoService.criarAPartirDeSolicitacao(EMPRESA_ID, SOLICITACAO_ID, mock(Usuario.class));

        verify(orcamentoRepository, never()).save(any());
    }

    @Test
    void criaOrcamentoQuandoSolicitacaoPertenceAoChamadorENaoExisteAinda() {

        Empresa empresa = new Empresa();

        Cliente cliente = new Cliente();

        SolicitacaoOrcamento solicitacao = new SolicitacaoOrcamento();
        solicitacao.setCliente(cliente);

        when(solicitacaoOrcamentoRepository.findByIdAndEmpresaId(SOLICITACAO_ID, EMPRESA_ID))
                .thenReturn(Optional.of(solicitacao));

        when(orcamentoRepository.findBySolicitacaoOrcamentoId(SOLICITACAO_ID))
                .thenReturn(Optional.empty());

        when(empresaRepository.findById(EMPRESA_ID))
                .thenReturn(Optional.of(empresa));

        when(orcamentoRepository.save(any()))
                .thenAnswer(invocacao -> invocacao.getArgument(0));

        orcamentoService.criarAPartirDeSolicitacao(EMPRESA_ID, SOLICITACAO_ID, mock(Usuario.class));

        verify(orcamentoRepository).save(any());
    }
}
