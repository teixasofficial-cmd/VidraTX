package br.com.vidratx.service;

import br.com.vidratx.config.FusoEmpresa;
import br.com.vidratx.dto.AgendaItemResponse;
import br.com.vidratx.dto.DashboardResumoResponse;
import br.com.vidratx.dto.ProximaAcaoItemResponse;
import br.com.vidratx.entity.Agendamento;
import br.com.vidratx.entity.AtendimentoWhatsapp;
import br.com.vidratx.entity.Empresa;
import br.com.vidratx.entity.Instalacao;
import br.com.vidratx.entity.Medicao;
import br.com.vidratx.entity.MensagemSaida;
import br.com.vidratx.entity.Orcamento;
import br.com.vidratx.entity.OrdemServico;
import br.com.vidratx.entity.SolicitacaoOrcamento;
import br.com.vidratx.enums.CategoriaMensagemSaida;
import br.com.vidratx.enums.StatusAgendamento;
import br.com.vidratx.enums.StatusAtendimento;
import br.com.vidratx.enums.StatusMensagemSaida;
import br.com.vidratx.enums.StatusOrcamento;
import br.com.vidratx.enums.StatusProducao;
import br.com.vidratx.enums.StatusSolicitacaoOrcamento;
import br.com.vidratx.exception.EmpresaNaoEncontradaException;
import br.com.vidratx.mapper.AtendimentoWhatsappMapper;
import br.com.vidratx.repository.AtendimentoWhatsappRepository;
import br.com.vidratx.repository.EmpresaRepository;
import br.com.vidratx.repository.InstalacaoRepository;
import br.com.vidratx.repository.MedicaoRepository;
import br.com.vidratx.repository.MensagemSaidaRepository;
import br.com.vidratx.repository.OrcamentoRepository;
import br.com.vidratx.repository.OrdemServicoRepository;
import br.com.vidratx.repository.SolicitacaoOrcamentoRepository;
import br.com.vidratx.repository.WhatsappInstanciaRepository;
import br.com.vidratx.service.NegociacaoAgendamentoService.ProximaAcao;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private static final DateTimeFormatter FORMATO_DATA = DateTimeFormatter.ofPattern("dd/MM/yyyy", PT_BR);

    private static final List<StatusOrcamento> STATUS_EM_ELABORACAO = List.of(
            StatusOrcamento.NOVO_CONTATO, StatusOrcamento.PRE_ORCAMENTO, StatusOrcamento.VISITA_AGENDADA,
            StatusOrcamento.MEDIDO, StatusOrcamento.ORCAMENTO_FINAL
    );

    private static final Set<StatusAgendamento> AGENDAMENTO_EM_ANDAMENTO = EnumSet.of(
            StatusAgendamento.PROPOSTA_ENVIADA, StatusAgendamento.CONTRAPROPOSTA_CLIENTE,
            StatusAgendamento.RECUSADA_CLIENTE, StatusAgendamento.REAGENDAMENTO_NECESSARIO,
            StatusAgendamento.AGENDADA
    );

    private static final Set<StatusAgendamento> ESPERANDO_A_EMPRESA = EnumSet.of(
            StatusAgendamento.CONTRAPROPOSTA_CLIENTE, StatusAgendamento.RECUSADA_CLIENTE,
            StatusAgendamento.REAGENDAMENTO_NECESSARIO
    );

    private static final Set<StatusAgendamento> NA_AGENDA = EnumSet.of(
            StatusAgendamento.AGENDADA, StatusAgendamento.PROPOSTA_ENVIADA
    );

    private static final Set<StatusAtendimento> CONVERSA_COM_PESSOA = EnumSet.of(
            StatusAtendimento.AGUARDANDO_ATENDENTE, StatusAtendimento.EM_ATENDIMENTO_HUMANO
    );

    private static final Map<String, Integer> ORDEM_RESPONSAVEL = Map.of(
            "EMPRESA", 0, "SISTEMA", 1, "CLIENTE", 2, "NINGUEM", 3
    );

    private final OrcamentoRepository orcamentoRepository;
    private final AtendimentoWhatsappService atendimentoWhatsappService;
    private final AtendimentoWhatsappRepository atendimentoWhatsappRepository;
    private final MedicaoRepository medicaoRepository;
    private final InstalacaoRepository instalacaoRepository;
    private final OrdemServicoRepository ordemServicoRepository;
    private final SolicitacaoOrcamentoRepository solicitacaoOrcamentoRepository;
    private final MensagemSaidaRepository mensagemSaidaRepository;
    private final EmpresaRepository empresaRepository;
    private final WhatsappInstanciaRepository whatsappInstanciaRepository;
    private final NegociacaoAgendamentoService negociacao;
    private final Clock clock;
    private final FusoEmpresa fuso;

    public DashboardService(
            OrcamentoRepository orcamentoRepository,
            AtendimentoWhatsappService atendimentoWhatsappService,
            AtendimentoWhatsappRepository atendimentoWhatsappRepository,
            MedicaoRepository medicaoRepository,
            InstalacaoRepository instalacaoRepository,
            OrdemServicoRepository ordemServicoRepository,
            SolicitacaoOrcamentoRepository solicitacaoOrcamentoRepository,
            MensagemSaidaRepository mensagemSaidaRepository,
            EmpresaRepository empresaRepository,
            WhatsappInstanciaRepository whatsappInstanciaRepository,
            NegociacaoAgendamentoService negociacao,
            Clock clock) {

        this.orcamentoRepository = orcamentoRepository;
        this.atendimentoWhatsappService = atendimentoWhatsappService;
        this.atendimentoWhatsappRepository = atendimentoWhatsappRepository;
        this.medicaoRepository = medicaoRepository;
        this.instalacaoRepository = instalacaoRepository;
        this.ordemServicoRepository = ordemServicoRepository;
        this.solicitacaoOrcamentoRepository = solicitacaoOrcamentoRepository;
        this.mensagemSaidaRepository = mensagemSaidaRepository;
        this.empresaRepository = empresaRepository;
        this.whatsappInstanciaRepository = whatsappInstanciaRepository;
        this.negociacao = negociacao;
        this.clock = clock;
        this.fuso = new FusoEmpresa(clock);
    }

    @Transactional(readOnly = true)
    public DashboardResumoResponse resumo(Long empresaId) {

        DashboardResumoResponse resumo = new DashboardResumoResponse();

        resumo.setOrcamentosRascunho(orcamentoRepository.countByEmpresaIdAndStatusIn(empresaId, STATUS_EM_ELABORACAO));
        resumo.setOrcamentosEnviados(orcamentoRepository.countByEmpresaIdAndStatus(empresaId, StatusOrcamento.ENVIADO));
        resumo.setOrcamentosAprovados(orcamentoRepository.countByEmpresaIdAndStatus(empresaId, StatusOrcamento.APROVADO));

        resumo.setOrcamentosRecusados(
                orcamentoRepository.countByEmpresaIdAndStatus(empresaId, StatusOrcamento.PERDIDO)
                        + orcamentoRepository.countByEmpresaIdAndStatus(empresaId, StatusOrcamento.EXPIRADO)
        );

        resumo.setValorAprovado(orcamentoRepository.somarValorPorEmpresaEStatus(empresaId, StatusOrcamento.APROVADO));

        long totalEnviados = orcamentoRepository.countByEmpresaIdAndEnviadoEmIsNotNull(empresaId);

        resumo.setTotalOrcamentosEnviados(totalEnviados);
        resumo.setValorTotalEnviado(orcamentoRepository.somarValorEnviado(empresaId));
        resumo.setTaxaConversaoPercentual(calcularTaxaConversao(resumo.getOrcamentosAprovados(), totalEnviados));

        whatsappInstanciaRepository.findByEmpresaId(empresaId).ifPresentOrElse(instancia -> {
            resumo.setWhatsappStatus(instancia.getStatus().name());
            resumo.setWhatsappDesconectadoEm(instancia.getDesconectadoEm());
        }, () -> resumo.setWhatsappStatus("NUNCA_CONECTADO"));

        resumo.setAtendimentosPendentesWhatsapp(atendimentoWhatsappService.contarPendentes(empresaId));
        resumo.setAtendimentosAtrasadosWhatsapp(atendimentoWhatsappService.contarAtrasados(empresaId));

        LocalDate hoje = fuso.hoje(empresaRepository.findById(empresaId).orElse(null));

        resumo.setMedicoesHoje(medicaoRepository
                .findAllByOrcamentoEmpresaIdAndStatusAndDataAgendadaBetweenOrderByDataAgendadaAsc(
                        empresaId, StatusAgendamento.AGENDADA, hoje.atStartOfDay(), hoje.plusDays(1).atStartOfDay())
                .size());

        resumo.setInstalacoesHoje(instalacaoRepository
                .findAllByOrdemServicoEmpresaIdAndStatusAndDataAgendadaBetweenOrderByDataAgendadaAsc(
                        empresaId, StatusAgendamento.AGENDADA, hoje.atStartOfDay(), hoje.plusDays(1).atStartOfDay())
                .size());

        List<ProximaAcaoItemResponse> acoes = montarProximasAcoes(empresaId);

        resumo.setAcoesDaEmpresa(acoes.stream().filter(a -> "EMPRESA".equals(a.responsavel())).count());

        resumo.setMedicoesAguardandoCliente(acoes.stream()
                .filter(a -> "MEDICAO".equals(a.tipo()) && "CLIENTE".equals(a.responsavel())).count());
        resumo.setContrapropostasPendentes(acoes.stream()
                .filter(a -> "CONTRAPROPOSTA_CLIENTE".equals(a.status())).count());
        resumo.setInstalacoesAguardandoAgendamento(acoes.stream()
                .filter(a -> "ORDEM_SERVICO".equals(a.tipo())).count());
        resumo.setOrcamentosAprovadosSemOs(acoes.stream()
                .filter(a -> "ORCAMENTO".equals(a.tipo()) && "APROVADO".equals(a.status())).count());
        resumo.setAguardandoCliente(acoes.stream().filter(a -> "CLIENTE".equals(a.responsavel())).count());
        resumo.setMensagensNaoEntregues(mensagemSaidaRepository.countByEmpresaIdAndStatusAndCriadoEmAfter(
                empresaId, StatusMensagemSaida.FALHOU, LocalDateTime.now(clock).minusDays(7)));

        return resumo;
    }

    @Transactional(readOnly = true)
    public List<ProximaAcaoItemResponse> proximasAcoes(Long empresaId) {
        return montarProximasAcoes(empresaId);
    }

    private List<ProximaAcaoItemResponse> montarProximasAcoes(Long empresaId) {

        LocalDateTime agora = fuso.agora(empresaRepository.findById(empresaId).orElse(null));
        List<ProximaAcaoItemResponse> itens = new ArrayList<>();

        List<Medicao> medicoes = medicaoRepository
                .findAllByOrcamentoEmpresaIdAndStatusInOrderByDataAgendadaAsc(empresaId, AGENDAMENTO_EM_ANDAMENTO);

        Map<Long, MensagemSaida> enviosMedicao = ultimosEnvios("MEDICAO",
                medicoes.stream().map(Medicao::getId).toList(), CategoriaMensagemSaida.PROPOSTA_DATA);

        for (Medicao medicao : medicoes) {

            if (!relevanteAgora(medicao, agora)) {
                continue;
            }

            itens.add(itemDeAgendamento("MEDICAO", medicao, medicao.getOrcamento().getId(), null,
                    medicao.getOrcamento().getCliente().getNome(), enviosMedicao.get(medicao.getId()), agora));
        }

        for (Medicao medicao : medicaoRepository.findAllByOrcamentoEmpresaIdAndStatusAndOrcamentoStatusIn(
                empresaId, StatusAgendamento.REALIZADA, STATUS_EM_ELABORACAO)) {

            itens.add(new ProximaAcaoItemResponse(
                    "ORCAMENTO", medicao.getOrcamento().getId(), medicao.getOrcamento().getId(), null, null,
                    medicao.getOrcamento().getCliente().getNome(), medicao.getOrcamento().getStatus().name(),
                    "EMPRESA", "Medição realizada: montar e enviar o orçamento final",
                    medicao.getDataAgendada(), false
            ));
        }

        List<Instalacao> instalacoes = instalacaoRepository
                .findAllByOrdemServicoEmpresaIdAndStatusInOrderByDataAgendadaAsc(empresaId, AGENDAMENTO_EM_ANDAMENTO);

        Map<Long, MensagemSaida> enviosInstalacao = ultimosEnvios("INSTALACAO",
                instalacoes.stream().map(Instalacao::getId).toList(), CategoriaMensagemSaida.PROPOSTA_DATA);

        for (Instalacao instalacao : instalacoes) {

            if (!relevanteAgora(instalacao, agora)) {
                continue;
            }

            OrdemServico ordem = instalacao.getOrdemServico();

            itens.add(itemDeAgendamento("INSTALACAO", instalacao, ordem.getOrcamento().getId(), ordem.getId(),
                    ordem.getOrcamento().getCliente().getNome(), enviosInstalacao.get(instalacao.getId()), agora));
        }

        for (OrdemServico ordem : ordemServicoRepository.findSemInstalacao(empresaId)) {

            boolean produzindo = Boolean.TRUE.equals(ordem.getNecessitaProducao())
                    && ordem.getStatusProducao() != StatusProducao.CONFERIDO;

            itens.add(new ProximaAcaoItemResponse(
                    "ORDEM_SERVICO", ordem.getId(), ordem.getOrcamento().getId(), ordem.getId(), null,
                    ordem.getOrcamento().getCliente().getNome(),
                    produzindo ? String.valueOf(ordem.getStatusProducao()) : "AGUARDANDO_AGENDAMENTO",
                    "EMPRESA",
                    produzindo
                            ? "Produção " + descreverProducao(ordem.getStatusProducao()) + ": conferir antes de agendar a instalação"
                            : "Agendar a instalação",
                    ordem.getCriadoEm(), false
            ));
        }

        itens.addAll(acoesDeOrcamentos(empresaId, agora));
        itens.addAll(acoesDeConversas(empresaId));

        for (SolicitacaoOrcamento solicitacao : solicitacaoOrcamentoRepository
                .findAllByEmpresaIdAndStatusOrderByCriadoEmDesc(empresaId, StatusSolicitacaoOrcamento.RECEBIDA)) {

            itens.add(new ProximaAcaoItemResponse(
                    "SOLICITACAO", solicitacao.getId(), null, null, null,
                    solicitacao.getCliente() != null ? solicitacao.getCliente().getNome() : null,
                    solicitacao.getStatus().name(), "EMPRESA", "Criar o orçamento da solicitação recebida pelo WhatsApp",
                    solicitacao.getCriadoEm(), false
            ));
        }

        for (MensagemSaida falha : mensagemSaidaRepository.findAllByEmpresaIdAndStatusOrderByIdDesc(
                empresaId, StatusMensagemSaida.FALHOU)) {

            if (falha.getCategoria() != CategoriaMensagemSaida.ATENDENTE
                    || falha.getCriadoEm() == null || falha.getCriadoEm().isBefore(agora.minusDays(7))) {
                continue;
            }

            itens.add(new ProximaAcaoItemResponse(
                    "CONVERSA", falha.getId(), null, null,
                    falha.getAtendimento() != null ? falha.getAtendimento().getId() : null,
                    falha.getAtendimento() != null && falha.getAtendimento().getCliente() != null
                            ? falha.getAtendimento().getCliente().getNome() : falha.getTelefone(),
                    falha.getStatus().name(), "EMPRESA",
                    "Mensagem não entregue ao cliente (" + falha.getUltimoErro() + "): reenviar ou ligar",
                    falha.getCriadoEm(), true
            ));
        }

        itens.sort(Comparator
                .comparing((ProximaAcaoItemResponse a) -> ORDEM_RESPONSAVEL.getOrDefault(a.responsavel(), 9))
                .thenComparing(a -> !a.urgente())
                .thenComparing(ProximaAcaoItemResponse::data, Comparator.nullsLast(Comparator.naturalOrder())));

        return itens;
    }

    private List<ProximaAcaoItemResponse> acoesDeOrcamentos(Long empresaId, LocalDateTime agora) {

        List<StatusOrcamento> status = new ArrayList<>(STATUS_EM_ELABORACAO);
        status.add(StatusOrcamento.ENVIADO);
        status.add(StatusOrcamento.APROVADO);

        List<Orcamento> orcamentos = orcamentoRepository.findAllByEmpresaIdAndStatusIn(empresaId, status);
        List<Long> ids = orcamentos.stream().map(Orcamento::getId).toList();

        if (ids.isEmpty()) {
            return List.of();
        }

        Map<Long, Medicao> medicoes = medicaoRepository.findAllByOrcamentoIdIn(ids).stream()
                .collect(Collectors.toMap(m -> m.getOrcamento().getId(), Function.identity(), (a, b) -> a));

        Set<Long> comOrdem = ordemServicoRepository.findAllByOrcamentoIdIn(ids).stream()
                .map(o -> o.getOrcamento().getId())
                .collect(Collectors.toSet());

        Map<Long, MensagemSaida> envios = ultimosEnvios("ORCAMENTO", ids, CategoriaMensagemSaida.ORCAMENTO);

        List<ProximaAcaoItemResponse> itens = new ArrayList<>();

        for (Orcamento orcamento : orcamentos) {

            Medicao medicao = medicoes.get(orcamento.getId());
            String cliente = orcamento.getCliente() != null ? orcamento.getCliente().getNome() : null;

            if (medicao != null && STATUS_EM_ELABORACAO.contains(orcamento.getStatus())
                    && (AGENDAMENTO_EM_ANDAMENTO.contains(medicao.getStatus())
                    || medicao.getStatus() == StatusAgendamento.REALIZADA)) {
                continue;
            }

            switch (orcamento.getStatus()) {

                case NOVO_CONTATO, PRE_ORCAMENTO, VISITA_AGENDADA -> itens.add(item(orcamento, cliente, "EMPRESA",
                        "Agendar a medição ou montar o orçamento com as medidas do cliente", orcamento.getCriadoEm(), false));

                case MEDIDO, ORCAMENTO_FINAL -> itens.add(item(orcamento, cliente, "EMPRESA",
                        "Revisar os valores e enviar o orçamento ao cliente", orcamento.getAtualizadoEm(), false));

                case ENVIADO -> {

                    MensagemSaida envio = envios.get(orcamento.getId());
                    boolean vencido = orcamento.getValidoAte() != null && orcamento.getValidoAte().isBefore(agora.toLocalDate());

                    if (orcamento.getAlteracaoSolicitadaEm() != null) {
                        itens.add(item(orcamento, cliente, "EMPRESA", "Cliente pediu alteração"
                                        + (orcamento.getAlteracaoSolicitadaTexto() != null
                                        ? ": \"" + orcamento.getAlteracaoSolicitadaTexto() + "\"" : "")
                                        + ". Revisar e reenviar",
                                orcamento.getAlteracaoSolicitadaEm(), true));
                    } else if (envio != null && envio.getStatus() == StatusMensagemSaida.FALHOU) {
                        itens.add(item(orcamento, cliente, "EMPRESA", "O orçamento não chegou ao cliente ("
                                + envio.getUltimoErro() + "). Revisar e reenviar ou combinar por telefone", orcamento.getEnviadoEm(), true));
                    } else if (envio != null && !envio.getStatus().chegouAoWhatsapp()) {
                        itens.add(item(orcamento, cliente, "SISTEMA", "Orçamento na fila de envio do WhatsApp",
                                orcamento.getEnviadoEm(), false));
                    } else if (vencido) {
                        itens.add(item(orcamento, cliente, "SISTEMA",
                                "Validade vencida: o orçamento será expirado e o cliente avisado", orcamento.getEnviadoEm(), false));
                    } else {
                        itens.add(item(orcamento, cliente, "CLIENTE", "Aguardando o cliente aprovar"
                                        + (orcamento.getValidoAte() != null
                                        ? " (válido até " + orcamento.getValidoAte().format(FORMATO_DATA) + ")" : ""),
                                orcamento.getEnviadoEm(), false));
                    }
                }

                case APROVADO -> {
                    if (!comOrdem.contains(orcamento.getId())) {
                        itens.add(item(orcamento, cliente, "EMPRESA", "Abrir a ordem de serviço", orcamento.getRespondidoEm(), true));
                    }
                }

                default -> {
                }
            }
        }

        return itens;
    }

    private List<ProximaAcaoItemResponse> acoesDeConversas(Long empresaId) {

        List<ProximaAcaoItemResponse> itens = new ArrayList<>();

        LocalDateTime agora = LocalDateTime.now(clock);

        for (AtendimentoWhatsapp atendimento : atendimentoWhatsappRepository
                .findAllByEmpresaIdAndStatusIn(empresaId, CONVERSA_COM_PESSOA)) {

            String cliente = atendimento.getCliente() != null ? atendimento.getCliente().getNome() : atendimento.getTelefone();

            String atraso = atendimento.respostaAtrasada(agora)
                    ? " — esperando há " + AtendimentoWhatsappMapper.descreverEspera(atendimento.minutosEsperandoResposta(agora))
                            + " (prazo de " + atendimento.prazoRespostaMinutos() + " min)"
                    : "";

            if (atendimento.getStatus() == StatusAtendimento.AGUARDANDO_ATENDENTE) {

                itens.add(new ProximaAcaoItemResponse(
                        "CONVERSA", atendimento.getId(), null, null, atendimento.getId(), cliente,
                        atendimento.getStatus().name(), "EMPRESA", "Assumir a conversa e responder o cliente" + atraso,
                        atendimento.getUltimaMensagemClienteEm() != null ? atendimento.inicioDaEspera() : atendimento.getAtualizadoEm(),
                        atendimento.clienteAguardandoResposta()
                ));

            } else if (atendimento.clienteAguardandoResposta()) {

                itens.add(new ProximaAcaoItemResponse(
                        "CONVERSA", atendimento.getId(), null, null, atendimento.getId(), cliente,
                        atendimento.getStatus().name(), "EMPRESA",
                        "Responder o cliente" + (atendimento.getAtendente() != null
                                ? " (com " + atendimento.getAtendente().getNome() + ")" : "") + atraso,
                        atendimento.inicioDaEspera(), true
                ));
            }
        }

        return itens;
    }

    private ProximaAcaoItemResponse itemDeAgendamento(
            String tipo, Agendamento agendamento, Long orcamentoId, Long ordemServicoId,
            String cliente, MensagemSaida envio, LocalDateTime agora) {

        boolean passou = agendamento.getDataAgendada() != null && agendamento.getDataAgendada().isBefore(agora);

        if (agendamento.getStatus() == StatusAgendamento.AGENDADA && passou) {

            return new ProximaAcaoItemResponse(
                    tipo, agendamento.getId(), orcamentoId, ordemServicoId, null, cliente, agendamento.getStatus().name(),
                    "EMPRESA",
                    (tipo.equals("MEDICAO") ? "A visita de " : "A instalação de ")
                            + NegociacaoAgendamentoService.formatar(agendamento.getDataAgendada())
                            + " já passou: marcar como realizada ou remarcar",
                    agendamento.getDataAgendada(), true
            );
        }

        ProximaAcao proxima = negociacao.proximaAcao(agendamento,
                agendamento.getStatus() == StatusAgendamento.PROPOSTA_ENVIADA ? envio : null);

        boolean urgente = ESPERANDO_A_EMPRESA.contains(agendamento.getStatus())
                || agendamento.getCancelamentoSolicitadoEm() != null
                || (envio != null && envio.getStatus() == StatusMensagemSaida.FALHOU
                && agendamento.getStatus() == StatusAgendamento.PROPOSTA_ENVIADA);

        return new ProximaAcaoItemResponse(
                tipo, agendamento.getId(), orcamentoId, ordemServicoId, null, cliente, agendamento.getStatus().name(),
                proxima.responsavel(), proxima.descricao(), agendamento.getDataAgendada(), urgente
        );
    }

    private boolean relevanteAgora(Agendamento agendamento, LocalDateTime agora) {

        if (agendamento.getStatus() != StatusAgendamento.AGENDADA
                || agendamento.getCancelamentoSolicitadoEm() != null) {
            return true;
        }

        return agendamento.getDataAgendada() != null
                && agendamento.getDataAgendada().isBefore(agora.toLocalDate().plusDays(2).atStartOfDay());
    }

    private ProximaAcaoItemResponse item(
            Orcamento orcamento, String cliente, String responsavel, String descricao, LocalDateTime data, boolean urgente) {

        return new ProximaAcaoItemResponse(
                "ORCAMENTO", orcamento.getId(), orcamento.getId(), null, null, cliente,
                orcamento.getStatus().name(), responsavel, descricao, data, urgente
        );
    }

    private Map<Long, MensagemSaida> ultimosEnvios(String referenciaTipo, Collection<Long> ids, CategoriaMensagemSaida categoria) {

        if (ids.isEmpty()) {
            return Map.of();
        }

        Map<Long, MensagemSaida> ultimos = new HashMap<>();

        for (MensagemSaida envio : mensagemSaidaRepository.findAllByReferenciaTipoAndReferenciaIdIn(referenciaTipo, ids)) {

            if (envio.getCategoria() != categoria) {
                continue;
            }

            ultimos.merge(envio.getReferenciaId(), envio, (a, b) -> a.getId() > b.getId() ? a : b);
        }

        return ultimos;
    }

    private String descreverProducao(StatusProducao status) {

        if (status == null) {
            return "não iniciada";
        }

        return switch (status) {
            case AGUARDANDO_PRODUCAO -> "aguardando início";
            case EM_PRODUCAO -> "em andamento";
            case PRONTO -> "pronta";
            case CONFERIDO -> "conferida";
        };
    }

    @Transactional(readOnly = true)
    public List<AgendaItemResponse> agenda(Long empresaId, LocalDate de, LocalDate ate) {

        if (ate.isBefore(de)) {
            throw new IllegalArgumentException("A data final da agenda deve ser igual ou posterior à inicial");
        }

        if (de.plusDays(62).isBefore(ate)) {
            throw new IllegalArgumentException("Consulte a agenda em períodos de até 62 dias");
        }

        Empresa empresa = empresaRepository.findById(empresaId)
                .orElseThrow(() -> new EmpresaNaoEncontradaException("Empresa não encontrada"));

        int duracaoMedicao = empresa.getDuracaoMedicaoMinutos() != null ? empresa.getDuracaoMedicaoMinutos() : 60;
        int duracaoInstalacao = empresa.getDuracaoInstalacaoMinutos() != null ? empresa.getDuracaoInstalacaoMinutos() : 240;

        LocalDateTime inicio = de.atStartOfDay();
        LocalDateTime fim = ate.plusDays(1).atStartOfDay();

        List<AgendaItemResponse> itens = new ArrayList<>();

        for (Medicao medicao : medicaoRepository
                .findAllByOrcamentoEmpresaIdAndStatusInAndDataAgendadaBetweenOrderByDataAgendadaAsc(empresaId, NA_AGENDA, inicio, fim)) {

            itens.add(new AgendaItemResponse(
                    "MEDICAO", medicao.getId(), medicao.getOrcamento().getId(), null,
                    medicao.getOrcamento().getCliente().getNome(), medicao.getEndereco(), null,
                    medicao.getStatus().name(), medicao.getDataAgendada(),
                    medicao.getDataAgendada().plusMinutes(duracaoMedicao),
                    medicao.getCancelamentoSolicitadoEm() != null
            ));
        }

        for (Instalacao instalacao : instalacaoRepository
                .findAllByOrdemServicoEmpresaIdAndStatusInAndDataAgendadaBetweenOrderByDataAgendadaAsc(empresaId, NA_AGENDA, inicio, fim)) {

            OrdemServico ordem = instalacao.getOrdemServico();

            itens.add(new AgendaItemResponse(
                    "INSTALACAO", instalacao.getId(), ordem.getOrcamento().getId(), ordem.getId(),
                    ordem.getOrcamento().getCliente().getNome(), instalacao.getEndereco(),
                    instalacao.getEquipeResponsavel(), instalacao.getStatus().name(), instalacao.getDataAgendada(),
                    instalacao.getDataAgendada().plusMinutes(duracaoInstalacao),
                    instalacao.getCancelamentoSolicitadoEm() != null
            ));
        }

        itens.sort(Comparator.comparing(AgendaItemResponse::inicio));

        return itens;
    }

    private double calcularTaxaConversao(long aprovados, long totalEnviados) {

        if (totalEnviados == 0) {
            return 0.0;
        }

        return BigDecimal.valueOf(aprovados * 100.0 / totalEnviados)
                .setScale(1, RoundingMode.HALF_UP)
                .doubleValue();
    }
}
