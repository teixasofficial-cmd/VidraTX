package br.com.vidratx.service;

import br.com.vidratx.config.FusoEmpresa;
import br.com.vidratx.conversa.ExtratorData;
import br.com.vidratx.entity.Agendamento;
import br.com.vidratx.entity.AtendimentoWhatsapp;
import br.com.vidratx.entity.Cliente;
import br.com.vidratx.entity.Empresa;
import br.com.vidratx.entity.Instalacao;
import br.com.vidratx.entity.Medicao;
import br.com.vidratx.entity.MensagemSaida;
import br.com.vidratx.entity.Orcamento;
import br.com.vidratx.entity.PerguntaPendente;
import br.com.vidratx.entity.PropostaAgendamento;
import br.com.vidratx.entity.Usuario;
import br.com.vidratx.entity.WhatsappInstancia;
import br.com.vidratx.enums.CategoriaMensagemSaida;
import br.com.vidratx.enums.OrigemProposta;
import br.com.vidratx.enums.RemetenteMensagem;
import br.com.vidratx.enums.StatusAgendamento;
import br.com.vidratx.enums.StatusInstanciaWhatsapp;
import br.com.vidratx.enums.StatusMensagemSaida;
import br.com.vidratx.enums.StatusPergunta;
import br.com.vidratx.enums.StatusProposta;
import br.com.vidratx.enums.TipoAgendamento;
import br.com.vidratx.enums.TipoEventoHistorico;
import br.com.vidratx.enums.TipoPergunta;
import br.com.vidratx.exception.AgendamentoInvalidoException;
import br.com.vidratx.exception.ConflitoAgendaException;
import br.com.vidratx.exception.TransicaoInvalidaException;
import br.com.vidratx.repository.EmpresaRepository;
import br.com.vidratx.repository.InstalacaoRepository;
import br.com.vidratx.repository.MedicaoRepository;
import br.com.vidratx.repository.PropostaAgendamentoRepository;
import br.com.vidratx.repository.WhatsappInstanciaRepository;
import br.com.vidratx.service.WhatsappSaidaService.NovaMensagem;
import br.com.vidratx.util.DinheiroUtils;
import br.com.vidratx.util.TelefoneUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.text.NumberFormat;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;

@Service
public class NegociacaoAgendamentoService {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private static final DateTimeFormatter FORMATO_DATA_HORA =
            DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy 'às' HH:mm", PT_BR);

    private static final DateTimeFormatter FORMATO_CURTO =
            DateTimeFormatter.ofPattern("dd/MM 'às' HH:mm", PT_BR);

    public static final Set<StatusAgendamento> STATUS_PODE_REAGENDAR_OU_CANCELAR = EnumSet.of(
            StatusAgendamento.PROPOSTA_ENVIADA, StatusAgendamento.AGENDADA,
            StatusAgendamento.RECUSADA_CLIENTE, StatusAgendamento.CONTRAPROPOSTA_CLIENTE,
            StatusAgendamento.REAGENDAMENTO_NECESSARIO
    );

    private final PropostaAgendamentoRepository propostaRepository;
    private final MedicaoRepository medicaoRepository;
    private final InstalacaoRepository instalacaoRepository;
    private final EmpresaRepository empresaRepository;
    private final WhatsappInstanciaRepository whatsappInstanciaRepository;
    private final PerguntaPendenteService perguntaPendenteService;
    private final WhatsappSaidaService whatsappSaidaService;
    private final WhatsappContatoService whatsappContatoService;
    private final HistoricoService historicoService;
    private final ParametroCalculoService parametroCalculoService;
    private final FusoEmpresa fusoEmpresa;
    private final Clock clock;

    public NegociacaoAgendamentoService(
            PropostaAgendamentoRepository propostaRepository,
            MedicaoRepository medicaoRepository,
            InstalacaoRepository instalacaoRepository,
            EmpresaRepository empresaRepository,
            WhatsappInstanciaRepository whatsappInstanciaRepository,
            PerguntaPendenteService perguntaPendenteService,
            WhatsappSaidaService whatsappSaidaService,
            WhatsappContatoService whatsappContatoService,
            HistoricoService historicoService,
            ParametroCalculoService parametroCalculoService,
            Clock clock) {

        this.propostaRepository = propostaRepository;
        this.medicaoRepository = medicaoRepository;
        this.instalacaoRepository = instalacaoRepository;
        this.empresaRepository = empresaRepository;
        this.whatsappInstanciaRepository = whatsappInstanciaRepository;
        this.perguntaPendenteService = perguntaPendenteService;
        this.whatsappSaidaService = whatsappSaidaService;
        this.whatsappContatoService = whatsappContatoService;
        this.historicoService = historicoService;
        this.parametroCalculoService = parametroCalculoService;
        this.fusoEmpresa = new FusoEmpresa(clock);
        this.clock = clock;
    }

    public enum ResultadoConfirmacao {
        CONFIRMADA,
        CONFLITO,
        DATA_PASSOU,
        PROPOSTA_DESATUALIZADA
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void travarContato(Orcamento orcamento) {

        String telefone = telefoneDoCliente(orcamento.getCliente());

        if (telefone != null) {
            whatsappContatoService.travar(orcamento.getEmpresa(), telefone);
        }
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public PropostaAgendamento proporData(
            Agendamento agendamento,
            LocalDateTime data,
            Usuario responsavel,
            String motivoRecusaCliente) {

        Orcamento orcamento = agendamento.getOrcamentoReferencia();
        Empresa empresa = orcamento.getEmpresa();

        validarDataProposta(agendamento, data);
        exigirEquipeNaInstalacao(agendamento);

        travarContato(orcamento);
        empresaRepository.travarPorId(empresa.getId());

        verificarConflito(agendamento, data, agendamento.getEquipeResponsavel());

        StatusAgendamento statusAnterior = agendamento.getStatus();
        LocalDateTime dataAnterior = agendamento.getDataAgendada();
        String sugestaoRecusada = null;

        for (PropostaAgendamento pendente : pendentes(agendamento)) {

            if (pendente.getOrigem() == OrigemProposta.CLIENTE) {

                sugestaoRecusada = pendente.getTextoCliente();
                pendente.setStatus(StatusProposta.RECUSADA);
                pendente.setMotivo(motivoRecusaCliente != null && !motivoRecusaCliente.isBlank()
                        ? motivoRecusaCliente.trim()
                        : "A empresa propôs outra data");
                pendente.setRespondidoPor(responsavel);

            } else {

                pendente.setStatus(StatusProposta.SUBSTITUIDA);
            }

            pendente.setRespondidoEm(agora());
            propostaRepository.save(pendente);
        }

        int versao = (agendamento.getVersaoProposta() == null ? 0 : agendamento.getVersaoProposta()) + 1;

        agendamento.setVersaoProposta(versao);
        agendamento.setDataAgendada(data);
        agendamento.setStatus(StatusAgendamento.PROPOSTA_ENVIADA);
        agendamento.setContrapropostaTexto(null);
        limparPedidoDeCancelamento(agendamento);
        salvar(agendamento);

        PropostaAgendamento proposta = new PropostaAgendamento();

        proposta.setEmpresa(empresa);
        proposta.vincular(agendamento);
        proposta.setVersao(versao);
        proposta.setOrigem(OrigemProposta.EMPRESA);
        proposta.setDataProposta(data);
        proposta.setEquipe(agendamento.getEquipeResponsavel());
        proposta.setStatus(StatusProposta.PENDENTE);
        proposta.setCriadoPor(responsavel);

        String preambulo = montarPreambulo(agendamento, statusAnterior, dataAnterior, sugestaoRecusada, motivoRecusaCliente);
        String texto = preambulo + textoProposta(agendamento, data);

        proposta.setMensagemSaida(enviarComPergunta(
                agendamento, TipoPergunta.confirmar(agendamento.getTipoAgendamento()), versao,
                "Confirmar " + rotulo(agendamento) + " de " + data.format(FORMATO_CURTO),
                texto, CategoriaMensagemSaida.PROPOSTA_DATA
        ));

        PropostaAgendamento salva = propostaRepository.save(proposta);

        if (sugestaoRecusada != null) {

            registrarHistorico(agendamento, "CONTRAPROPOSTA_RECUSADA",
                    "Empresa recusou a data sugerida pelo cliente (\"" + sugestaoRecusada + "\")"
                            + (motivoRecusaCliente != null && !motivoRecusaCliente.isBlank()
                            ? ": " + motivoRecusaCliente.trim() : "")
                            + " e propôs " + formatar(data),
                    responsavel);

        } else if (statusAnterior == null || dataAnterior == null || agendamento.getVersaoProposta() == 1) {

            registrarHistorico(agendamento, "AGENDADA",
                    "Data proposta ao cliente: " + formatar(data) + " (aguardando confirmação)", responsavel);

        } else {

            registrarHistorico(agendamento, "REAGENDADA",
                    "Nova data proposta: " + formatar(data) + " (antes: " + formatar(dataAnterior)
                            + ", " + descreverStatus(statusAnterior) + "); aguardando confirmação do cliente",
                    responsavel);
        }

        return salva;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void aceitarContraproposta(Agendamento agendamento, LocalDateTime data, Usuario responsavel) {

        Orcamento orcamento = agendamento.getOrcamentoReferencia();

        travarContato(orcamento);
        exigirStatus(agendamento, StatusAgendamento.CONTRAPROPOSTA_CLIENTE);

        if (data == null || !data.isAfter(fusoEmpresa.agora(orcamento.getEmpresa()))) {
            throw new AgendamentoInvalidoException("dataAgendada",
                    "A data precisa estar no futuro (horário da empresa: "
                            + fusoEmpresa.agora(orcamento.getEmpresa()).format(FORMATO_CURTO) + ")");
        }

        exigirEquipeNaInstalacao(agendamento);

        empresaRepository.travarPorId(orcamento.getEmpresa().getId());
        verificarConflito(agendamento, data, agendamento.getEquipeResponsavel());

        PropostaAgendamento sugestao = pendentes(agendamento).stream()
                .filter(p -> p.getOrigem() == OrigemProposta.CLIENTE)
                .reduce((a, b) -> b)
                .orElse(null);

        String textoSugestao = sugestao != null ? sugestao.getTextoCliente() : agendamento.getContrapropostaTexto();

        for (PropostaAgendamento pendente : pendentes(agendamento)) {

            if (pendente == sugestao) {
                pendente.setStatus(StatusProposta.ACEITA);
                pendente.setDataProposta(data);
                pendente.setEquipe(agendamento.getEquipeResponsavel());
                pendente.setRespondidoPor(responsavel);
            } else {
                pendente.setStatus(StatusProposta.SUBSTITUIDA);
            }

            pendente.setRespondidoEm(agora());
            propostaRepository.save(pendente);
        }

        agendamento.setDataAgendada(data);
        agendamento.setStatus(StatusAgendamento.AGENDADA);
        salvar(agendamento);

        encerrarPerguntas(agendamento, StatusPergunta.RESPONDIDA);

        enviarSemPergunta(agendamento,
                "Combinado! " + capitalizar(nomeCompleto(agendamento)) + " ficou confirmada para "
                        + formatar(data)
                        + (textoSugestao != null ? ", conforme a sua sugestão (\"" + textoSugestao + "\")" : "")
                        + ". Se houver algum engano, responda REMARCAR.",
                CategoriaMensagemSaida.CONFIRMACAO, null);

        registrarHistorico(agendamento, "CONTRAPROPOSTA_ACEITA",
                "Sugestão do cliente" + (textoSugestao != null ? " (\"" + textoSugestao + "\")" : "")
                        + " aceita: confirmado para " + formatar(data),
                responsavel);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void confirmarManualmente(Agendamento agendamento, Usuario responsavel) {

        Orcamento orcamento = agendamento.getOrcamentoReferencia();

        travarContato(orcamento);
        exigirStatus(agendamento, StatusAgendamento.PROPOSTA_ENVIADA);

        empresaRepository.travarPorId(orcamento.getEmpresa().getId());
        verificarConflito(agendamento, agendamento.getDataAgendada(), agendamento.getEquipeResponsavel());

        for (PropostaAgendamento pendente : pendentes(agendamento)) {

            pendente.setStatus(pendente.getOrigem() == OrigemProposta.EMPRESA
                    ? StatusProposta.ACEITA
                    : StatusProposta.SUBSTITUIDA);
            pendente.setRespondidoPor(responsavel);
            pendente.setRespondidoEm(agora());
            propostaRepository.save(pendente);
        }

        agendamento.setStatus(StatusAgendamento.AGENDADA);
        salvar(agendamento);

        encerrarPerguntas(agendamento, StatusPergunta.CANCELADA);

        enviarSemPergunta(agendamento,
                "Confirmado: " + nomeCompleto(agendamento) + " ficou para "
                        + formatar(agendamento.getDataAgendada()) + ". Obrigado!",
                CategoriaMensagemSaida.CONFIRMACAO, null);

        registrarHistorico(agendamento, "CONFIRMADA_MANUAL",
                "Data " + formatar(agendamento.getDataAgendada()) + " confirmada manualmente por "
                        + (responsavel != null ? responsavel.getNome() : "um usuário")
                        + " (cliente confirmou por outro canal)",
                responsavel);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void cancelar(Agendamento agendamento, Usuario responsavel, String motivo) {

        Orcamento orcamento = agendamento.getOrcamentoReferencia();

        travarContato(orcamento);
        exigirStatusEmAny(agendamento, STATUS_PODE_REAGENDAR_OU_CANCELAR);

        StatusAgendamento statusAnterior = agendamento.getStatus();

        for (PropostaAgendamento pendente : pendentes(agendamento)) {

            pendente.setStatus(StatusProposta.CANCELADA);
            pendente.setRespondidoPor(responsavel);
            pendente.setRespondidoEm(agora());
            propostaRepository.save(pendente);
        }

        agendamento.setStatus(StatusAgendamento.CANCELADA);
        limparPedidoDeCancelamento(agendamento);
        salvar(agendamento);

        encerrarPerguntas(agendamento, StatusPergunta.CANCELADA);

        enviarSemPergunta(agendamento,
                capitalizar(nomeCompleto(agendamento))
                        + (statusAnterior == StatusAgendamento.AGENDADA
                        ? " marcada para " + formatar(agendamento.getDataAgendada()) + " foi cancelada"
                        : " que estávamos combinando foi cancelada")
                        + (motivo != null && !motivo.isBlank() ? " (" + motivo.trim() + ")" : "")
                        + ". Se precisar de alguma coisa, é só responder esta mensagem.",
                CategoriaMensagemSaida.CANCELAMENTO, null);

        registrarHistorico(agendamento, "CANCELADA",
                capitalizar(rotulo(agendamento)) + " cancelada (estava " + descreverStatus(statusAnterior) + ")"
                        + (motivo != null && !motivo.isBlank() ? ": " + motivo.trim() : ""),
                responsavel);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void expirarProposta(Agendamento agendamento, AtendimentoWhatsapp atendimento, boolean respostaDoCliente) {

        if (agendamento.getStatus() != StatusAgendamento.PROPOSTA_ENVIADA) {
            return;
        }

        LocalDateTime data = agendamento.getDataAgendada();

        for (PropostaAgendamento pendente : pendentes(agendamento)) {

            pendente.setStatus(StatusProposta.EXPIRADA);
            pendente.setMotivo("A data passou sem confirmação do cliente");
            pendente.setRespondidoEm(agora());
            propostaRepository.save(pendente);
        }

        agendamento.setStatus(StatusAgendamento.REAGENDAMENTO_NECESSARIO);
        salvar(agendamento);

        encerrarPerguntas(agendamento, StatusPergunta.EXPIRADA);

        enviarSemPergunta(agendamento,
                respostaDoCliente
                        ? "A data que tínhamos proposto (" + formatar(data) + ") já passou. "
                        + "Nossa equipe vai te propor uma nova data em breve."
                        : "A data que propusemos para " + nomeCompleto(agendamento) + " (" + formatar(data)
                        + ") passou sem confirmação. Nossa equipe vai te propor uma nova data.",
                respostaDoCliente ? CategoriaMensagemSaida.BOT : CategoriaMensagemSaida.AVISO,
                atendimento);

        registrarHistorico(agendamento, "REAGENDAMENTO_NECESSARIO",
                "Proposta de " + formatar(data) + " vencida sem confirmação do cliente"
                        + (respostaDoCliente ? " (cliente respondeu depois da data)" : ""),
                null);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public ResultadoConfirmacao confirmarPeloCliente(
            Agendamento agendamento,
            PerguntaPendente pergunta,
            AtendimentoWhatsapp atendimento) {

        if (agendamento.getStatus() != StatusAgendamento.PROPOSTA_ENVIADA
                || !pergunta.getVersao().equals(agendamento.getVersaoProposta())) {

            perguntaPendenteService.encerrar(pergunta, StatusPergunta.SUBSTITUIDA);

            enviarSemPergunta(agendamento,
                    "Essa proposta de data foi atualizada pela nossa equipe. "
                            + "Confira a mensagem mais recente, por favor.",
                    CategoriaMensagemSaida.BOT, atendimento);

            return ResultadoConfirmacao.PROPOSTA_DESATUALIZADA;
        }

        if (agendamento.getDataAgendada().isBefore(agoraNaEmpresa(agendamento))) {

            expirarProposta(agendamento, atendimento, true);

            return ResultadoConfirmacao.DATA_PASSOU;
        }

        empresaRepository.travarPorId(agendamento.getOrcamentoReferencia().getEmpresa().getId());

        Optional<String> conflito = descreverConflito(
                agendamento, agendamento.getDataAgendada(), agendamento.getEquipeResponsavel()
        );

        if (conflito.isPresent()) {

            for (PropostaAgendamento pendente : pendentes(agendamento)) {

                pendente.setStatus(StatusProposta.EXPIRADA);
                pendente.setMotivo("Horário ocupado antes da confirmação: " + conflito.get());
                pendente.setRespondidoPeloCliente(true);
                pendente.setRespondidoEm(agora());
                propostaRepository.save(pendente);
            }

            agendamento.setStatus(StatusAgendamento.REAGENDAMENTO_NECESSARIO);
            salvar(agendamento);

            perguntaPendenteService.encerrar(pergunta, StatusPergunta.RESPONDIDA);

            enviarSemPergunta(agendamento,
                    "Esse horário acabou de ser preenchido por outro atendimento. "
                            + "Nossa equipe vai te propor uma nova data em breve.",
                    CategoriaMensagemSaida.BOT, atendimento);

            registrarHistorico(agendamento, "REAGENDAMENTO_NECESSARIO",
                    "Cliente confirmou " + formatar(agendamento.getDataAgendada())
                            + ", mas o horário já estava ocupado (" + conflito.get() + ")",
                    null);

            return ResultadoConfirmacao.CONFLITO;
        }

        for (PropostaAgendamento pendente : pendentes(agendamento)) {

            pendente.setStatus(pendente.getOrigem() == OrigemProposta.EMPRESA
                    ? StatusProposta.ACEITA
                    : StatusProposta.SUBSTITUIDA);
            pendente.setRespondidoPeloCliente(true);
            pendente.setRespondidoEm(agora());
            propostaRepository.save(pendente);
        }

        agendamento.setStatus(StatusAgendamento.AGENDADA);
        salvar(agendamento);

        perguntaPendenteService.encerrar(pergunta, StatusPergunta.RESPONDIDA);

        enviarSemPergunta(agendamento,
                "Confirmado! " + capitalizar(nomeCompleto(agendamento)) + " está confirmada para "
                        + formatar(agendamento.getDataAgendada()) + ". Até lá!",
                CategoriaMensagemSaida.BOT, atendimento);

        registrarHistorico(agendamento, "CONFIRMADA_CLIENTE",
                "Cliente confirmou pelo WhatsApp: " + formatar(agendamento.getDataAgendada()), null);

        return ResultadoConfirmacao.CONFIRMADA;
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void recusarPeloCliente(Agendamento agendamento, PerguntaPendente pergunta, AtendimentoWhatsapp atendimento) {

        LocalDateTime data = agendamento.getDataAgendada();

        for (PropostaAgendamento pendente : pendentes(agendamento)) {

            if (pendente.getOrigem() == OrigemProposta.EMPRESA) {
                pendente.setStatus(StatusProposta.RECUSADA);
                pendente.setRespondidoPeloCliente(true);
                pendente.setRespondidoEm(agora());
                propostaRepository.save(pendente);
            }
        }

        agendamento.setStatus(StatusAgendamento.RECUSADA_CLIENTE);
        salvar(agendamento);

        perguntaPendenteService.encerrar(pergunta, StatusPergunta.RESPONDIDA);

        pedirDataAoCliente(agendamento, atendimento,
                "Sem problemas! Qual dia e horário ficam melhores para você? (ex.: sexta às 14h)");

        registrarHistorico(agendamento, "RECUSADA_CLIENTE",
                "Cliente não pode em " + formatar(data) + "; aguardando nova data", null);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void registrarSugestaoCliente(
            Agendamento agendamento,
            PerguntaPendente pergunta,
            String texto,
            AtendimentoWhatsapp atendimento) {

        boolean refinamento = agendamento.getStatus() == StatusAgendamento.CONTRAPROPOSTA_CLIENTE;
        LocalDateTime dataEmpresa = agendamento.getDataAgendada();

        for (PropostaAgendamento pendente : pendentes(agendamento)) {

            if (pendente.getOrigem() == OrigemProposta.EMPRESA) {
                pendente.setStatus(StatusProposta.RECUSADA);
                pendente.setMotivo("Cliente sugeriu outra data");
                pendente.setRespondidoPeloCliente(true);
            } else {
                pendente.setStatus(StatusProposta.SUBSTITUIDA);
            }

            pendente.setRespondidoEm(agora());
            propostaRepository.save(pendente);
        }

        PropostaAgendamento sugestao = new PropostaAgendamento();

        sugestao.setEmpresa(agendamento.getOrcamentoReferencia().getEmpresa());
        sugestao.vincular(agendamento);
        sugestao.setVersao(agendamento.getVersaoProposta() == null ? 0 : agendamento.getVersaoProposta());
        sugestao.setOrigem(OrigemProposta.CLIENTE);
        sugestao.setTextoCliente(texto);

        LocalDateTime dataEntendida = ExtratorData.extrair(texto, agoraNaEmpresa(agendamento))
                .filter(data -> !data.equals(dataEmpresa))
                .orElse(null);
        sugestao.setDataProposta(dataEntendida);
        sugestao.setStatus(StatusProposta.PENDENTE);
        propostaRepository.save(sugestao);

        agendamento.setStatus(StatusAgendamento.CONTRAPROPOSTA_CLIENTE);
        agendamento.setContrapropostaTexto(texto);
        salvar(agendamento);

        if (pergunta.getTipo().ehConfirmacaoDeData()) {

            perguntaPendenteService.encerrar(pergunta, StatusPergunta.RESPONDIDA);

            perguntaPendenteService.abrir(
                    agendamento.getOrcamentoReferencia().getEmpresa(),
                    telefoneDoCliente(agendamento.getOrcamentoReferencia().getCliente()),
                    agendamento.getOrcamentoReferencia().getCliente(),
                    TipoPergunta.sugerirData(agendamento.getTipoAgendamento()),
                    agendamento.getId(),
                    sugestao.getVersao(),
                    "Nova data para " + rotulo(agendamento) + " (sugestão enviada)",
                    "Se quiser, mande outra sugestão de dia e horário para " + nomeCompleto(agendamento) + ".",
                    null
            );

        } else {

            pergunta.setTentativas(0);
        }

        String entendido = dataEntendida != null ? " (entendi: " + formatar(dataEntendida) + ")" : "";

        enviarSemPergunta(agendamento,
                refinamento
                        ? "Anotado, atualizamos a sua sugestão: \"" + texto + "\"" + entendido
                        + ". Assim que a equipe confirmar, te avisamos por aqui."
                        : "Anotado: \"" + texto + "\"" + entendido + ". Vamos verificar com a equipe e te confirmamos por aqui.",
                CategoriaMensagemSaida.BOT, atendimento);

        registrarHistorico(agendamento, "CONTRAPROPOSTA_CLIENTE",
                refinamento
                        ? "Cliente atualizou a sugestão de data: \"" + texto + "\" — aguardando decisão da empresa"
                        : "Cliente sugeriu outra data em vez de " + formatar(dataEmpresa) + ": \"" + texto
                        + "\" — aguardando decisão da empresa",
                null);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void solicitarReagendamentoPeloCliente(
            Agendamento agendamento,
            String texto,
            boolean textoTemData,
            AtendimentoWhatsapp atendimento) {

        exigirStatus(agendamento, StatusAgendamento.AGENDADA);

        LocalDateTime data = agendamento.getDataAgendada();

        agendamento.setStatus(StatusAgendamento.REAGENDAMENTO_NECESSARIO);
        salvar(agendamento);

        registrarHistorico(agendamento, "REAGENDAMENTO_NECESSARIO",
                "Cliente pediu para remarcar " + rotulo(agendamento) + " de " + formatar(data) + ": \"" + texto + "\"",
                null);

        if (textoTemData) {

            PerguntaPendente sugestao = perguntaPendenteService.abrir(
                    agendamento.getOrcamentoReferencia().getEmpresa(),
                    telefoneDoCliente(agendamento.getOrcamentoReferencia().getCliente()),
                    agendamento.getOrcamentoReferencia().getCliente(),
                    TipoPergunta.sugerirData(agendamento.getTipoAgendamento()),
                    agendamento.getId(),
                    agendamento.getVersaoProposta() == null ? 0 : agendamento.getVersaoProposta(),
                    "Nova data para " + rotulo(agendamento),
                    "Qual dia e horário ficam melhores para " + nomeCompleto(agendamento) + "?",
                    null
            );

            registrarSugestaoCliente(agendamento, sugestao, texto, atendimento);
            return;
        }

        pedirDataAoCliente(agendamento, atendimento,
                "Sem problemas, vamos remarcar " + nomeCompleto(agendamento) + " de " + formatar(data)
                        + ". Qual dia e horário ficam melhores para você? (ex.: sexta às 14h)");
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void registrarPedidoDeCancelamento(Agendamento agendamento, String texto) {

        exigirStatus(agendamento, StatusAgendamento.AGENDADA);

        agendamento.setCancelamentoSolicitadoEm(agora());
        agendamento.setCancelamentoSolicitadoTexto(texto);
        salvar(agendamento);

        registrarHistorico(agendamento, "CANCELAMENTO_SOLICITADO",
                "Cliente pediu pelo WhatsApp para cancelar " + rotulo(agendamento) + " de "
                        + formatar(agendamento.getDataAgendada()) + ": \"" + texto + "\" — confirmar com ele",
                null);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public void manterData(Agendamento agendamento, Usuario responsavel) {

        travarContato(agendamento.getOrcamentoReferencia());
        exigirStatus(agendamento, StatusAgendamento.AGENDADA);

        if (agendamento.getCancelamentoSolicitadoEm() == null) {
            throw new TransicaoInvalidaException("Não há pedido de cancelamento em aberto para " + rotulo(agendamento));
        }

        limparPedidoDeCancelamento(agendamento);
        salvar(agendamento);

        enviarSemPergunta(agendamento,
                "Combinado: " + nomeCompleto(agendamento) + " continua marcada para "
                        + formatar(agendamento.getDataAgendada()) + ".",
                CategoriaMensagemSaida.CONFIRMACAO, null);

        registrarHistorico(agendamento, "DATA_MANTIDA",
                "Pedido de cancelamento descartado por " + (responsavel != null ? responsavel.getNome() : "um usuário")
                        + ": " + rotulo(agendamento) + " continua em " + formatar(agendamento.getDataAgendada()),
                responsavel);
    }

    public void limparPedidoDeCancelamento(Agendamento agendamento) {
        agendamento.setCancelamentoSolicitadoEm(null);
        agendamento.setCancelamentoSolicitadoTexto(null);
    }

    @Transactional(readOnly = true)
    public Optional<PropostaAgendamento> sugestaoPendente(Agendamento agendamento) {

        return pendentes(agendamento).stream()
                .filter(p -> p.getOrigem() == OrigemProposta.CLIENTE)
                .reduce((a, b) -> b);
    }

    @Transactional(readOnly = true)
    public List<PropostaAgendamento> historicoPropostas(Agendamento agendamento) {

        return agendamento instanceof Medicao
                ? propostaRepository.findAllByMedicaoIdOrderByIdAsc(agendamento.getId())
                : propostaRepository.findAllByInstalacaoIdOrderByIdAsc(agendamento.getId());
    }

    @Transactional(readOnly = true)
    public Optional<MensagemSaida> envioDaUltimaProposta(Agendamento agendamento) {

        Optional<PropostaAgendamento> ultima = agendamento instanceof Medicao
                ? propostaRepository.findFirstByMedicaoIdAndOrigemOrderByIdDesc(agendamento.getId(), OrigemProposta.EMPRESA)
                : propostaRepository.findFirstByInstalacaoIdAndOrigemOrderByIdDesc(agendamento.getId(), OrigemProposta.EMPRESA);

        return ultima.map(PropostaAgendamento::getMensagemSaida);
    }

    @Transactional(readOnly = true)
    public String avisoEnvio(Orcamento orcamento) {

        if (telefoneDoCliente(orcamento.getCliente()) == null) {
            return "O cliente não tem WhatsApp cadastrado: combine a data por telefone e use "
                    + "\"Confirmar manualmente\" quando ele confirmar.";
        }

        WhatsappInstancia instancia = whatsappInstanciaRepository
                .findByEmpresaId(orcamento.getEmpresa().getId())
                .orElse(null);

        if (instancia == null || instancia.getStatus() != StatusInstanciaWhatsapp.CONECTADO) {
            return "O WhatsApp da empresa está desconectado: a proposta fica na fila e é enviada "
                    + "assim que a conexão voltar.";
        }

        return null;
    }

    public record ProximaAcao(String responsavel, String descricao) {
    }

    public ProximaAcao proximaAcao(Agendamento agendamento, MensagemSaida envio) {

        LocalDateTime data = agendamento.getDataAgendada();

        return switch (agendamento.getStatus()) {

            case PROPOSTA_ENVIADA -> {

                if (envio == null && telefoneDoCliente(agendamento.getOrcamentoReferencia().getCliente()) == null) {
                    yield new ProximaAcao("EMPRESA",
                            "Cliente sem WhatsApp: combinar a data de " + formatar(data) + " por telefone e confirmar manualmente");
                }

                if (envio != null && envio.getStatus() == StatusMensagemSaida.FALHOU) {
                    yield new ProximaAcao("EMPRESA",
                            "A proposta de " + formatar(data) + " não chegou ao cliente (" + envio.getUltimoErro()
                                    + "). Proponha de novo ou confirme por telefone");
                }

                if (envio != null && !envio.getStatus().chegouAoWhatsapp()) {
                    yield new ProximaAcao("SISTEMA",
                            "Proposta de " + formatar(data) + " na fila de envio"
                                    + (envio.getUltimoErro() != null ? " (" + envio.getUltimoErro() + ")" : ""));
                }

                yield new ProximaAcao("CLIENTE", "Aguardando o cliente responder à proposta de " + formatar(data));
            }

            case CONTRAPROPOSTA_CLIENTE -> new ProximaAcao("EMPRESA",
                    "Cliente sugeriu: \"" + agendamento.getContrapropostaTexto()
                            + "\". Aceite informando a data exata ou recuse propondo outra");

            case RECUSADA_CLIENTE -> new ProximaAcao("EMPRESA",
                    "Cliente não pode em " + formatar(data) + ". Proponha outra data (ele também pode sugerir uma)");

            case REAGENDAMENTO_NECESSARIO -> new ProximaAcao("EMPRESA", "Propor uma nova data ao cliente");

            case AGENDADA -> agendamento.getCancelamentoSolicitadoEm() != null
                    ? new ProximaAcao("EMPRESA", "Cliente pediu para cancelar (\""
                    + agendamento.getCancelamentoSolicitadoTexto() + "\"): confirme com ele e cancele — ou mantenha a data de "
                    + formatar(data))
                    : new ProximaAcao("EMPRESA", "Comparecer em " + formatar(data) + " e marcar como realizada");

            case REALIZADA -> agendamento.getTipoAgendamento() == TipoAgendamento.MEDICAO
                    ? new ProximaAcao("EMPRESA", "Montar e enviar o orçamento final")
                    : new ProximaAcao("NINGUEM", "Instalação concluída");

            case CANCELADA -> new ProximaAcao("NINGUEM", "Cancelada");
        };
    }

    public void verificarConflito(Agendamento agendamento, LocalDateTime data, String equipe) {

        descreverConflito(agendamento, data, equipe).ifPresent(descricao -> {
            throw new ConflitoAgendaException("Horário indisponível: " + descricao + ". Escolha outro horário.");
        });
    }

    public Optional<String> descreverConflito(Agendamento agendamento, LocalDateTime data, String equipe) {

        Empresa empresa = agendamento.getOrcamentoReferencia().getEmpresa();
        Long ignorarId = agendamento.getId() == null ? -1L : agendamento.getId();

        if (agendamento.getTipoAgendamento() == TipoAgendamento.MEDICAO) {

            int duracao = positivo(empresa.getDuracaoMedicaoMinutos(), 60);
            int capacidade = positivo(empresa.getMedicoesSimultaneas(), 1);

            List<Medicao> conflitantes = medicaoRepository.findConfirmadasNaJanela(
                    empresa.getId(), ignorarId, data.minusMinutes(duracao), data.plusMinutes(duracao)
            );

            if (conflitantes.size() >= capacidade) {

                Medicao primeira = conflitantes.get(0);

                return Optional.of("já existe medição confirmada em "
                        + primeira.getDataAgendada().format(FORMATO_CURTO)
                        + " (" + primeira.getOrcamento().getCliente().getNome() + ")");
            }

            return Optional.empty();
        }

        if (equipe == null || equipe.isBlank()) {
            return Optional.empty();
        }

        int duracao = positivo(empresa.getDuracaoInstalacaoMinutos(), 240);

        List<Instalacao> conflitantes = instalacaoRepository.findConfirmadasDaEquipeNaJanela(
                empresa.getId(), ignorarId, equipe.trim().toLowerCase(Locale.ROOT),
                data.minusMinutes(duracao), data.plusMinutes(duracao)
        );

        if (!conflitantes.isEmpty()) {

            Instalacao primeira = conflitantes.get(0);

            return Optional.of("a equipe \"" + equipe.trim() + "\" já tem instalação confirmada em "
                    + primeira.getDataAgendada().format(FORMATO_CURTO)
                    + " (" + primeira.getOrdemServico().getOrcamento().getCliente().getNome() + ")");
        }

        return Optional.empty();
    }

    private void pedirDataAoCliente(Agendamento agendamento, AtendimentoWhatsapp atendimento, String texto) {

        Cliente cliente = agendamento.getOrcamentoReferencia().getCliente();
        String telefone = telefoneDoCliente(cliente);

        if (telefone == null) {
            return;
        }

        String textoCompleto = texto + "\n\nSe preferir falar com um atendente, responda 9.";

        PerguntaPendente pergunta = perguntaPendenteService.abrir(
                agendamento.getOrcamentoReferencia().getEmpresa(), telefone, cliente,
                TipoPergunta.sugerirData(agendamento.getTipoAgendamento()),
                agendamento.getId(),
                agendamento.getVersaoProposta() == null ? 0 : agendamento.getVersaoProposta(),
                "Informar uma nova data para " + rotulo(agendamento),
                textoCompleto,
                null
        );

        whatsappSaidaService.enfileirar(new NovaMensagem(
                agendamento.getOrcamentoReferencia().getEmpresa(), telefone, cliente, textoCompleto,
                CategoriaMensagemSaida.BOT, RemetenteMensagem.BOT,
                agendamento.getTipoAgendamento().name(), agendamento.getId(), pergunta, atendimento
        ));
    }

    private MensagemSaida enviarComPergunta(
            Agendamento agendamento,
            TipoPergunta tipo,
            int versao,
            String resumo,
            String texto,
            CategoriaMensagemSaida categoria) {

        Orcamento orcamento = agendamento.getOrcamentoReferencia();
        Cliente cliente = orcamento.getCliente();
        String telefone = telefoneDoCliente(cliente);

        if (telefone == null) {
            return null;
        }

        PerguntaPendente pergunta = perguntaPendenteService.abrir(
                orcamento.getEmpresa(), telefone, cliente, tipo, agendamento.getId(), versao, resumo, texto, null
        );

        return whatsappSaidaService.enfileirar(new NovaMensagem(
                orcamento.getEmpresa(), telefone, cliente, texto, categoria, RemetenteMensagem.BOT,
                agendamento.getTipoAgendamento().name(), agendamento.getId(), pergunta, null
        ));
    }

    private MensagemSaida enviarSemPergunta(
            Agendamento agendamento,
            String texto,
            CategoriaMensagemSaida categoria,
            AtendimentoWhatsapp atendimento) {

        Orcamento orcamento = agendamento.getOrcamentoReferencia();
        Cliente cliente = orcamento.getCliente();
        String telefone = atendimento != null ? atendimento.getTelefone() : telefoneDoCliente(cliente);

        if (telefone == null) {
            return null;
        }

        return whatsappSaidaService.enfileirar(new NovaMensagem(
                orcamento.getEmpresa(), telefone, cliente, texto, categoria, RemetenteMensagem.BOT,
                agendamento.getTipoAgendamento().name(), agendamento.getId(), null, atendimento
        ));
    }

    private String textoProposta(Agendamento agendamento, LocalDateTime data) {

        Cliente cliente = agendamento.getOrcamentoReferencia().getCliente();
        StringBuilder texto = new StringBuilder();

        texto.append("Olá, ").append(primeiroNome(cliente)).append("! Podemos agendar ")
                .append(nomeCompleto(agendamento)).append(" para ").append(formatar(data));

        if (agendamento.getEndereco() != null && !agendamento.getEndereco().isBlank()) {
            texto.append(", no endereço ").append(agendamento.getEndereco().trim());
        }

        texto.append('?');

        if (agendamento.getEquipeResponsavel() != null && !agendamento.getEquipeResponsavel().isBlank()) {
            texto.append(" Equipe: ").append(agendamento.getEquipeResponsavel().trim()).append('.');
        }

        if (agendamento.getTipoAgendamento() == TipoAgendamento.MEDICAO) {

            Long valorVisita = parametroCalculoService
                    .buscarOuCriarPadrao(agendamento.getOrcamentoReferencia().getEmpresa().getId())
                    .getValorVisitaTecnicaCentavos();

            if (valorVisita != null && valorVisita > 0) {
                texto.append(" Valor da visita técnica: ")
                        .append(NumberFormat.getCurrencyInstance(PT_BR).format(DinheiroUtils.paraReais(valorVisita)))
                        .append('.');
            }
        }

        texto.append("\n\nResponda:\n1 - Confirmar\n2 - Sugerir outra data\n3 - Falar com um atendente");

        return texto.toString();
    }

    private String montarPreambulo(
            Agendamento agendamento,
            StatusAgendamento statusAnterior,
            LocalDateTime dataAnterior,
            String sugestaoRecusada,
            String motivo) {

        if (sugestaoRecusada != null) {
            return "Infelizmente não conseguimos atender na data que você sugeriu (\"" + sugestaoRecusada + "\")"
                    + (motivo != null && !motivo.isBlank() ? ": " + motivo.trim() : "") + ".\n\n";
        }

        if (statusAnterior == StatusAgendamento.AGENDADA && dataAnterior != null) {
            return "Precisamos remarcar " + nomeCompleto(agendamento) + " que estava marcada para "
                    + formatar(dataAnterior) + ".\n\n";
        }

        return "";
    }

    private List<PropostaAgendamento> pendentes(Agendamento agendamento) {

        if (agendamento.getId() == null) {
            return List.of();
        }

        return agendamento instanceof Medicao
                ? propostaRepository.findAllByMedicaoIdAndStatus(agendamento.getId(), StatusProposta.PENDENTE)
                : propostaRepository.findAllByInstalacaoIdAndStatus(agendamento.getId(), StatusProposta.PENDENTE);
    }

    private void encerrarPerguntas(Agendamento agendamento, StatusPergunta status) {

        if (agendamento instanceof Medicao) {
            perguntaPendenteService.encerrarDaMedicao(agendamento.getId(), status);
        } else {
            perguntaPendenteService.encerrarDaInstalacao(agendamento.getId(), status);
        }
    }

    private void salvar(Agendamento agendamento) {

        if (agendamento instanceof Medicao medicao) {
            medicaoRepository.saveAndFlush(medicao);
        } else if (agendamento instanceof Instalacao instalacao) {
            instalacaoRepository.saveAndFlush(instalacao);
        }
    }

    private void registrarHistorico(Agendamento agendamento, String sufixo, String descricao, Usuario responsavel) {

        if (agendamento instanceof Medicao medicao) {

            historicoService.registrarEventoOrcamento(
                    medicao.getOrcamento(), TipoEventoHistorico.valueOf("MEDICAO_" + sufixo), descricao, responsavel
            );

        } else if (agendamento instanceof Instalacao instalacao) {

            historicoService.registrarEventoInstalacao(
                    instalacao.getOrdemServico().getEmpresa(),
                    instalacao.getOrdemServico().getOrcamento().getCliente(),
                    instalacao, TipoEventoHistorico.valueOf("INSTALACAO_" + sufixo), descricao, responsavel
            );
        }
    }

    private void exigirStatus(Agendamento agendamento, StatusAgendamento esperado) {

        if (agendamento.getStatus() != esperado) {
            throw new TransicaoInvalidaException(
                    "Esta ação exige " + rotulo(agendamento) + " com status " + esperado
                            + ", mas está " + agendamento.getStatus()
            );
        }
    }

    private void exigirStatusEmAny(Agendamento agendamento, Set<StatusAgendamento> permitidos) {

        if (!permitidos.contains(agendamento.getStatus())) {
            throw new TransicaoInvalidaException(
                    "Esta ação não é permitida com " + rotulo(agendamento) + " no status " + agendamento.getStatus()
            );
        }
    }

    public static String telefoneDoCliente(Cliente cliente) {

        if (cliente == null || cliente.getWhatsapp() == null || cliente.getWhatsapp().isBlank()) {
            return null;
        }

        return TelefoneUtils.canonico(cliente.getWhatsapp());
    }

    public static String formatar(LocalDateTime data) {
        return data == null ? "" : data.format(FORMATO_DATA_HORA);
    }

    private static String rotulo(Agendamento agendamento) {
        return agendamento.getTipoAgendamento() == TipoAgendamento.MEDICAO ? "a medição" : "a instalação";
    }

    private static String nomeCompleto(Agendamento agendamento) {
        return agendamento.getTipoAgendamento() == TipoAgendamento.MEDICAO
                ? "a visita técnica para medição"
                : "a instalação";
    }

    private static String descreverStatus(StatusAgendamento status) {

        if (status == null) {
            return "sem data";
        }

        return switch (status) {
            case PROPOSTA_ENVIADA -> "aguardando o cliente";
            case CONTRAPROPOSTA_CLIENTE -> "com sugestão do cliente";
            case RECUSADA_CLIENTE -> "recusada pelo cliente";
            case REAGENDAMENTO_NECESSARIO -> "precisando de nova data";
            case AGENDADA -> "confirmada";
            case REALIZADA -> "realizada";
            case CANCELADA -> "cancelada";
        };
    }

    private static String capitalizar(String texto) {
        return texto.isEmpty() ? texto : Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }

    private static String primeiroNome(Cliente cliente) {

        if (cliente == null || cliente.getNome() == null || cliente.getNome().isBlank()) {
            return "";
        }

        return cliente.getNome().trim().split("\\s+")[0];
    }

    private static int positivo(Integer valor, int padrao) {
        return valor != null && valor > 0 ? valor : padrao;
    }

    public LocalDateTime agoraNaEmpresa(Agendamento agendamento) {
        return fusoEmpresa.agora(agendamento.getOrcamentoReferencia().getEmpresa());
    }

    private void validarDataProposta(Agendamento agendamento, LocalDateTime data) {

        Empresa empresa = agendamento.getOrcamentoReferencia().getEmpresa();
        LocalDateTime agora = fusoEmpresa.agora(empresa);
        int antecedencia = empresa.getAntecedenciaMinimaMinutos() != null
                ? Math.max(0, empresa.getAntecedenciaMinimaMinutos()) : 0;

        if (data == null || !data.isAfter(agora)) {
            throw new AgendamentoInvalidoException("dataAgendada",
                    "A data precisa estar no futuro (horário da empresa: " + agora.format(FORMATO_CURTO) + ")");
        }

        if (data.isBefore(agora.plusMinutes(antecedencia))) {
            throw new AgendamentoInvalidoException("dataAgendada",
                    "Proponha com pelo menos " + descreverMinutos(antecedencia) + " de antecedência (a partir de "
                            + agora.plusMinutes(antecedencia).format(FORMATO_CURTO)
                            + "): o cliente precisa de tempo para ler e responder. "
                            + "A antecedência mínima fica em Configurações › Agenda.");
        }
    }

    private void exigirEquipeNaInstalacao(Agendamento agendamento) {

        if (agendamento.getTipoAgendamento() == TipoAgendamento.INSTALACAO
                && (agendamento.getEquipeResponsavel() == null || agendamento.getEquipeResponsavel().isBlank())) {

            throw new AgendamentoInvalidoException("equipeResponsavel",
                    "Informe a equipe ou o responsável pela instalação");
        }
    }

    private static String descreverMinutos(int minutos) {

        if (minutos % 60 == 0) {
            int horas = minutos / 60;
            return horas + (horas == 1 ? " hora" : " horas");
        }

        return minutos + " minutos";
    }

    private LocalDateTime agora() {
        return LocalDateTime.now(clock);
    }
}
