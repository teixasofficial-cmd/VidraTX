package br.com.vidratx.service;

import br.com.vidratx.config.FusoEmpresa;
import br.com.vidratx.dto.MedicaoReagendarRequest;
import br.com.vidratx.dto.MedicaoRealizarRequest;
import br.com.vidratx.dto.MedicaoRequest;
import br.com.vidratx.dto.MedicaoResponse;
import br.com.vidratx.dto.PropostaAgendamentoResponse;
import br.com.vidratx.dto.RecusarContrapropostaRequest;
import br.com.vidratx.entity.AtendimentoWhatsapp;
import br.com.vidratx.entity.Medicao;
import br.com.vidratx.entity.MensagemSaida;
import br.com.vidratx.entity.Orcamento;
import br.com.vidratx.entity.PerguntaPendente;
import br.com.vidratx.entity.Usuario;
import br.com.vidratx.enums.StatusAgendamento;
import br.com.vidratx.enums.TipoEventoHistorico;
import br.com.vidratx.exception.MedicaoJaExisteException;
import br.com.vidratx.exception.MedicaoNaoEncontradaException;
import br.com.vidratx.exception.OrcamentoNaoEncontradoException;
import br.com.vidratx.exception.TransicaoInvalidaException;
import br.com.vidratx.mapper.MedicaoMapper;
import br.com.vidratx.repository.MedicaoRepository;
import br.com.vidratx.repository.OrcamentoRepository;
import br.com.vidratx.service.NegociacaoAgendamentoService.ProximaAcao;
import br.com.vidratx.service.NegociacaoAgendamentoService.ResultadoConfirmacao;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Service
public class MedicaoService {

    private static final long FOLGA_FUSOS_HORAS = 18;

    private static final Logger log =
            LoggerFactory.getLogger(MedicaoService.class);

    private final MedicaoRepository medicaoRepository;
    private final OrcamentoRepository orcamentoRepository;
    private final MedicaoMapper medicaoMapper;
    private final HistoricoService historicoService;
    private final NegociacaoAgendamentoService negociacao;
    private final PipelineOrcamentoService pipeline;
    private final TransactionTemplate transactionTemplate;
    private final Clock clock;
    private final FusoEmpresa fuso;

    public MedicaoService(
            MedicaoRepository medicaoRepository,
            OrcamentoRepository orcamentoRepository,
            MedicaoMapper medicaoMapper,
            HistoricoService historicoService,
            NegociacaoAgendamentoService negociacao,
            PipelineOrcamentoService pipeline,
            PlatformTransactionManager transactionManager,
            Clock clock) {

        this.medicaoRepository = medicaoRepository;
        this.orcamentoRepository = orcamentoRepository;
        this.medicaoMapper = medicaoMapper;
        this.historicoService = historicoService;
        this.negociacao = negociacao;
        this.pipeline = pipeline;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.clock = clock;
        this.fuso = new FusoEmpresa(clock);
    }

    @Transactional(readOnly = true)
    public MedicaoResponse buscar(Long empresaId, Long orcamentoId) {

        buscarOrcamento(empresaId, orcamentoId);

        return montarResponse(buscarMedicao(orcamentoId), null);
    }

    @Transactional(readOnly = true)
    public List<PropostaAgendamentoResponse> propostas(Long empresaId, Long orcamentoId) {

        buscarOrcamento(empresaId, orcamentoId);

        return negociacao.historicoPropostas(buscarMedicao(orcamentoId)).stream()
                .map(AgendamentoMapperSupport::toPropostaResponse)
                .toList();
    }

    @Transactional
    public MedicaoResponse agendar(
            Long empresaId,
            Long orcamentoId,
            MedicaoRequest request,
            Usuario responsavel) {

        Orcamento orcamento = buscarOrcamento(empresaId, orcamentoId);

        if (!PipelineOrcamentoService.STATUS_EM_ELABORACAO.contains(orcamento.getStatus())) {
            throw new TransicaoInvalidaException(
                    "Só é possível agendar medição para orçamento em elaboração (este está "
                            + orcamento.getStatus() + ")"
            );
        }

        negociacao.travarContato(orcamento);

        Medicao medicao = medicaoRepository.findByOrcamentoId(orcamentoId).orElse(null);

        if (medicao != null && medicao.getStatus() != StatusAgendamento.CANCELADA) {
            throw new MedicaoJaExisteException(
                    "Este orçamento já possui uma medição em andamento — use \"Propor outra data\""
            );
        }

        if (medicao == null) {

            medicao = medicaoMapper.toEntity(request, orcamento);

        } else {

            medicao.setEndereco(normalizar(request.getEndereco()));
            medicao.setObservacoes(normalizar(request.getObservacoes()));
            medicao.setDataRealizada(null);
            medicao.setContrapropostaTexto(null);
        }

        medicao.setDataAgendada(request.getDataAgendada());
        medicao.setStatus(StatusAgendamento.PROPOSTA_ENVIADA);
        medicao = medicaoRepository.saveAndFlush(medicao);

        negociacao.proporData(medicao, request.getDataAgendada(), responsavel, null);

        return montarResponse(medicao, negociacao.avisoEnvio(orcamento));
    }

    @Transactional
    public MedicaoResponse reagendar(
            Long empresaId,
            Long orcamentoId,
            MedicaoReagendarRequest request,
            Usuario responsavel) {

        Orcamento orcamento = buscarOrcamento(empresaId, orcamentoId);

        negociacao.travarContato(orcamento);

        Medicao medicao = buscarMedicao(orcamentoId);

        exigirStatusEmAny(medicao, NegociacaoAgendamentoService.STATUS_PODE_REAGENDAR_OU_CANCELAR);

        boolean estavaConfirmada = medicao.getStatus() == StatusAgendamento.AGENDADA;

        negociacao.proporData(medicao, request.getDataAgendada(), responsavel, null);

        if (estavaConfirmada) {
            pipeline.visitaDesmarcada(orcamento, responsavel, "visita remarcada, aguardando nova confirmação");
        }

        return montarResponse(medicao, negociacao.avisoEnvio(orcamento));
    }

    @Transactional
    public MedicaoResponse recusarContraproposta(
            Long empresaId,
            Long orcamentoId,
            RecusarContrapropostaRequest request,
            Usuario responsavel) {

        Orcamento orcamento = buscarOrcamento(empresaId, orcamentoId);

        negociacao.travarContato(orcamento);

        Medicao medicao = buscarMedicao(orcamentoId);

        exigirStatus(medicao, StatusAgendamento.CONTRAPROPOSTA_CLIENTE);

        negociacao.proporData(
                medicao, request.getDataAgendada(), responsavel,
                request.getMotivo() == null || request.getMotivo().isBlank()
                        ? "não temos disponibilidade nesse horário"
                        : request.getMotivo()
        );

        return montarResponse(medicao, negociacao.avisoEnvio(orcamento));
    }

    @Transactional
    public MedicaoResponse aceitarContraproposta(
            Long empresaId,
            Long orcamentoId,
            MedicaoReagendarRequest request,
            Usuario responsavel) {

        Orcamento orcamento = buscarOrcamento(empresaId, orcamentoId);

        negociacao.travarContato(orcamento);

        Medicao medicao = buscarMedicao(orcamentoId);

        negociacao.aceitarContraproposta(medicao, request.getDataAgendada(), responsavel);

        pipeline.visitaConfirmada(orcamento, responsavel);

        return montarResponse(medicao, null);
    }

    @Transactional
    public MedicaoResponse confirmarManualmente(
            Long empresaId,
            Long orcamentoId,
            Usuario responsavel) {

        Orcamento orcamento = buscarOrcamento(empresaId, orcamentoId);

        negociacao.travarContato(orcamento);

        Medicao medicao = buscarMedicao(orcamentoId);

        negociacao.confirmarManualmente(medicao, responsavel);

        pipeline.visitaConfirmada(orcamento, responsavel);

        return montarResponse(medicao, null);
    }

    @Transactional
    public MedicaoResponse realizar(
            Long empresaId,
            Long orcamentoId,
            MedicaoRealizarRequest request,
            Usuario responsavel) {

        Orcamento orcamento = buscarOrcamento(empresaId, orcamentoId);

        Medicao medicao = buscarMedicao(orcamentoId);

        exigirStatus(medicao, StatusAgendamento.AGENDADA);

        LocalDate hoje = fuso.hoje(orcamento.getEmpresa());

        if (medicao.getDataAgendada().toLocalDate().isAfter(hoje)) {
            throw new TransicaoInvalidaException(
                    "A medição está marcada para " + NegociacaoAgendamentoService.formatar(medicao.getDataAgendada())
                            + " — só pode ser marcada como realizada a partir do dia agendado"
            );
        }

        medicao.setStatus(StatusAgendamento.REALIZADA);
        negociacao.limparPedidoDeCancelamento(medicao);
        medicao.setDataRealizada(LocalDateTime.now(clock));

        if (request.getObservacoes() != null) {
            medicao.setObservacoes(normalizar(request.getObservacoes()));
        }

        Medicao salva = medicaoRepository.saveAndFlush(medicao);

        historicoService.registrarEventoOrcamento(
                orcamento, TipoEventoHistorico.MEDICAO_REALIZADA, "Medição realizada", responsavel
        );

        pipeline.medicaoRealizada(orcamento, responsavel);

        return montarResponse(salva, null);
    }

    @Transactional
    public MedicaoResponse cancelar(
            Long empresaId,
            Long orcamentoId,
            String motivo,
            Usuario responsavel) {

        Orcamento orcamento = buscarOrcamento(empresaId, orcamentoId);

        negociacao.travarContato(orcamento);

        Medicao medicao = buscarMedicao(orcamentoId);

        negociacao.cancelar(medicao, responsavel, motivo);

        pipeline.visitaDesmarcada(orcamento, responsavel, "visita de medição cancelada");

        return montarResponse(medicao, null);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void cancelarPorEncerramentoDoOrcamento(Orcamento orcamento, Usuario responsavel) {

        medicaoRepository.findByOrcamentoId(orcamento.getId())
                .filter(m -> NegociacaoAgendamentoService.STATUS_PODE_REAGENDAR_OU_CANCELAR.contains(m.getStatus()))
                .ifPresent(m -> negociacao.cancelar(m, responsavel, "o orçamento foi encerrado"));
    }

    @Transactional
    public MedicaoResponse manterData(Long empresaId, Long orcamentoId, Usuario responsavel) {

        Orcamento orcamento = buscarOrcamento(empresaId, orcamentoId);

        negociacao.travarContato(orcamento);

        Medicao medicao = buscarMedicao(orcamentoId);

        negociacao.manterData(medicao, responsavel);

        return montarResponse(medicao, null);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void registrarPedidoDeCancelamento(Medicao medicao, String texto) {
        negociacao.registrarPedidoDeCancelamento(medicao, texto);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public ResultadoConfirmacao confirmarPeloCliente(Medicao medicao, PerguntaPendente pergunta, AtendimentoWhatsapp atendimento) {

        ResultadoConfirmacao resultado = negociacao.confirmarPeloCliente(medicao, pergunta, atendimento);

        if (resultado == ResultadoConfirmacao.CONFIRMADA) {
            pipeline.visitaConfirmada(medicao.getOrcamento(), null);
        }

        return resultado;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void recusarPeloCliente(Medicao medicao, PerguntaPendente pergunta, AtendimentoWhatsapp atendimento) {
        negociacao.recusarPeloCliente(medicao, pergunta, atendimento);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void registrarSugestaoCliente(Medicao medicao, PerguntaPendente pergunta, String texto, AtendimentoWhatsapp atendimento) {
        negociacao.registrarSugestaoCliente(medicao, pergunta, texto, atendimento);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void solicitarReagendamentoPeloCliente(Medicao medicao, String texto, boolean temData, AtendimentoWhatsapp atendimento) {

        negociacao.solicitarReagendamentoPeloCliente(medicao, texto, temData, atendimento);

        pipeline.visitaDesmarcada(medicao.getOrcamento(), null, "cliente pediu para remarcar a visita");
    }

    @Scheduled(fixedDelayString = "PT10M", initialDelayString = "PT3M")
    public void expirarPropostasVencidas() {

        List<Long> ids = transactionTemplate.execute(status ->
                medicaoRepository
                        .findTop200ByStatusAndDataAgendadaBeforeOrderByDataAgendadaAsc(
                                StatusAgendamento.PROPOSTA_ENVIADA, LocalDateTime.now(clock).plusHours(FOLGA_FUSOS_HORAS))
                        .stream().map(Medicao::getId).toList()
        );

        if (ids == null) {
            return;
        }

        for (Long id : ids) {

            try {

                transactionTemplate.executeWithoutResult(status -> medicaoRepository.findById(id).ifPresent(m -> {
                    if (m.getDataAgendada().isBefore(negociacao.agoraNaEmpresa(m))) {
                        negociacao.travarContato(m.getOrcamento());
                        negociacao.expirarProposta(m, null, false);
                    }
                }));

            } catch (RuntimeException ex) {
                log.warn("Não foi possível expirar a proposta da medição {}", id, ex);
            }
        }
    }

    MedicaoResponse montarResponse(Medicao medicao, String aviso) {

        MedicaoResponse response = medicaoMapper.toResponse(medicao);
        MensagemSaida envio = negociacao.envioDaUltimaProposta(medicao).orElse(null);
        ProximaAcao proxima = negociacao.proximaAcao(medicao, envio);

        response.setVersaoProposta(medicao.getVersaoProposta());
        response.setEnvioStatus(AgendamentoMapperSupport.statusEnvio(envio, medicao.getOrcamento().getCliente()));
        response.setEnvioErro(envio != null ? envio.getUltimoErro() : null);
        response.setResponsavelProximaAcao(proxima.responsavel());
        response.setProximaAcao(proxima.descricao());
        response.setAviso(aviso);
        response.setCancelamentoSolicitadoEm(medicao.getCancelamentoSolicitadoEm());
        response.setCancelamentoSolicitadoTexto(medicao.getCancelamentoSolicitadoTexto());

        negociacao.sugestaoPendente(medicao).ifPresent(sugestao -> {
            response.setContrapropostaData(sugestao.getDataProposta());
            response.setContrapropostaEm(sugestao.getCriadoEm());
        });

        return response;
    }

    private void exigirStatus(Medicao medicao, StatusAgendamento esperado) {

        if (medicao.getStatus() != esperado) {
            throw new TransicaoInvalidaException(
                    "Esta ação exige que a medição esteja com status " + esperado + ", mas está " + medicao.getStatus()
            );
        }
    }

    private void exigirStatusEmAny(Medicao medicao, Set<StatusAgendamento> permitidos) {

        if (!permitidos.contains(medicao.getStatus())) {
            throw new TransicaoInvalidaException(
                    "Esta ação não é permitida com a medição no status " + medicao.getStatus()
            );
        }
    }

    private String normalizar(String valor) {
        return valor == null || valor.isBlank() ? null : valor.trim();
    }

    private Orcamento buscarOrcamento(Long empresaId, Long orcamentoId) {

        return orcamentoRepository
                .findByIdAndEmpresaId(orcamentoId, empresaId)
                .orElseThrow(() -> new OrcamentoNaoEncontradoException("Orçamento não encontrado"));
    }

    private Medicao buscarMedicao(Long orcamentoId) {

        return medicaoRepository
                .findByOrcamentoId(orcamentoId)
                .orElseThrow(() -> new MedicaoNaoEncontradaException("Medição não encontrada para este orçamento"));
    }
}
