package br.com.vidratx.service;

import br.com.vidratx.config.FusoEmpresa;
import br.com.vidratx.dto.AlertaCalculoResponse;
import br.com.vidratx.dto.MensagemAtendimentoResponse;
import br.com.vidratx.dto.OrcamentoRequest;
import br.com.vidratx.dto.OrcamentoResponse;
import br.com.vidratx.dto.OrcamentoTotaisResponse;
import br.com.vidratx.entity.AtendimentoWhatsapp;
import br.com.vidratx.entity.Cliente;
import br.com.vidratx.entity.Empresa;
import br.com.vidratx.entity.Medicao;
import br.com.vidratx.entity.MensagemSaida;
import br.com.vidratx.entity.Orcamento;
import br.com.vidratx.entity.OrcamentoItem;
import br.com.vidratx.entity.OrdemServico;
import br.com.vidratx.entity.ParametroCalculo;
import br.com.vidratx.entity.PerguntaPendente;
import br.com.vidratx.entity.SolicitacaoOrcamento;
import br.com.vidratx.entity.Usuario;
import br.com.vidratx.enums.CategoriaMensagemSaida;
import br.com.vidratx.enums.DecisaoMedicaoPendente;
import br.com.vidratx.enums.MotivoPerda;
import br.com.vidratx.enums.RemetenteMensagem;
import br.com.vidratx.enums.StatusAgendamento;
import br.com.vidratx.enums.EtapaFluxo;
import br.com.vidratx.enums.StatusAtendimento;
import br.com.vidratx.enums.StatusMensagemSaida;
import br.com.vidratx.enums.StatusOrcamento;
import br.com.vidratx.enums.StatusPergunta;
import br.com.vidratx.enums.StatusSolicitacaoOrcamento;
import br.com.vidratx.enums.TipoEventoHistorico;
import br.com.vidratx.enums.TipoMensagem;
import br.com.vidratx.enums.TipoPergunta;
import br.com.vidratx.exception.ClienteNaoEncontradoException;
import br.com.vidratx.exception.EmpresaNaoEncontradaException;
import br.com.vidratx.exception.MedicaoPendenteNoEnvioException;
import br.com.vidratx.exception.OrcamentoNaoEditavelException;
import br.com.vidratx.exception.OrcamentoNaoEncontradoException;
import br.com.vidratx.exception.PrecoAlteradoException;
import br.com.vidratx.exception.SolicitacaoOrcamentoNaoEncontradaException;
import br.com.vidratx.exception.TransicaoInvalidaException;
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
import br.com.vidratx.service.NegociacaoAgendamentoService.ProximaAcao;
import br.com.vidratx.service.WhatsappSaidaService.NovaMensagem;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.text.NumberFormat;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class OrcamentoService {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy", PT_BR);

    public static final Set<StatusOrcamento> STATUS_EDITAVEL = PipelineOrcamentoService.STATUS_EM_ELABORACAO;

    private static final Set<StatusAgendamento> MEDICAO_EM_ABERTO = EnumSet.of(
            StatusAgendamento.PROPOSTA_ENVIADA, StatusAgendamento.CONTRAPROPOSTA_CLIENTE,
            StatusAgendamento.RECUSADA_CLIENTE, StatusAgendamento.REAGENDAMENTO_NECESSARIO,
            StatusAgendamento.AGENDADA
    );

    private final OrcamentoRepository orcamentoRepository;
    private final EmpresaRepository empresaRepository;
    private final ClienteRepository clienteRepository;
    private final SolicitacaoOrcamentoRepository solicitacaoOrcamentoRepository;
    private final AtendimentoWhatsappRepository atendimentoWhatsappRepository;
    private final MensagemAtendimentoRepository mensagemAtendimentoRepository;
    private final OrcamentoItemRepository orcamentoItemRepository;
    private final MedicaoRepository medicaoRepository;
    private final OrdemServicoRepository ordemServicoRepository;
    private final OrcamentoMapper orcamentoMapper;
    private final AtendimentoWhatsappMapper atendimentoWhatsappMapper;
    private final HistoricoService historicoService;
    private final OrcamentoCalculoService orcamentoCalculoService;
    private final ParametroCalculoService parametroCalculoService;
    private final PerguntaPendenteService perguntaPendenteService;
    private final WhatsappSaidaService whatsappSaidaService;
    private final WhatsappContatoService whatsappContatoService;
    private final ConversaWhatsappService conversaWhatsappService;
    private final NegociacaoAgendamentoService negociacao;
    private final MedicaoService medicaoService;
    private final OrdemServicoService ordemServicoService;
    private final PipelineOrcamentoService pipeline;
    private final Clock clock;
    private final FusoEmpresa fuso;

    public OrcamentoService(
            OrcamentoRepository orcamentoRepository,
            EmpresaRepository empresaRepository,
            ClienteRepository clienteRepository,
            SolicitacaoOrcamentoRepository solicitacaoOrcamentoRepository,
            AtendimentoWhatsappRepository atendimentoWhatsappRepository,
            MensagemAtendimentoRepository mensagemAtendimentoRepository,
            OrcamentoItemRepository orcamentoItemRepository,
            MedicaoRepository medicaoRepository,
            OrdemServicoRepository ordemServicoRepository,
            OrcamentoMapper orcamentoMapper,
            AtendimentoWhatsappMapper atendimentoWhatsappMapper,
            HistoricoService historicoService,
            OrcamentoCalculoService orcamentoCalculoService,
            ParametroCalculoService parametroCalculoService,
            PerguntaPendenteService perguntaPendenteService,
            WhatsappSaidaService whatsappSaidaService,
            WhatsappContatoService whatsappContatoService,
            ConversaWhatsappService conversaWhatsappService,
            NegociacaoAgendamentoService negociacao,
            MedicaoService medicaoService,
            OrdemServicoService ordemServicoService,
            PipelineOrcamentoService pipeline,
            Clock clock) {

        this.orcamentoRepository = orcamentoRepository;
        this.empresaRepository = empresaRepository;
        this.clienteRepository = clienteRepository;
        this.solicitacaoOrcamentoRepository = solicitacaoOrcamentoRepository;
        this.atendimentoWhatsappRepository = atendimentoWhatsappRepository;
        this.mensagemAtendimentoRepository = mensagemAtendimentoRepository;
        this.orcamentoItemRepository = orcamentoItemRepository;
        this.medicaoRepository = medicaoRepository;
        this.ordemServicoRepository = ordemServicoRepository;
        this.orcamentoMapper = orcamentoMapper;
        this.atendimentoWhatsappMapper = atendimentoWhatsappMapper;
        this.historicoService = historicoService;
        this.orcamentoCalculoService = orcamentoCalculoService;
        this.parametroCalculoService = parametroCalculoService;
        this.perguntaPendenteService = perguntaPendenteService;
        this.whatsappSaidaService = whatsappSaidaService;
        this.whatsappContatoService = whatsappContatoService;
        this.conversaWhatsappService = conversaWhatsappService;
        this.negociacao = negociacao;
        this.medicaoService = medicaoService;
        this.ordemServicoService = ordemServicoService;
        this.pipeline = pipeline;
        this.clock = clock;
        this.fuso = new FusoEmpresa(clock);
    }

    public enum ResultadoRespostaCliente {
        APROVADO,
        RECUSADO,
        ALTERACAO_REGISTRADA,
        DESATUALIZADO,
        VENCIDO
    }

    @Transactional(readOnly = true)
    public List<OrcamentoResponse> listarPorEmpresa(
            Long empresaId,
            StatusOrcamento status,
            Long clienteId) {

        verificarEmpresa(empresaId);

        List<Orcamento> orcamentos;

        if (status != null && clienteId != null) {
            orcamentos = orcamentoRepository
                    .findAllByEmpresaIdAndStatusAndClienteIdOrderByCriadoEmDesc(empresaId, status, clienteId);
        } else if (status != null) {
            orcamentos = orcamentoRepository.findAllByEmpresaIdAndStatusOrderByCriadoEmDesc(empresaId, status);
        } else if (clienteId != null) {
            orcamentos = orcamentoRepository.findAllByEmpresaIdAndClienteIdOrderByCriadoEmDesc(empresaId, clienteId);
        } else {
            orcamentos = orcamentoRepository.findAllByEmpresaIdOrderByCriadoEmDesc(empresaId);
        }

        List<Long> ids = orcamentos.stream().map(Orcamento::getId).toList();

        Map<Long, Medicao> medicoes = ids.isEmpty() ? Map.of() : medicaoRepository.findAllByOrcamentoIdIn(ids)
                .stream().collect(Collectors.toMap(m -> m.getOrcamento().getId(), Function.identity(), (a, b) -> a));

        Map<Long, OrdemServico> ordens = ids.isEmpty() ? Map.of() : ordemServicoRepository.findAllByOrcamentoIdIn(ids)
                .stream().collect(Collectors.toMap(o -> o.getOrcamento().getId(), Function.identity(), (a, b) -> a));

        return orcamentos.stream()
                .map(o -> montarResponse(o, medicoes.get(o.getId()), ordens.get(o.getId()), null, false))
                .toList();
    }

    @Transactional(readOnly = true)
    public OrcamentoResponse buscarPorId(Long empresaId, Long orcamentoId) {
        return montarResponseCompleto(buscarOrcamento(empresaId, orcamentoId), null);
    }

    @Transactional
    public OrcamentoResponse criar(Long empresaId, OrcamentoRequest request, Usuario responsavel) {

        Empresa empresa = buscarEmpresa(empresaId);
        Cliente cliente = buscarCliente(empresaId, request.getClienteId());

        Orcamento salvo = orcamentoRepository.save(orcamentoMapper.toEntity(request, empresa, cliente));

        historicoService.registrarEventoOrcamento(
                salvo, TipoEventoHistorico.ORCAMENTO_CRIADO, "Orçamento criado", responsavel
        );

        return montarResponseCompleto(salvo, null);
    }

    @Transactional
    public OrcamentoResponse criarAPartirDeSolicitacao(Long empresaId, Long solicitacaoId, Usuario responsavel) {

        SolicitacaoOrcamento solicitacao = solicitacaoOrcamentoRepository
                .findByIdAndEmpresaId(solicitacaoId, empresaId)
                .orElseThrow(() -> new SolicitacaoOrcamentoNaoEncontradaException(
                        "Solicitação de orçamento não encontrada"
                ));

        Optional<Orcamento> existente = orcamentoRepository.findBySolicitacaoOrcamentoId(solicitacaoId);

        if (existente.isPresent()) {
            return montarResponseCompleto(existente.get(), null);
        }

        if (solicitacao.getStatus() == StatusSolicitacaoOrcamento.CANCELADA) {
            throw new TransicaoInvalidaException("Esta solicitação foi descartada");
        }

        Orcamento orcamento = new Orcamento();

        orcamento.setEmpresa(buscarEmpresa(empresaId));
        orcamento.setCliente(solicitacao.getCliente());
        orcamento.setSolicitacaoOrcamento(solicitacao);

        Orcamento salvo = orcamentoRepository.save(orcamento);

        solicitacao.setStatus(StatusSolicitacaoOrcamento.CONVERTIDA);
        solicitacaoOrcamentoRepository.save(solicitacao);

        historicoService.registrarEventoOrcamento(
                salvo, TipoEventoHistorico.ORCAMENTO_CRIADO,
                "Orçamento criado a partir de solicitação recebida pelo WhatsApp", responsavel
        );

        return montarResponseCompleto(salvo, null);
    }

    @Transactional(readOnly = true)
    public List<MensagemAtendimentoResponse> listarFotosWhatsapp(Long empresaId, Long orcamentoId) {

        Orcamento orcamento = buscarOrcamento(empresaId, orcamentoId);

        if (orcamento.getSolicitacaoOrcamento() == null) {
            return List.of();
        }

        return atendimentoWhatsappRepository
                .findBySolicitacaoOrcamentoId(orcamento.getSolicitacaoOrcamento().getId())
                .map(atendimento -> mensagemAtendimentoRepository
                        .findAllByAtendimentoIdAndTipoOrderByEnviadoEmAsc(atendimento.getId(), TipoMensagem.IMAGEM)
                        .stream()
                        .map(atendimentoWhatsappMapper::toMensagemResponse)
                        .toList())
                .orElse(List.of());
    }

    @Transactional
    public OrcamentoResponse atualizar(Long empresaId, Long orcamentoId, OrcamentoRequest request, Usuario responsavel) {

        Orcamento orcamento = buscarOrcamento(empresaId, orcamentoId);

        garantirEditavel(orcamento);

        Cliente cliente = buscarCliente(empresaId, request.getClienteId());

        orcamentoMapper.updateEntity(orcamento, request, cliente);

        Orcamento salvo = orcamentoRepository.save(orcamento);

        historicoService.registrarEventoOrcamento(
                salvo, TipoEventoHistorico.ORCAMENTO_ALTERADO, "Orçamento atualizado", responsavel
        );

        return montarResponseCompleto(salvo, null);
    }

    @Transactional
    public void excluir(Long empresaId, Long orcamentoId) {

        Orcamento orcamento = buscarOrcamento(empresaId, orcamentoId);

        garantirEditavel(orcamento);

        medicaoRepository.findByOrcamentoId(orcamento.getId()).ifPresent(medicao -> {

            if (MEDICAO_EM_ABERTO.contains(medicao.getStatus())) {
                throw new TransicaoInvalidaException(
                        "Este orçamento tem uma medição em andamento. Cancele a medição antes de excluir."
                );
            }
        });

        perguntaPendenteService.encerrarDoOrcamento(orcamento.getId(), StatusPergunta.CANCELADA);

        orcamentoRepository.delete(orcamento);
    }

    @Transactional
    public OrcamentoResponse enviar(Long empresaId, Long orcamentoId, BigDecimal valorEsperado, Usuario responsavel) {
        return enviar(empresaId, orcamentoId, valorEsperado, null, responsavel);
    }

    @Transactional
    public OrcamentoResponse enviar(
            Long empresaId, Long orcamentoId, BigDecimal valorEsperado,
            DecisaoMedicaoPendente medicaoPendente, Usuario responsavel) {

        Orcamento orcamento = buscarOrcamento(empresaId, orcamentoId);

        String telefone = telefoneObrigatorio(orcamento.getCliente());

        whatsappContatoService.travar(orcamento.getEmpresa(), telefone);

        exigirStatus(orcamento, StatusOrcamento.ORCAMENTO_FINAL);

        OrcamentoTotaisResponse totais = orcamentoCalculoService.prepararEnvio(orcamento);

        if (!totais.getBloqueiosEnvio().isEmpty()) {
            throw new TransicaoInvalidaException(
                    "O orçamento ainda não pode ser enviado: " + String.join(" ", totais.getBloqueiosEnvio())
            );
        }

        if (valorEsperado != null && valorEsperado.setScale(2, RoundingMode.HALF_UP)
                .compareTo(totais.getValorFinal().setScale(2, RoundingMode.HALF_UP)) != 0) {

            throw new PrecoAlteradoException(
                    "O valor do orçamento mudou de " + formatarValor(valorEsperado) + " para "
                            + formatarValor(totais.getValorFinal())
                            + " desde que a tela foi aberta (itens ou parâmetros de cálculo alterados). "
                            + "Confira o novo valor e envie de novo.",
                    valorEsperado, totais.getValorFinal()
            );
        }

        Medicao medicaoEmAberto = medicaoRepository.findByOrcamentoId(orcamento.getId())
                .filter(m -> MEDICAO_EM_ABERTO.contains(m.getStatus()))
                .orElse(null);

        if (medicaoEmAberto != null && medicaoPendente == null) {

            String data = NegociacaoAgendamentoService.formatar(medicaoEmAberto.getDataAgendada());

            throw new MedicaoPendenteNoEnvioException(
                    (medicaoEmAberto.getStatus() == StatusAgendamento.AGENDADA
                            ? "A visita de medição está confirmada para " + data
                            : "A visita de medição ainda está sendo combinada com o cliente (" + data + ")")
                            + ". Ao enviar o orçamento final, cancele a visita (o cliente é avisado) "
                            + "ou mantenha-a para conferir as medidas.",
                    medicaoEmAberto.getStatus().name(), data
            );
        }

        LocalDate hoje = fuso.hoje(orcamento.getEmpresa());

        if (orcamento.getValidoAte() == null) {

            ParametroCalculo parametro = parametroCalculoService.buscarOuCriarPadrao(empresaId);
            int dias = parametro.getValidadePadraoDias() != null && parametro.getValidadePadraoDias() > 0
                    ? parametro.getValidadePadraoDias() : 7;

            orcamento.setValidoAte(hoje.plusDays(dias));

        } else if (orcamento.getValidoAte().isBefore(hoje)) {

            throw new TransicaoInvalidaException(
                    "A validade do orçamento (" + orcamento.getValidoAte().format(FORMATO_DATA)
                            + ") já passou. Ajuste a data de validade antes de enviar."
            );
        }

        if (medicaoEmAberto != null && medicaoPendente == DecisaoMedicaoPendente.CANCELAR) {

            negociacao.cancelar(medicaoEmAberto, responsavel, "o orçamento foi preparado com as medidas já informadas");

        } else if (medicaoEmAberto != null) {

            historicoService.registrarEventoOrcamento(orcamento, TipoEventoHistorico.ORCAMENTO_ALTERADO,
                    "Orçamento final enviado mantendo a visita de medição de "
                            + NegociacaoAgendamentoService.formatar(medicaoEmAberto.getDataAgendada())
                            + " (conferência de medidas)", responsavel);
        }

        int revisao = (orcamento.getRevisaoEnvio() == null ? 0 : orcamento.getRevisaoEnvio()) + 1;

        orcamento.setRevisaoEnvio(revisao);
        orcamento.setStatus(StatusOrcamento.ENVIADO);
        orcamento.setEnviadoEm(LocalDateTime.now(clock));
        orcamento.setRespondidoEm(null);
        orcamento.setLembreteEnviadoEm(null);
        orcamento.setAlteracaoSolicitadaEm(null);
        orcamento.setAlteracaoSolicitadaTexto(null);

        Orcamento salvo = orcamentoRepository.saveAndFlush(orcamento);

        String texto = montarMensagemOrcamento(salvo, totais, revisao > 1);

        PerguntaPendente pergunta = perguntaPendenteService.abrir(
                salvo.getEmpresa(), telefone, salvo.getCliente(), TipoPergunta.APROVAR_ORCAMENTO,
                salvo.getId(), revisao,
                "Aprovar o orçamento nº " + salvo.getId() + " (" + formatarValor(totais.getValorFinal()) + ")",
                texto,
                salvo.getValidoAte().atTime(LocalTime.MAX)
        );

        AtendimentoWhatsapp atendimento = atendimentoDaSolicitacao(salvo);

        whatsappSaidaService.enfileirar(new NovaMensagem(
                salvo.getEmpresa(), telefone, salvo.getCliente(), texto, CategoriaMensagemSaida.ORCAMENTO,
                RemetenteMensagem.ATENDENTE, "ORCAMENTO", salvo.getId(), pergunta, atendimento
        ));

        devolverAoBotSeOPedidoFoiRespondido(atendimento, salvo, responsavel);

        historicoService.registrarEventoOrcamento(
                salvo, TipoEventoHistorico.ORCAMENTO_ENVIADO,
                "Orçamento enviado ao cliente" + (revisao > 1 ? " (revisão " + revisao + ")" : "") + ": "
                        + formatarValor(totais.getValorFinal()) + ", válido até "
                        + salvo.getValidoAte().format(FORMATO_DATA),
                responsavel
        );

        return montarResponseCompleto(salvo, negociacao.avisoEnvio(salvo));
    }

    @Transactional
    public OrcamentoResponse enviarEstimativa(Long empresaId, Long orcamentoId, Usuario responsavel) {

        Orcamento orcamento = buscarOrcamento(empresaId, orcamentoId);

        String telefone = telefoneObrigatorio(orcamento.getCliente());

        whatsappContatoService.travar(orcamento.getEmpresa(), telefone);

        if (!EnumSet.of(StatusOrcamento.NOVO_CONTATO, StatusOrcamento.PRE_ORCAMENTO, StatusOrcamento.VISITA_AGENDADA)
                .contains(orcamento.getStatus())) {

            throw new TransicaoInvalidaException(
                    "A estimativa é enviada antes da medição (Novo contato, Pré-orçamento ou Visita agendada)"
            );
        }

        OrcamentoTotaisResponse totais = orcamentoCalculoService.prepararEnvio(orcamento);

        if (orcamentoItemRepository.countByOrcamentoId(orcamento.getId()) == 0
                || totais.getValorFinal().compareTo(BigDecimal.ZERO) <= 0) {

            throw new TransicaoInvalidaException(
                    "Adicione os itens com medidas aproximadas antes de enviar uma estimativa"
            );
        }

        List<String> pendencias = totais.getAlertas().stream()
                .filter(a -> OrcamentoCalculoService.ALERTAS_PENDENCIA_ITEM.contains(a.getCodigo()))
                .map(AlertaCalculoResponse::getMensagem)
                .toList();

        if (!pendencias.isEmpty()) {
            throw new TransicaoInvalidaException(
                    "A estimativa ainda não pode ser enviada: " + String.join(" ", pendencias)
            );
        }

        ParametroCalculo parametro = parametroCalculoService.buscarOuCriarPadrao(empresaId);

        BigDecimal minimo = aplicarVariacao(totais.getValorFinal(), parametro.getVariacaoPreOrcamentoMinPct());
        BigDecimal maximo = aplicarVariacao(totais.getValorFinal(), parametro.getVariacaoPreOrcamentoMaxPct());

        if (minimo.compareTo(maximo) > 0) {
            BigDecimal troca = minimo;
            minimo = maximo;
            maximo = troca;
        }

        String texto = "Olá, " + primeiroNome(orcamento.getCliente().getNome()) + "! Pela descrição que recebemos, "
                + "o seu serviço deve ficar entre " + formatarValor(minimo) + " e " + formatarValor(maximo)
                + ". É uma estimativa sem compromisso: o valor exato sai depois da medição no local."
                + "\n\nSe quiser seguir, responda esta mensagem que combinamos a visita.";

        whatsappSaidaService.enfileirar(new NovaMensagem(
                orcamento.getEmpresa(), telefone, orcamento.getCliente(), texto, CategoriaMensagemSaida.ESTIMATIVA,
                RemetenteMensagem.ATENDENTE, "ORCAMENTO", orcamento.getId(), null, atendimentoDaSolicitacao(orcamento)
        ));

        orcamento.setEstimativaEnviadaEm(LocalDateTime.now(clock));
        orcamentoRepository.save(orcamento);

        pipeline.moverSeEstiverEm(
                orcamento, EnumSet.of(StatusOrcamento.NOVO_CONTATO), StatusOrcamento.PRE_ORCAMENTO,
                "estimativa enviada ao cliente", responsavel
        );

        historicoService.registrarEventoOrcamento(
                orcamento, TipoEventoHistorico.ORCAMENTO_ESTIMATIVA_ENVIADA,
                "Estimativa enviada ao cliente: entre " + formatarValor(minimo) + " e " + formatarValor(maximo),
                responsavel
        );

        return montarResponseCompleto(orcamento, negociacao.avisoEnvio(orcamento));
    }

    @Transactional
    public OrcamentoResponse aprovar(Long empresaId, Long orcamentoId, Usuario responsavel) {

        Orcamento orcamento = buscarOrcamento(empresaId, orcamentoId);

        travarContato(orcamento);
        exigirStatus(orcamento, StatusOrcamento.ENVIADO);

        boolean vencido = orcamento.getValidoAte() != null
                && orcamento.getValidoAte().isBefore(fuso.hoje(orcamento.getEmpresa()));

        aprovarInterno(orcamento, responsavel,
                "Orçamento aprovado manualmente no painel por "
                        + (responsavel != null ? responsavel.getNome() : "um usuário")
                        + (vencido ? " (depois da validade de " + orcamento.getValidoAte().format(FORMATO_DATA) + ")" : ""),
                StatusPergunta.CANCELADA, null);

        return montarResponseCompleto(orcamento, null);
    }

    @Transactional
    public OrcamentoResponse perder(
            Long empresaId, Long orcamentoId, MotivoPerda motivo, String motivoOutro, Usuario responsavel) {

        Orcamento orcamento = buscarOrcamento(empresaId, orcamentoId);

        travarContato(orcamento);
        exigirStatus(orcamento, StatusOrcamento.ENVIADO);

        if (motivo == null) {
            throw new TransicaoInvalidaException("Informe o motivo da perda");
        }

        perderInterno(orcamento, motivo, motivoOutro, responsavel,
                "Orçamento marcado como perdido manualmente no painel");

        return montarResponseCompleto(orcamento, null);
    }

    @Transactional
    public OrcamentoResponse expirar(Long empresaId, Long orcamentoId, Usuario responsavel) {

        Orcamento orcamento = buscarOrcamento(empresaId, orcamentoId);

        travarContato(orcamento);
        exigirStatus(orcamento, StatusOrcamento.ENVIADO);

        expirarInterno(orcamento, responsavel, "Orçamento expirado manualmente", true, null);

        return montarResponseCompleto(orcamento, null);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void expirarAutomaticamente(Orcamento orcamento) {

        travarContato(orcamento);

        if (orcamento.getStatus() != StatusOrcamento.ENVIADO) {
            return;
        }

        expirarInterno(orcamento, null,
                "Orçamento expirado automaticamente"
                        + (orcamento.getValidoAte() != null
                        ? ": validade até " + orcamento.getValidoAte().format(FORMATO_DATA) : ""),
                true, null);
    }

    @Transactional
    public OrcamentoResponse reabrir(Long empresaId, Long orcamentoId, String motivo, Usuario responsavel) {

        Orcamento orcamento = buscarOrcamento(empresaId, orcamentoId);

        travarContato(orcamento);
        exigirStatus(orcamento, StatusOrcamento.PERDIDO);

        StatusOrcamento destino = destinoDaReabertura(orcamento);
        String motivoPerda = orcamento.getMotivoPerda() != null ? orcamento.getMotivoPerda().name() : "sem motivo";

        if (destino == StatusOrcamento.ORCAMENTO_FINAL && orcamento.getValidoAte() != null
                && orcamento.getValidoAte().isBefore(fuso.hoje(orcamento.getEmpresa()))) {

            orcamento.setValidoAte(null);
        }

        orcamento.setStatus(destino);
        orcamento.setStatusAntesPerda(null);
        orcamento.setMotivoPerda(null);
        orcamento.setMotivoPerdaOutro(null);
        orcamento.setRespondidoEm(null);
        orcamentoRepository.saveAndFlush(orcamento);

        orcamentoCalculoService.recalcular(orcamento);

        historicoService.registrarEventoOrcamento(orcamento, TipoEventoHistorico.ORCAMENTO_REABERTO,
                "Orçamento reaberto por " + (responsavel != null ? responsavel.getNome() : "um usuário")
                        + " (estava perdido: " + motivoPerda + "); voltou para " + destino
                        + (motivo != null && !motivo.isBlank() ? " — " + motivo.trim() : ""),
                responsavel);

        return montarResponseCompleto(orcamento, null);
    }

    private StatusOrcamento destinoDaReabertura(Orcamento orcamento) {

        StatusOrcamento anterior = orcamento.getStatusAntesPerda();

        if (anterior == null) {
            return orcamento.getEnviadoEm() != null ? StatusOrcamento.ORCAMENTO_FINAL : StatusOrcamento.NOVO_CONTATO;
        }

        return switch (anterior) {
            case ENVIADO, EXPIRADO, ORCAMENTO_FINAL -> StatusOrcamento.ORCAMENTO_FINAL;
            case VISITA_AGENDADA -> StatusOrcamento.PRE_ORCAMENTO;
            case NOVO_CONTATO, PRE_ORCAMENTO, MEDIDO -> anterior;
            case APROVADO, PERDIDO -> StatusOrcamento.ORCAMENTO_FINAL;
        };
    }

    @Transactional
    public OrcamentoResponse revisar(Long empresaId, Long orcamentoId, Usuario responsavel) {

        Orcamento orcamento = buscarOrcamento(empresaId, orcamentoId);

        travarContato(orcamento);

        if (orcamento.getStatus() != StatusOrcamento.ENVIADO && orcamento.getStatus() != StatusOrcamento.EXPIRADO) {
            throw new TransicaoInvalidaException(
                    "Só é possível revisar um orçamento enviado ou expirado (está " + orcamento.getStatus() + ")"
            );
        }

        StatusOrcamento anterior = orcamento.getStatus();

        perguntaPendenteService.encerrarDoOrcamento(orcamento.getId(), StatusPergunta.SUBSTITUIDA);

        orcamento.setStatus(StatusOrcamento.ORCAMENTO_FINAL);
        orcamento.setRespondidoEm(null);
        orcamentoRepository.saveAndFlush(orcamento);

        orcamentoCalculoService.recalcular(orcamento);

        historicoService.registrarEventoOrcamento(
                orcamento, TipoEventoHistorico.ORCAMENTO_REVISAO,
                "Orçamento reaberto para revisão (estava " + anterior + ")"
                        + (orcamento.getAlteracaoSolicitadaTexto() != null
                        ? " — pedido do cliente: \"" + orcamento.getAlteracaoSolicitadaTexto() + "\"" : ""),
                responsavel
        );

        return montarResponseCompleto(orcamento, null);
    }

    @Transactional
    public OrcamentoResponse moverPipeline(
            Long empresaId,
            Long orcamentoId,
            StatusOrcamento novoStatus,
            MotivoPerda motivoPerda,
            String motivoPerdaOutro,
            Usuario responsavel) {

        Orcamento orcamento = buscarOrcamento(empresaId, orcamentoId);

        travarContato(orcamento);

        if (!STATUS_EDITAVEL.contains(orcamento.getStatus())) {
            throw new TransicaoInvalidaException(
                    "Este orçamento já foi enviado — use aprovar, marcar como perdido, expirar ou revisar"
            );
        }

        if (novoStatus == StatusOrcamento.ENVIADO) {
            throw new TransicaoInvalidaException(
                    "Use a ação \"Enviar ao cliente\" para mover um orçamento para Enviado"
            );
        }

        if (novoStatus != StatusOrcamento.PERDIDO && !STATUS_EDITAVEL.contains(novoStatus)) {
            throw new TransicaoInvalidaException("Transição de status inválida");
        }

        Optional<Medicao> medicao = medicaoRepository.findByOrcamentoId(orcamento.getId());

        if (novoStatus == StatusOrcamento.VISITA_AGENDADA
                && medicao.map(m -> m.getStatus() != StatusAgendamento.AGENDADA).orElse(true)) {

            throw new TransicaoInvalidaException(
                    "\"Visita agendada\" exige uma medição confirmada. Agende a medição: o card muda sozinho "
                            + "quando o cliente confirmar."
            );
        }

        if (novoStatus == StatusOrcamento.MEDIDO
                && medicao.map(m -> m.getStatus() != StatusAgendamento.REALIZADA).orElse(true)) {

            throw new TransicaoInvalidaException(
                    "\"Medido\" exige a medição marcada como realizada."
            );
        }

        if (orcamento.getStatus() == StatusOrcamento.VISITA_AGENDADA
                && novoStatus != StatusOrcamento.PERDIDO
                && medicao.map(m -> m.getStatus() == StatusAgendamento.AGENDADA).orElse(false)) {

            throw new TransicaoInvalidaException(
                    "A visita de medição está confirmada. Cancele ou remarque a medição antes de mover o card."
            );
        }

        if (novoStatus == StatusOrcamento.PERDIDO) {

            if (motivoPerda == null) {
                throw new TransicaoInvalidaException("Informe o motivo ao mover um orçamento para Perdido");
            }

            perderInterno(orcamento, motivoPerda, motivoPerdaOutro, responsavel,
                    "Orçamento movido de " + orcamento.getStatus() + " para PERDIDO");

            return montarResponseCompleto(orcamento, null);
        }

        StatusOrcamento statusAnterior = orcamento.getStatus();

        orcamento.setStatus(novoStatus);

        Orcamento salvo = orcamentoRepository.save(orcamento);

        historicoService.registrarEventoOrcamento(
                salvo, TipoEventoHistorico.ORCAMENTO_MOVIDO_PIPELINE,
                "Orçamento movido de " + statusAnterior + " para " + novoStatus, responsavel
        );

        return montarResponseCompleto(salvo, null);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public ResultadoRespostaCliente aprovarPeloCliente(
            Orcamento orcamento, PerguntaPendente pergunta, AtendimentoWhatsapp atendimento) {

        Optional<ResultadoRespostaCliente> invalida = validarRespostaCliente(orcamento, pergunta, atendimento);

        if (invalida.isPresent()) {
            return invalida.get();
        }

        aprovarInterno(orcamento, null,
                "Orçamento aprovado pelo cliente via WhatsApp (revisão " + pergunta.getVersao() + ", "
                        + formatarValor(orcamento.getValorTotal()) + ")",
                StatusPergunta.RESPONDIDA, atendimento);

        return ResultadoRespostaCliente.APROVADO;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public ResultadoRespostaCliente recusarPeloCliente(
            Orcamento orcamento, PerguntaPendente pergunta, String texto, AtendimentoWhatsapp atendimento) {

        Optional<ResultadoRespostaCliente> invalida = validarRespostaCliente(orcamento, pergunta, atendimento);

        if (invalida.isPresent()) {
            return invalida.get();
        }

        perguntaPendenteService.encerrar(pergunta, StatusPergunta.RESPONDIDA);

        perderInterno(orcamento, MotivoPerda.RECUSADO_VIA_WHATSAPP, null, null,
                "Cliente recusou o orçamento pelo WhatsApp: \"" + texto + "\"");

        enviarAoCliente(orcamento, atendimento,
                "Tudo bem, obrigado pelo retorno! Registramos que você não vai seguir com o orçamento nº "
                        + orcamento.getId() + ". Vou avisar um atendente para conversar com você sobre outras opções.",
                CategoriaMensagemSaida.ESCALONAMENTO);

        conversaWhatsappService.escalar(atendimento, "Cliente recusou o orçamento nº " + orcamento.getId()
                + " pelo WhatsApp — conversar sobre outras opções");

        return ResultadoRespostaCliente.RECUSADO;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public ResultadoRespostaCliente registrarAlteracaoSolicitada(
            Orcamento orcamento, PerguntaPendente pergunta, String texto, AtendimentoWhatsapp atendimento) {

        Optional<ResultadoRespostaCliente> invalida = validarRespostaCliente(orcamento, pergunta, atendimento);

        if (invalida.isPresent()) {
            return invalida.get();
        }

        perguntaPendenteService.encerrar(pergunta, StatusPergunta.RESPONDIDA);

        boolean temDetalhe = texto != null && !texto.isBlank() && !texto.trim().matches("\\D*2\\D*");

        orcamento.setAlteracaoSolicitadaEm(LocalDateTime.now(clock));
        orcamento.setAlteracaoSolicitadaTexto(temDetalhe ? limitar(texto.trim(), 1000) : null);
        orcamentoRepository.saveAndFlush(orcamento);

        historicoService.registrarEventoOrcamento(
                orcamento, TipoEventoHistorico.ORCAMENTO_ALTERACAO_SOLICITADA,
                "Cliente pediu alteração no orçamento pelo WhatsApp" + (temDetalhe ? ": \"" + texto.trim() + "\"" : ""),
                null
        );

        enviarAoCliente(orcamento, atendimento,
                temDetalhe
                        ? "Certo! Anotamos o seu pedido: \"" + texto.trim() + "\". Um atendente vai falar com você para ajustar o orçamento."
                        : "Certo! Um atendente vai falar com você para entender o que ajustar no orçamento.",
                CategoriaMensagemSaida.ESCALONAMENTO);

        conversaWhatsappService.escalar(atendimento,
                "Cliente pediu alteração no orçamento nº " + orcamento.getId());

        return ResultadoRespostaCliente.ALTERACAO_REGISTRADA;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public String descreverParaCliente(Orcamento orcamento, AtendimentoWhatsapp atendimento) {

        return switch (orcamento.getStatus()) {

            case APROVADO -> "O orçamento nº " + orcamento.getId() + " já está aprovado. "
                    + "Nossa equipe vai entrar em contato para os próximos passos.";

            case ENVIADO -> orcamento.getAlteracaoSolicitadaEm() != null
                    ? "Já anotamos o seu pedido de alteração no orçamento nº " + orcamento.getId()
                    + ". Um atendente vai falar com você."
                    : "Nossa equipe está revendo o orçamento nº " + orcamento.getId() + ". Em breve te respondemos.";

            case EXPIRADO -> {
                conversaWhatsappService.escalar(atendimento,
                        "Cliente respondeu ao orçamento nº " + orcamento.getId() + " já expirado");
                yield "O orçamento nº " + orcamento.getId() + " venceu"
                        + (orcamento.getValidoAte() != null ? " em " + orcamento.getValidoAte().format(FORMATO_DATA) : "")
                        + ". Vou pedir para um atendente te enviar um orçamento atualizado.";
            }

            case PERDIDO -> {
                conversaWhatsappService.escalar(atendimento,
                        "Cliente respondeu ao orçamento nº " + orcamento.getId() + " já encerrado");
                yield "O orçamento nº " + orcamento.getId() + " foi encerrado. Vou avisar um atendente para "
                        + "conversar com você.";
            }

            default -> "Nossa equipe está atualizando o orçamento nº " + orcamento.getId()
                    + ". Em breve você recebe a versão nova por aqui.";
        };
    }

    private Optional<ResultadoRespostaCliente> validarRespostaCliente(
            Orcamento orcamento, PerguntaPendente pergunta, AtendimentoWhatsapp atendimento) {

        if (orcamento.getStatus() != StatusOrcamento.ENVIADO
                || !pergunta.getVersao().equals(orcamento.getRevisaoEnvio())) {

            perguntaPendenteService.encerrar(pergunta, StatusPergunta.SUBSTITUIDA);

            enviarAoCliente(orcamento, atendimento,
                    "Esse orçamento foi atualizado pela nossa equipe. Confira a mensagem mais recente, por favor.",
                    CategoriaMensagemSaida.BOT);

            return Optional.of(ResultadoRespostaCliente.DESATUALIZADO);
        }

        if (orcamento.getValidoAte() != null && orcamento.getValidoAte().isBefore(fuso.hoje(orcamento.getEmpresa()))) {

            perguntaPendenteService.encerrar(pergunta, StatusPergunta.EXPIRADA);

            expirarInterno(orcamento, null,
                    "Orçamento expirado: o cliente respondeu depois da validade ("
                            + orcamento.getValidoAte().format(FORMATO_DATA) + ")",
                    false, atendimento);

            enviarAoCliente(orcamento, atendimento,
                    "O orçamento nº " + orcamento.getId() + " venceu em "
                            + orcamento.getValidoAte().format(FORMATO_DATA)
                            + ". Vou pedir para um atendente te enviar um orçamento atualizado.",
                    CategoriaMensagemSaida.ESCALONAMENTO);

            conversaWhatsappService.escalar(atendimento,
                    "Cliente respondeu ao orçamento nº " + orcamento.getId() + " depois da validade");

            return Optional.of(ResultadoRespostaCliente.VENCIDO);
        }

        return Optional.empty();
    }

    private void aprovarInterno(
            Orcamento orcamento,
            Usuario responsavel,
            String descricao,
            StatusPergunta statusPergunta,
            AtendimentoWhatsapp atendimento) {

        orcamento.setStatus(StatusOrcamento.APROVADO);
        orcamento.setRespondidoEm(LocalDateTime.now(clock));

        orcamentoRepository.saveAndFlush(orcamento);

        perguntaPendenteService.encerrarDoOrcamento(orcamento.getId(), statusPergunta);

        historicoService.registrarEventoOrcamento(orcamento, TipoEventoHistorico.ORCAMENTO_APROVADO, descricao, responsavel);

        ordemServicoService.criarAutomaticamente(orcamento, responsavel);

        enviarAoCliente(orcamento, atendimento,
                "Orçamento nº " + orcamento.getId() + " aprovado: " + formatarValor(orcamento.getValorTotal())
                        + ". Obrigado pela confiança! Nossa equipe vai entrar em contato para agendar a instalação.",
                atendimento != null ? CategoriaMensagemSaida.BOT : CategoriaMensagemSaida.CONFIRMACAO);
    }

    private void perderInterno(
            Orcamento orcamento,
            MotivoPerda motivo,
            String motivoOutro,
            Usuario responsavel,
            String descricao) {

        orcamento.setStatusAntesPerda(orcamento.getStatus());
        orcamento.setStatus(StatusOrcamento.PERDIDO);
        orcamento.setRespondidoEm(LocalDateTime.now(clock));
        orcamento.setMotivoPerda(motivo);
        orcamento.setMotivoPerdaOutro(motivo == MotivoPerda.OUTRO ? motivoOutro : null);

        orcamentoRepository.saveAndFlush(orcamento);

        perguntaPendenteService.encerrarDoOrcamento(orcamento.getId(), StatusPergunta.CANCELADA);

        historicoService.registrarEventoOrcamento(orcamento, TipoEventoHistorico.ORCAMENTO_PERDIDO,
                descricao + " (motivo: " + motivo + (motivo == MotivoPerda.OUTRO && motivoOutro != null
                        ? " — " + motivoOutro : "") + ")",
                responsavel);

        medicaoService.cancelarPorEncerramentoDoOrcamento(orcamento, responsavel);
    }

    private void expirarInterno(
            Orcamento orcamento,
            Usuario responsavel,
            String descricao,
            boolean avisarCliente,
            AtendimentoWhatsapp atendimento) {

        orcamento.setStatus(StatusOrcamento.EXPIRADO);
        orcamento.setRespondidoEm(LocalDateTime.now(clock));

        orcamentoRepository.saveAndFlush(orcamento);

        perguntaPendenteService.encerrarDoOrcamento(orcamento.getId(), StatusPergunta.EXPIRADA);

        historicoService.registrarEventoOrcamento(orcamento, TipoEventoHistorico.ORCAMENTO_EXPIRADO, descricao, responsavel);

        medicaoService.cancelarPorEncerramentoDoOrcamento(orcamento, responsavel);

        if (avisarCliente) {
            enviarAoCliente(orcamento, atendimento,
                    "Olá, " + primeiroNome(orcamento.getCliente().getNome()) + "! O orçamento nº " + orcamento.getId()
                            + (orcamento.getValidoAte() != null
                            ? " venceu em " + orcamento.getValidoAte().format(FORMATO_DATA) : " venceu")
                            + ". Se ainda tiver interesse, é só responder esta mensagem que preparamos um orçamento atualizado.",
                    CategoriaMensagemSaida.AVISO);
        }
    }

    private void enviarAoCliente(
            Orcamento orcamento, AtendimentoWhatsapp atendimento, String texto, CategoriaMensagemSaida categoria) {

        String telefone = atendimento != null
                ? atendimento.getTelefone()
                : NegociacaoAgendamentoService.telefoneDoCliente(orcamento.getCliente());

        if (telefone == null) {
            return;
        }

        whatsappSaidaService.enfileirar(new NovaMensagem(
                orcamento.getEmpresa(), telefone, orcamento.getCliente(), texto, categoria,
                RemetenteMensagem.BOT, "ORCAMENTO", orcamento.getId(), null, atendimento
        ));
    }

    private void travarContato(Orcamento orcamento) {
        negociacao.travarContato(orcamento);
    }

    private void devolverAoBotSeOPedidoFoiRespondido(
            AtendimentoWhatsapp atendimento, Orcamento orcamento, Usuario responsavel) {

        if (atendimento == null || atendimento.getStatus() != StatusAtendimento.AGUARDANDO_ATENDENTE
                || atendimento.getAtendente() != null) {
            return;
        }

        atendimento.setStatus(StatusAtendimento.EM_FLUXO_BOT);
        atendimento.setEtapaFluxo(EtapaFluxo.MENU);
        atendimento.setTentativasErro(0);

        historicoService.registrarEventoAtendimento(atendimento, TipoEventoHistorico.ATENDIMENTO_DEVOLVIDO_BOT,
                "Pedido respondido com o orçamento nº " + orcamento.getId()
                        + " — a conversa voltou ao atendimento automático", responsavel);
    }

    private AtendimentoWhatsapp atendimentoDaSolicitacao(Orcamento orcamento) {

        if (orcamento.getSolicitacaoOrcamento() == null) {
            return null;
        }

        return atendimentoWhatsappRepository
                .findBySolicitacaoOrcamentoId(orcamento.getSolicitacaoOrcamento().getId())
                .filter(a -> a.getStatus() != StatusAtendimento.ENCERRADO)
                .filter(a -> a.getTelefone().equals(NegociacaoAgendamentoService.telefoneDoCliente(orcamento.getCliente())))
                .orElse(null);
    }

    private String montarMensagemOrcamento(Orcamento orcamento, OrcamentoTotaisResponse totais, boolean revisao) {

        Empresa empresa = orcamento.getEmpresa();
        StringBuilder texto = new StringBuilder();

        texto.append("Olá, ").append(primeiroNome(orcamento.getCliente().getNome())).append("! ");
        texto.append(revisao ? "Segue o seu orçamento atualizado" : "Segue o seu orçamento");
        texto.append(" nº ").append(orcamento.getId());

        String nomeEmpresa = empresa.getNomeFantasia() != null && !empresa.getNomeFantasia().isBlank()
                ? empresa.getNomeFantasia() : empresa.getRazaoSocial();

        if (nomeEmpresa != null && !nomeEmpresa.isBlank()) {
            texto.append(" da ").append(nomeEmpresa.trim());
        }

        texto.append(":\n");

        for (OrcamentoItem item : orcamentoItemRepository.findAllByOrcamentoIdOrderByOrdemAsc(orcamento.getId())) {
            texto.append("\n• ").append(descreverItem(item));
        }

        texto.append("\n\nValor total: ").append(formatarValor(totais.getValorFinal()));

        if (orcamento.getParcelasCartao() != null && orcamento.getParcelasCartao() > 1) {
            texto.append(" (em até ").append(orcamento.getParcelasCartao()).append("x no cartão)");
        }

        if (empresa.getCondicoesPagamento() != null && !empresa.getCondicoesPagamento().isBlank()) {
            texto.append("\nFormas de pagamento: ").append(empresa.getCondicoesPagamento().trim());
        }

        texto.append("\nVálido até ").append(orcamento.getValidoAte().format(FORMATO_DATA)).append('.');

        if (orcamento.getObservacoes() != null && !orcamento.getObservacoes().isBlank()) {
            texto.append("\nObservações: ").append(orcamento.getObservacoes().trim());
        }

        texto.append("\n\nResponda:\n1 - Aprovar\n2 - Pedir alteração\n3 - Falar com um atendente\n4 - Não tenho interesse");

        return texto.toString();
    }

    private String descreverItem(OrcamentoItem item) {

        StringBuilder descricao = new StringBuilder();

        if (item.getQuantidade() != null && item.getQuantidade() > 1) {
            descricao.append(item.getQuantidade()).append("× ");
        }

        descricao.append(item.getTipologia().getNome());

        if (item.getAmbiente() != null && !item.getAmbiente().isBlank()) {
            descricao.append(" (").append(item.getAmbiente().trim()).append(')');
        }

        if (item.getLarguraVaoMm() != null && item.getAlturaVaoMm() != null) {
            descricao.append(" — ").append(item.getLarguraVaoMm()).append(" × ").append(item.getAlturaVaoMm()).append(" mm");
        }

        if (item.getTipoVidro() != null) {

            descricao.append(", vidro ").append(item.getTipoVidro().name().toLowerCase(PT_BR));

            if (item.getEspessuraMm() != null) {
                descricao.append(' ').append(item.getEspessuraMm()).append(" mm");
            }

            if (item.getCor() != null) {
                descricao.append(' ').append(item.getCor().name().toLowerCase(PT_BR));
            }
        }

        return descricao.toString();
    }

    private OrcamentoResponse montarResponseCompleto(Orcamento orcamento, String aviso) {

        Medicao medicao = medicaoRepository.findByOrcamentoId(orcamento.getId()).orElse(null);
        OrdemServico ordem = ordemServicoRepository.findByOrcamentoId(orcamento.getId()).orElse(null);

        return montarResponse(orcamento, medicao, ordem, aviso, true);
    }

    private OrcamentoResponse montarResponse(
            Orcamento orcamento, Medicao medicao, OrdemServico ordem, String aviso, boolean comEnvio) {

        OrcamentoResponse response = orcamentoMapper.toResponse(orcamento);

        response.setAviso(aviso);
        response.setVencido(orcamento.getStatus() == StatusOrcamento.ENVIADO
                && orcamento.getValidoAte() != null
                && orcamento.getValidoAte().isBefore(fuso.hoje(orcamento.getEmpresa())));

        if (medicao != null) {
            response.setMedicaoStatus(medicao.getStatus().name());
            response.setMedicaoData(medicao.getDataAgendada());
        }

        if (ordem != null) {
            response.setOrdemServicoId(ordem.getId());
        }

        MensagemSaida envio = null;

        if (comEnvio && orcamento.getStatus() == StatusOrcamento.ENVIADO) {

            envio = whatsappSaidaService.ultimaDaReferencia("ORCAMENTO", orcamento.getId())
                    .filter(m -> m.getCategoria() == CategoriaMensagemSaida.ORCAMENTO)
                    .orElse(null);

            if (envio != null) {
                response.setEnvioStatus(envio.getStatus().name());
                response.setEnvioErro(envio.getUltimoErro());
            } else if (NegociacaoAgendamentoService.telefoneDoCliente(orcamento.getCliente()) == null) {
                response.setEnvioStatus("SEM_WHATSAPP");
            }
        }

        MensagemSaida envioMedicao = comEnvio && medicao != null
                && medicao.getStatus() == StatusAgendamento.PROPOSTA_ENVIADA
                ? negociacao.envioDaUltimaProposta(medicao).orElse(null)
                : null;

        ProximaAcao proxima = proximaAcao(orcamento, medicao, envioMedicao, ordem, envio, response.isVencido());

        response.setResponsavelProximaAcao(proxima.responsavel());
        response.setProximaAcao(proxima.descricao());

        return response;
    }

    private ProximaAcao proximaAcao(
            Orcamento orcamento, Medicao medicao, MensagemSaida envioMedicao, OrdemServico ordem,
            MensagemSaida envio, boolean vencido) {

        if (medicao != null && STATUS_EDITAVEL.contains(orcamento.getStatus())
                && medicao.getStatus() != StatusAgendamento.REALIZADA
                && medicao.getStatus() != StatusAgendamento.CANCELADA) {

            return negociacao.proximaAcao(medicao, envioMedicao);
        }

        return switch (orcamento.getStatus()) {

            case NOVO_CONTATO, PRE_ORCAMENTO -> new ProximaAcao("EMPRESA",
                    "Agendar a medição ou montar o orçamento com as medidas do cliente");

            case VISITA_AGENDADA -> new ProximaAcao("EMPRESA", "Realizar a visita de medição");

            case MEDIDO, ORCAMENTO_FINAL -> new ProximaAcao("EMPRESA", "Revisar os valores e enviar o orçamento ao cliente");

            case ENVIADO -> {

                if (orcamento.getAlteracaoSolicitadaEm() != null) {
                    yield new ProximaAcao("EMPRESA", "Cliente pediu alteração"
                            + (orcamento.getAlteracaoSolicitadaTexto() != null
                            ? ": \"" + orcamento.getAlteracaoSolicitadaTexto() + "\"" : "")
                            + ". Revise e reenvie, ou aprove se o cliente desistir da alteração");
                }

                if (envio != null && envio.getStatus() == StatusMensagemSaida.FALHOU) {
                    yield new ProximaAcao("EMPRESA", "O orçamento não chegou ao cliente (" + envio.getUltimoErro()
                            + "). Revise e reenvie ou combine por telefone");
                }

                if (envio != null && !envio.getStatus().chegouAoWhatsapp()) {
                    yield new ProximaAcao("SISTEMA", "Orçamento na fila de envio do WhatsApp");
                }

                if (vencido) {
                    yield new ProximaAcao("SISTEMA", "Validade vencida: o orçamento será expirado e o cliente avisado");
                }

                yield new ProximaAcao("CLIENTE", "Aguardando o cliente aprovar o orçamento"
                        + (orcamento.getValidoAte() != null ? " (válido até " + orcamento.getValidoAte().format(FORMATO_DATA) + ")" : ""));
            }

            case APROVADO -> ordem == null
                    ? new ProximaAcao("EMPRESA", "Abrir a ordem de serviço")
                    : new ProximaAcao("EMPRESA", "Acompanhar a ordem de serviço e agendar a instalação");

            case EXPIRADO -> new ProximaAcao("EMPRESA", "Cliente avisado do vencimento: revise e reenvie se ele responder");

            case PERDIDO -> new ProximaAcao("NINGUEM", "Orçamento encerrado");
        };
    }

    private String telefoneObrigatorio(Cliente cliente) {

        String telefone = NegociacaoAgendamentoService.telefoneDoCliente(cliente);

        if (telefone == null) {
            throw new TransicaoInvalidaException(
                    "Este cliente não tem WhatsApp cadastrado. Cadastre o número de WhatsApp (com DDD) para enviar por aqui."
            );
        }

        return telefone;
    }

    private BigDecimal aplicarVariacao(BigDecimal valor, BigDecimal percentual) {

        BigDecimal pct = percentual == null ? BigDecimal.ZERO : percentual;

        return valor.multiply(BigDecimal.ONE.add(pct.divide(BigDecimal.valueOf(100), 6, RoundingMode.HALF_UP)))
                .setScale(0, RoundingMode.HALF_UP)
                .setScale(2, RoundingMode.UNNECESSARY);
    }

    private String primeiroNome(String nomeCompleto) {
        return nomeCompleto == null || nomeCompleto.isBlank() ? "" : nomeCompleto.trim().split("\\s+")[0];
    }

    public static String formatarValor(BigDecimal valor) {
        return NumberFormat.getCurrencyInstance(PT_BR).format(valor == null ? BigDecimal.ZERO : valor);
    }

    private static String limitar(String texto, int maximo) {
        return texto.length() > maximo ? texto.substring(0, maximo) : texto;
    }

    private void exigirStatus(Orcamento orcamento, StatusOrcamento statusEsperado) {

        if (orcamento.getStatus() != statusEsperado) {
            throw new TransicaoInvalidaException(
                    "Esta ação exige que o orçamento esteja com status " + statusEsperado
                            + ", mas está " + orcamento.getStatus()
            );
        }
    }

    private void garantirEditavel(Orcamento orcamento) {

        if (!STATUS_EDITAVEL.contains(orcamento.getStatus())) {
            throw new OrcamentoNaoEditavelException(
                    "Só é possível editar um orçamento antes de ele ser enviado ao cliente"
            );
        }
    }

    private Empresa buscarEmpresa(Long empresaId) {

        return empresaRepository.findById(empresaId)
                .orElseThrow(() -> new EmpresaNaoEncontradaException("Empresa não encontrada"));
    }

    private void verificarEmpresa(Long empresaId) {
        buscarEmpresa(empresaId);
    }

    private Cliente buscarCliente(Long empresaId, Long clienteId) {

        return clienteRepository.findByIdAndEmpresaId(clienteId, empresaId)
                .orElseThrow(() -> new ClienteNaoEncontradoException("Cliente não encontrado"));
    }

    private Orcamento buscarOrcamento(Long empresaId, Long orcamentoId) {

        return orcamentoRepository.findByIdAndEmpresaId(orcamentoId, empresaId)
                .orElseThrow(() -> new OrcamentoNaoEncontradoException("Orçamento não encontrado"));
    }
}
