package br.com.vidratx.service;

import br.com.vidratx.config.FusoEmpresa;
import br.com.vidratx.dto.InstalacaoReagendarRequest;
import br.com.vidratx.dto.InstalacaoRealizarRequest;
import br.com.vidratx.dto.InstalacaoRequest;
import br.com.vidratx.dto.InstalacaoResponse;
import br.com.vidratx.dto.PropostaAgendamentoResponse;
import br.com.vidratx.dto.RecusarContrapropostaRequest;
import br.com.vidratx.entity.AtendimentoWhatsapp;
import br.com.vidratx.entity.Instalacao;
import br.com.vidratx.entity.MensagemSaida;
import br.com.vidratx.entity.Orcamento;
import br.com.vidratx.entity.OrdemServico;
import br.com.vidratx.entity.PerguntaPendente;
import br.com.vidratx.entity.Usuario;
import br.com.vidratx.enums.CategoriaMensagemSaida;
import br.com.vidratx.enums.RemetenteMensagem;
import br.com.vidratx.enums.StatusAgendamento;
import br.com.vidratx.enums.StatusAtendimento;
import br.com.vidratx.enums.StatusProducao;
import br.com.vidratx.enums.TipoEventoHistorico;
import br.com.vidratx.exception.InstalacaoJaExisteException;
import br.com.vidratx.exception.InstalacaoNaoEncontradaException;
import br.com.vidratx.exception.InstalacaoNaoPermitidaException;
import br.com.vidratx.exception.OrdemServicoNaoEncontradaException;
import br.com.vidratx.exception.TransicaoInvalidaException;
import br.com.vidratx.mapper.InstalacaoMapper;
import br.com.vidratx.repository.InstalacaoRepository;
import br.com.vidratx.repository.OrdemServicoRepository;
import br.com.vidratx.service.NegociacaoAgendamentoService.ProximaAcao;
import br.com.vidratx.service.NegociacaoAgendamentoService.ResultadoConfirmacao;
import br.com.vidratx.service.WhatsappSaidaService.NovaMensagem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class InstalacaoService {

    private static final long FOLGA_FUSOS_HORAS = 18;

    private static final Logger log =
            LoggerFactory.getLogger(InstalacaoService.class);

    private final InstalacaoRepository instalacaoRepository;
    private final OrdemServicoRepository ordemServicoRepository;
    private final InstalacaoMapper instalacaoMapper;
    private final HistoricoService historicoService;
    private final NegociacaoAgendamentoService negociacao;
    private final WhatsappSaidaService whatsappSaidaService;
    private final ConversaWhatsappService conversaWhatsappService;
    private final PerguntaPendenteService perguntaPendenteService;
    private final WhatsappContatoService whatsappContatoService;
    private final TransactionTemplate transactionTemplate;
    private final Clock clock;
    private final FusoEmpresa fuso;

    public InstalacaoService(
            InstalacaoRepository instalacaoRepository,
            OrdemServicoRepository ordemServicoRepository,
            InstalacaoMapper instalacaoMapper,
            HistoricoService historicoService,
            NegociacaoAgendamentoService negociacao,
            WhatsappSaidaService whatsappSaidaService,
            ConversaWhatsappService conversaWhatsappService,
            PerguntaPendenteService perguntaPendenteService,
            WhatsappContatoService whatsappContatoService,
            PlatformTransactionManager transactionManager,
            Clock clock) {

        this.instalacaoRepository = instalacaoRepository;
        this.ordemServicoRepository = ordemServicoRepository;
        this.instalacaoMapper = instalacaoMapper;
        this.historicoService = historicoService;
        this.negociacao = negociacao;
        this.whatsappSaidaService = whatsappSaidaService;
        this.conversaWhatsappService = conversaWhatsappService;
        this.perguntaPendenteService = perguntaPendenteService;
        this.whatsappContatoService = whatsappContatoService;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.clock = clock;
        this.fuso = new FusoEmpresa(clock);
    }

    @Transactional(readOnly = true)
    public InstalacaoResponse buscar(Long empresaId, Long ordemServicoId) {

        buscarOrdemServico(empresaId, ordemServicoId);

        return montarResponse(buscarInstalacao(ordemServicoId), null);
    }

    @Transactional(readOnly = true)
    public List<PropostaAgendamentoResponse> propostas(Long empresaId, Long ordemServicoId) {

        buscarOrdemServico(empresaId, ordemServicoId);

        return negociacao.historicoPropostas(buscarInstalacao(ordemServicoId)).stream()
                .map(AgendamentoMapperSupport::toPropostaResponse)
                .toList();
    }

    @Transactional
    public InstalacaoResponse agendar(
            Long empresaId,
            Long ordemServicoId,
            InstalacaoRequest request,
            Usuario responsavel) {

        OrdemServico ordemServico = buscarOrdemServico(empresaId, ordemServicoId);

        if (Boolean.TRUE.equals(ordemServico.getNecessitaProducao())
                && ordemServico.getStatusProducao() != StatusProducao.CONFERIDO) {

            throw new InstalacaoNaoPermitidaException(
                    "A produção precisa estar conferida antes de agendar a instalação"
            );
        }

        negociacao.travarContato(ordemServico.getOrcamento());

        Instalacao instalacao = instalacaoRepository.findByOrdemServicoId(ordemServicoId).orElse(null);

        if (instalacao != null && instalacao.getStatus() != StatusAgendamento.CANCELADA) {
            throw new InstalacaoJaExisteException(
                    "Esta ordem de serviço já possui uma instalação em andamento — use \"Propor outra data\""
            );
        }

        if (instalacao == null) {

            instalacao = instalacaoMapper.toEntity(request, ordemServico);

        } else {

            instalacao.setEndereco(normalizar(request.getEndereco()));
            instalacao.setObservacoes(normalizar(request.getObservacoes()));
            instalacao.setEquipeResponsavel(normalizar(request.getEquipeResponsavel()));
            instalacao.setDataRealizada(null);
            instalacao.setContrapropostaTexto(null);
        }

        instalacao.setDataAgendada(request.getDataAgendada());
        instalacao.setStatus(StatusAgendamento.PROPOSTA_ENVIADA);
        instalacao = instalacaoRepository.saveAndFlush(instalacao);

        negociacao.proporData(instalacao, request.getDataAgendada(), responsavel, null);

        return montarResponse(instalacao, negociacao.avisoEnvio(ordemServico.getOrcamento()));
    }

    @Transactional
    public InstalacaoResponse reagendar(
            Long empresaId,
            Long ordemServicoId,
            InstalacaoReagendarRequest request,
            Usuario responsavel) {

        OrdemServico ordemServico = buscarOrdemServico(empresaId, ordemServicoId);

        negociacao.travarContato(ordemServico.getOrcamento());

        Instalacao instalacao = buscarInstalacao(ordemServicoId);

        exigirStatusEmAny(instalacao, NegociacaoAgendamentoService.STATUS_PODE_REAGENDAR_OU_CANCELAR);

        if (request.getEquipeResponsavel() != null) {
            instalacao.setEquipeResponsavel(normalizar(request.getEquipeResponsavel()));
        }

        negociacao.proporData(instalacao, request.getDataAgendada(), responsavel, null);

        return montarResponse(instalacao, negociacao.avisoEnvio(ordemServico.getOrcamento()));
    }

    @Transactional
    public InstalacaoResponse recusarContraproposta(
            Long empresaId,
            Long ordemServicoId,
            RecusarContrapropostaRequest request,
            Usuario responsavel) {

        OrdemServico ordemServico = buscarOrdemServico(empresaId, ordemServicoId);

        negociacao.travarContato(ordemServico.getOrcamento());

        Instalacao instalacao = buscarInstalacao(ordemServicoId);

        exigirStatus(instalacao, StatusAgendamento.CONTRAPROPOSTA_CLIENTE);

        if (request.getEquipeResponsavel() != null) {
            instalacao.setEquipeResponsavel(normalizar(request.getEquipeResponsavel()));
        }

        negociacao.proporData(
                instalacao, request.getDataAgendada(), responsavel,
                request.getMotivo() == null || request.getMotivo().isBlank()
                        ? "não temos disponibilidade nesse horário"
                        : request.getMotivo()
        );

        return montarResponse(instalacao, negociacao.avisoEnvio(ordemServico.getOrcamento()));
    }

    @Transactional
    public InstalacaoResponse aceitarContraproposta(
            Long empresaId,
            Long ordemServicoId,
            InstalacaoReagendarRequest request,
            Usuario responsavel) {

        OrdemServico ordemServico = buscarOrdemServico(empresaId, ordemServicoId);

        negociacao.travarContato(ordemServico.getOrcamento());

        Instalacao instalacao = buscarInstalacao(ordemServicoId);

        if (request.getEquipeResponsavel() != null) {
            instalacao.setEquipeResponsavel(normalizar(request.getEquipeResponsavel()));
        }

        negociacao.aceitarContraproposta(instalacao, request.getDataAgendada(), responsavel);

        return montarResponse(instalacao, null);
    }

    @Transactional
    public InstalacaoResponse confirmarManualmente(
            Long empresaId,
            Long ordemServicoId,
            Usuario responsavel) {

        OrdemServico ordemServico = buscarOrdemServico(empresaId, ordemServicoId);

        negociacao.travarContato(ordemServico.getOrcamento());

        Instalacao instalacao = buscarInstalacao(ordemServicoId);

        negociacao.confirmarManualmente(instalacao, responsavel);

        return montarResponse(instalacao, null);
    }

    @Transactional
    public InstalacaoResponse realizar(
            Long empresaId,
            Long ordemServicoId,
            InstalacaoRealizarRequest request,
            Usuario responsavel) {

        OrdemServico ordemServico = buscarOrdemServico(empresaId, ordemServicoId);

        negociacao.travarContato(ordemServico.getOrcamento());

        Instalacao instalacao = buscarInstalacao(ordemServicoId);

        exigirStatus(instalacao, StatusAgendamento.AGENDADA);

        if (instalacao.getDataAgendada().toLocalDate().isAfter(fuso.hoje(ordemServico.getEmpresa()))) {
            throw new TransicaoInvalidaException(
                    "A instalação está marcada para " + NegociacaoAgendamentoService.formatar(instalacao.getDataAgendada())
                            + " — só pode ser marcada como realizada a partir do dia agendado"
            );
        }

        instalacao.setStatus(StatusAgendamento.REALIZADA);
        negociacao.limparPedidoDeCancelamento(instalacao);
        instalacao.setDataRealizada(LocalDateTime.now(clock));

        if (request.getChecklist() != null) {
            instalacao.setChecklist(normalizar(request.getChecklist()));
        }

        if (request.getObservacoes() != null) {
            instalacao.setObservacoes(normalizar(request.getObservacoes()));
        }

        Instalacao salva = instalacaoRepository.saveAndFlush(instalacao);

        historicoService.registrarEventoInstalacao(
                ordemServico.getEmpresa(), ordemServico.getOrcamento().getCliente(), salva,
                TipoEventoHistorico.INSTALACAO_REALIZADA, "Instalação realizada", responsavel
        );

        concluirAtendimento(ordemServico.getOrcamento());

        return montarResponse(salva, null);
    }

    @Transactional
    public InstalacaoResponse cancelar(
            Long empresaId,
            Long ordemServicoId,
            String motivo,
            Usuario responsavel) {

        OrdemServico ordemServico = buscarOrdemServico(empresaId, ordemServicoId);

        negociacao.travarContato(ordemServico.getOrcamento());

        Instalacao instalacao = buscarInstalacao(ordemServicoId);

        negociacao.cancelar(instalacao, responsavel, motivo);

        return montarResponse(instalacao, null);
    }

    @Transactional
    public InstalacaoResponse manterData(Long empresaId, Long ordemServicoId, Usuario responsavel) {

        OrdemServico ordemServico = buscarOrdemServico(empresaId, ordemServicoId);

        negociacao.travarContato(ordemServico.getOrcamento());

        Instalacao instalacao = buscarInstalacao(ordemServicoId);

        negociacao.manterData(instalacao, responsavel);

        return montarResponse(instalacao, null);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void registrarPedidoDeCancelamento(Instalacao instalacao, String texto) {
        negociacao.registrarPedidoDeCancelamento(instalacao, texto);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public ResultadoConfirmacao confirmarPeloCliente(Instalacao instalacao, PerguntaPendente pergunta, AtendimentoWhatsapp atendimento) {
        return negociacao.confirmarPeloCliente(instalacao, pergunta, atendimento);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void recusarPeloCliente(Instalacao instalacao, PerguntaPendente pergunta, AtendimentoWhatsapp atendimento) {
        negociacao.recusarPeloCliente(instalacao, pergunta, atendimento);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void registrarSugestaoCliente(Instalacao instalacao, PerguntaPendente pergunta, String texto, AtendimentoWhatsapp atendimento) {
        negociacao.registrarSugestaoCliente(instalacao, pergunta, texto, atendimento);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void solicitarReagendamentoPeloCliente(Instalacao instalacao, String texto, boolean temData, AtendimentoWhatsapp atendimento) {
        negociacao.solicitarReagendamentoPeloCliente(instalacao, texto, temData, atendimento);
    }

    @Scheduled(fixedDelayString = "PT10M", initialDelayString = "PT4M")
    public void expirarPropostasVencidas() {

        List<Long> ids = transactionTemplate.execute(status ->
                instalacaoRepository
                        .findTop200ByStatusAndDataAgendadaBeforeOrderByDataAgendadaAsc(
                                StatusAgendamento.PROPOSTA_ENVIADA, LocalDateTime.now(clock).plusHours(FOLGA_FUSOS_HORAS))
                        .stream().map(Instalacao::getId).toList()
        );

        if (ids == null) {
            return;
        }

        for (Long id : ids) {

            try {

                transactionTemplate.executeWithoutResult(status -> instalacaoRepository.findById(id).ifPresent(i -> {
                    if (i.getDataAgendada().isBefore(negociacao.agoraNaEmpresa(i))) {
                        negociacao.travarContato(i.getOrdemServico().getOrcamento());
                        negociacao.expirarProposta(i, null, false);
                    }
                }));

            } catch (RuntimeException ex) {
                log.warn("Não foi possível expirar a proposta da instalação {}", id, ex);
            }
        }
    }

    private void concluirAtendimento(Orcamento orcamento) {

        String telefone = NegociacaoAgendamentoService.telefoneDoCliente(orcamento.getCliente());

        if (telefone == null) {
            return;
        }

        whatsappContatoService.travar(orcamento.getEmpresa(), telefone);

        AtendimentoWhatsapp conversa = conversaWhatsappService
                .obterConversaAberta(orcamento.getEmpresa(), telefone, orcamento.getCliente())
                .atendimento();

        boolean clienteEsperandoResposta = conversa.getUltimaMensagemClienteEm() != null
                && (conversa.getUltimaMensagemEmpresaEm() == null
                || conversa.getUltimaMensagemClienteEm().isAfter(conversa.getUltimaMensagemEmpresaEm()));

        whatsappSaidaService.enfileirar(new NovaMensagem(
                orcamento.getEmpresa(), telefone, orcamento.getCliente(),
                "Instalação concluída! Muito obrigado pela confiança na "
                        + orcamento.getEmpresa().getNomeFantasia()
                        + ". Se precisar de qualquer coisa, é só mandar uma mensagem por aqui.",
                CategoriaMensagemSaida.CONCLUSAO, RemetenteMensagem.BOT,
                "ORCAMENTO", orcamento.getId(), null, conversa
        ));

        boolean semPendencias = perguntaPendenteService.ativas(orcamento.getEmpresa().getId(), telefone).isEmpty();

        if (semPendencias && !clienteEsperandoResposta
                && conversa.getStatus() != StatusAtendimento.EM_ATENDIMENTO_HUMANO) {

            conversaWhatsappService.encerrar(
                    conversa, ConversaWhatsappService.MOTIVO_PROCESSO_CONCLUIDO, null,
                    "Conversa encerrada automaticamente: instalação concluída e nenhuma pendência com o cliente"
            );
        }
    }

    InstalacaoResponse montarResponse(Instalacao instalacao, String aviso) {

        InstalacaoResponse response = instalacaoMapper.toResponse(instalacao);
        MensagemSaida envio = negociacao.envioDaUltimaProposta(instalacao).orElse(null);
        ProximaAcao proxima = negociacao.proximaAcao(instalacao, envio);

        response.setVersaoProposta(instalacao.getVersaoProposta());
        response.setEnvioStatus(AgendamentoMapperSupport.statusEnvio(envio, instalacao.getOrcamentoReferencia().getCliente()));
        response.setEnvioErro(envio != null ? envio.getUltimoErro() : null);
        response.setResponsavelProximaAcao(proxima.responsavel());
        response.setProximaAcao(proxima.descricao());
        response.setAviso(aviso);
        response.setCancelamentoSolicitadoEm(instalacao.getCancelamentoSolicitadoEm());
        response.setCancelamentoSolicitadoTexto(instalacao.getCancelamentoSolicitadoTexto());

        negociacao.sugestaoPendente(instalacao).ifPresent(sugestao -> {
            response.setContrapropostaData(sugestao.getDataProposta());
            response.setContrapropostaEm(sugestao.getCriadoEm());
        });

        return response;
    }

    private void exigirStatus(Instalacao instalacao, StatusAgendamento esperado) {

        if (instalacao.getStatus() != esperado) {
            throw new TransicaoInvalidaException(
                    "Esta ação exige que a instalação esteja com status " + esperado + ", mas está " + instalacao.getStatus()
            );
        }
    }

    private void exigirStatusEmAny(Instalacao instalacao, Set<StatusAgendamento> permitidos) {

        if (!permitidos.contains(instalacao.getStatus())) {
            throw new TransicaoInvalidaException(
                    "Esta ação não é permitida com a instalação no status " + instalacao.getStatus()
            );
        }
    }

    private OrdemServico buscarOrdemServico(Long empresaId, Long ordemServicoId) {

        return ordemServicoRepository
                .findByIdAndEmpresaId(ordemServicoId, empresaId)
                .orElseThrow(() -> new OrdemServicoNaoEncontradaException("Ordem de serviço não encontrada"));
    }

    private Instalacao buscarInstalacao(Long ordemServicoId) {

        return instalacaoRepository
                .findByOrdemServicoId(ordemServicoId)
                .orElseThrow(() -> new InstalacaoNaoEncontradaException(
                        "Instalação não encontrada para esta ordem de serviço"
                ));
    }

    private String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }
}
