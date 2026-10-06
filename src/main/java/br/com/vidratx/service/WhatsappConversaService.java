package br.com.vidratx.service;

import br.com.vidratx.config.FusoEmpresa;
import br.com.vidratx.conversa.ExtratorData;
import br.com.vidratx.conversa.Interpretacao;
import br.com.vidratx.conversa.InterpretadorResposta;
import br.com.vidratx.entity.Agendamento;
import br.com.vidratx.entity.AtendimentoWhatsapp;
import br.com.vidratx.entity.Instalacao;
import br.com.vidratx.entity.Medicao;
import br.com.vidratx.entity.MensagemAtendimento;
import br.com.vidratx.entity.MensagemRecebida;
import br.com.vidratx.entity.MensagemSaida;
import br.com.vidratx.entity.Orcamento;
import br.com.vidratx.entity.PerguntaPendente;
import br.com.vidratx.entity.WhatsappContato;
import br.com.vidratx.enums.CategoriaMensagemSaida;
import br.com.vidratx.enums.EtapaFluxo;
import br.com.vidratx.enums.RemetenteMensagem;
import br.com.vidratx.enums.StatusAgendamento;
import br.com.vidratx.enums.StatusAtendimento;
import br.com.vidratx.enums.StatusMensagemRecebida;
import br.com.vidratx.enums.StatusOrcamento;
import br.com.vidratx.enums.StatusPergunta;
import br.com.vidratx.enums.TipoAgendamento;
import br.com.vidratx.enums.TipoEventoHistorico;
import br.com.vidratx.enums.TipoMensagem;
import br.com.vidratx.enums.TipoMensagemRecebida;
import br.com.vidratx.enums.TipoPergunta.TipoReferenciaPergunta;
import br.com.vidratx.repository.InstalacaoRepository;
import br.com.vidratx.repository.MedicaoRepository;
import br.com.vidratx.repository.MensagemAtendimentoRepository;
import br.com.vidratx.repository.MensagemRecebidaRepository;
import br.com.vidratx.repository.MensagemSaidaRepository;
import br.com.vidratx.repository.OrcamentoRepository;
import br.com.vidratx.repository.PerguntaPendenteRepository;
import br.com.vidratx.service.ConversaWhatsappService.ConversaAberta;
import br.com.vidratx.service.FluxoAtendimentoService.RespostaFluxo;
import br.com.vidratx.service.WhatsappSaidaService.NovaMensagem;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.text.NumberFormat;
import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class WhatsappConversaService {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy", PT_BR);

    private static final Set<StatusAtendimento> MODO_HUMANO =
            EnumSet.of(StatusAtendimento.AGUARDANDO_ATENDENTE, StatusAtendimento.EM_ATENDIMENTO_HUMANO);

    private static final Set<EtapaFluxo> ETAPAS_DE_CAPTACAO = EnumSet.of(
            EtapaFluxo.COLETA_NOME, EtapaFluxo.COLETA_SERVICO, EtapaFluxo.COLETA_DESCRICAO,
            EtapaFluxo.CONFIRMACAO, EtapaFluxo.CORRECAO, EtapaFluxo.DUVIDA
    );

    private static final Set<StatusAgendamento> AGUARDANDO_DATA_DO_CLIENTE = EnumSet.of(
            StatusAgendamento.RECUSADA_CLIENTE, StatusAgendamento.REAGENDAMENTO_NECESSARIO,
            StatusAgendamento.CONTRAPROPOSTA_CLIENTE
    );

    private static final int MAXIMO_ITENS_LISTA = 8;

    private static final Pattern VOCABULARIO_DE_QUANDO = Pattern.compile(
            "\\b(dia|dias|semana|mes|manha|tarde|noite|almoco|cedo|horario|hora|horas|feriado|"
                    + "janeiro|fevereiro|marco|abril|maio|junho|julho|agosto|setembro|outubro|novembro|dezembro)\\b"
    );

    private static final String NAO_SUPORTADA =
            "Ainda não consigo ouvir áudios nem abrir vídeos, figurinhas ou documentos por aqui. "
                    + "Pode escrever a sua mensagem? Se preferir falar com um atendente, responda 9.";

    private final MensagemRecebidaRepository mensagemRecebidaRepository;
    private final MensagemSaidaRepository mensagemSaidaRepository;
    private final MensagemAtendimentoRepository mensagemAtendimentoRepository;
    private final PerguntaPendenteRepository perguntaPendenteRepository;
    private final MedicaoRepository medicaoRepository;
    private final InstalacaoRepository instalacaoRepository;
    private final OrcamentoRepository orcamentoRepository;
    private final WhatsappContatoService whatsappContatoService;
    private final ConversaWhatsappService conversaWhatsappService;
    private final PerguntaPendenteService perguntaPendenteService;
    private final WhatsappSaidaService whatsappSaidaService;
    private final FluxoAtendimentoService fluxoAtendimentoService;
    private final MedicaoService medicaoService;
    private final InstalacaoService instalacaoService;
    private final OrcamentoService orcamentoService;
    private final HistoricoService historicoService;
    private final TransactionTemplate transactionTemplate;
    private final Clock clock;
    private final FusoEmpresa fuso;
    private final HorarioAtendimento horarioAtendimento;
    private final int maxTentativas;

    public WhatsappConversaService(
            MensagemRecebidaRepository mensagemRecebidaRepository,
            MensagemSaidaRepository mensagemSaidaRepository,
            MensagemAtendimentoRepository mensagemAtendimentoRepository,
            PerguntaPendenteRepository perguntaPendenteRepository,
            MedicaoRepository medicaoRepository,
            InstalacaoRepository instalacaoRepository,
            OrcamentoRepository orcamentoRepository,
            WhatsappContatoService whatsappContatoService,
            ConversaWhatsappService conversaWhatsappService,
            PerguntaPendenteService perguntaPendenteService,
            WhatsappSaidaService whatsappSaidaService,
            FluxoAtendimentoService fluxoAtendimentoService,
            MedicaoService medicaoService,
            InstalacaoService instalacaoService,
            OrcamentoService orcamentoService,
            HistoricoService historicoService,
            PlatformTransactionManager transactionManager,
            Clock clock,
            @Value("${vidratx.whatsapp.pergunta.max-tentativas:3}") int maxTentativas) {

        this.mensagemRecebidaRepository = mensagemRecebidaRepository;
        this.mensagemSaidaRepository = mensagemSaidaRepository;
        this.mensagemAtendimentoRepository = mensagemAtendimentoRepository;
        this.perguntaPendenteRepository = perguntaPendenteRepository;
        this.medicaoRepository = medicaoRepository;
        this.instalacaoRepository = instalacaoRepository;
        this.orcamentoRepository = orcamentoRepository;
        this.whatsappContatoService = whatsappContatoService;
        this.conversaWhatsappService = conversaWhatsappService;
        this.perguntaPendenteService = perguntaPendenteService;
        this.whatsappSaidaService = whatsappSaidaService;
        this.fluxoAtendimentoService = fluxoAtendimentoService;
        this.medicaoService = medicaoService;
        this.instalacaoService = instalacaoService;
        this.orcamentoService = orcamentoService;
        this.historicoService = historicoService;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.clock = clock;
        this.fuso = new FusoEmpresa(clock);
        this.horarioAtendimento = new HorarioAtendimento(clock);
        this.maxTentativas = maxTentativas;
    }

    private record Chave(Long empresaId, String telefone, StatusMensagemRecebida status) {
    }

    private record Contexto(
            MensagemRecebida mensagem,
            AtendimentoWhatsapp atendimento,
            WhatsappContato contato,
            Interpretacao interpretacao,
            String texto,
            List<PerguntaPendente> ativas,
            List<PerguntaPendente> vistas,
            MensagemSaida ultimaSaida,
            PerguntaPendente perguntaDaUltimaSaida,
            boolean nova) {

        boolean humano() {
            return MODO_HUMANO.contains(atendimento.getStatus());
        }
    }

    public void processar(Long mensagemId) {

        Chave chave = lerChave(mensagemId);

        if (chave == null || chave.status() != StatusMensagemRecebida.RECEBIDA) {
            return;
        }

        transactionTemplate.executeWithoutResult(tx -> {

            WhatsappContato contato = whatsappContatoService.travarExistente(chave.empresaId(), chave.telefone());

            for (MensagemRecebida anterior : mensagemRecebidaRepository
                    .findAllByEmpresaIdAndTelefoneAndStatusAndIdLessThanAndTentativasLessThanOrderByIdAsc(
                            chave.empresaId(), chave.telefone(), StatusMensagemRecebida.RECEBIDA, mensagemId,
                            WhatsappInboxService.MAX_TENTATIVAS_PROCESSAMENTO)) {

                processarUma(anterior, contato);
            }

            mensagemRecebidaRepository.findById(mensagemId).ifPresent(m -> processarUma(m, contato));
        });
    }

    public void escalarPorFalha(Long mensagemId) {

        Chave chave = lerChave(mensagemId);

        if (chave == null) {
            return;
        }

        transactionTemplate.executeWithoutResult(tx -> {

            whatsappContatoService.travarExistente(chave.empresaId(), chave.telefone());

            MensagemRecebida mensagem = mensagemRecebidaRepository.findById(mensagemId).orElse(null);

            if (mensagem == null) {
                return;
            }

            AtendimentoWhatsapp atendimento = conversaWhatsappService
                    .obterConversaAberta(mensagem.getEmpresa(), mensagem.getTelefone(), null)
                    .atendimento();

            boolean jaRegistrada = mensagem.getWhatsappMensagemId() != null
                    && mensagemAtendimentoRepository.existsByAtendimentoIdAndWhatsappMensagemId(
                    atendimento.getId(), mensagem.getWhatsappMensagemId());

            if (!jaRegistrada) {
                conversaWhatsappService.registrarMensagemCliente(atendimento, mensagem);
            }

            conversaWhatsappService.escalar(atendimento,
                    "Mensagem do cliente não pôde ser processada automaticamente — responder manualmente");

            whatsappSaidaService.enfileirar(NovaMensagem.doBot(atendimento,
                    "Recebemos a sua mensagem. Um atendente vai te responder em breve.")
                    .comCategoria(CategoriaMensagemSaida.ESCALONAMENTO));
        });
    }

    private Chave lerChave(Long mensagemId) {

        return transactionTemplate.execute(tx -> mensagemRecebidaRepository.findById(mensagemId)
                .map(m -> new Chave(m.getEmpresa().getId(), m.getTelefone(), m.getStatus()))
                .orElse(null));
    }

    private void processarUma(MensagemRecebida mensagem, WhatsappContato contato) {

        if (mensagem.getStatus() != StatusMensagemRecebida.RECEBIDA) {
            return;
        }

        ConversaAberta conversa = conversaWhatsappService
                .obterConversaAberta(mensagem.getEmpresa(), mensagem.getTelefone(), null);

        AtendimentoWhatsapp atendimento = conversa.atendimento();

        MensagemAtendimento registrada = conversaWhatsappService.registrarMensagemCliente(atendimento, mensagem);

        boolean atrasada = mensagem.getEnviadaEm() != null
                && mensagemRecebidaRepository
                .findAllByEmpresaIdAndTelefoneAndStatusAndEnviadaEmAfter(
                        mensagem.getEmpresa().getId(), mensagem.getTelefone(), StatusMensagemRecebida.PROCESSADA,
                        mensagem.getEnviadaEm())
                .stream()
                .anyMatch(WhatsappConversaService::disseAlgo);

        if (!atrasada) {

            Contexto contexto = montarContexto(mensagem, atendimento, contato, conversa.nova());

            if (mensagem.ehReacao()) {
                tratarReacao(contexto, registrada);
            } else if (mensagem.alteraMensagemAnterior()) {
                tratarAlteracao(contexto, registrada);
            } else {
                switch (mensagem.getTipo()) {
                    case TEXTO -> tratarTexto(contexto);
                    case IMAGEM -> tratarImagem(contexto, registrada);
                    case NAO_SUPORTADA -> tratarNaoSuportada(contexto);
                }
            }
        }

        mensagem.setStatus(StatusMensagemRecebida.PROCESSADA);
        mensagem.setProcessadaEm(LocalDateTime.now(clock));
        mensagem.setErro(null);
        mensagemRecebidaRepository.save(mensagem);
    }

    private static boolean disseAlgo(MensagemRecebida posterior) {

        if (posterior.getTipo() == TipoMensagemRecebida.IMAGEM || posterior.ehReacao()) {
            return false;
        }

        if (posterior.alteraMensagemAnterior()) {
            return true;
        }

        if (posterior.getTipo() != TipoMensagemRecebida.TEXTO) {
            return true;
        }

        Interpretacao i = InterpretadorResposta.interpretar(posterior.getConteudo());

        return !(i.cortesia() || i.saudacao() || i.vazia());
    }

    private Contexto montarContexto(
            MensagemRecebida mensagem, AtendimentoWhatsapp atendimento, WhatsappContato contato, boolean nova) {

        String texto = mensagem.getTipo() == TipoMensagemRecebida.TEXTO && mensagem.getConteudo() != null
                ? mensagem.getConteudo().trim() : "";

        LocalDateTime momento = mensagem.momentoDoCliente();

        List<PerguntaPendente> ativas = perguntaPendenteService
                .ativas(mensagem.getEmpresa().getId(), mensagem.getTelefone());

        List<PerguntaPendente> vistas = ativas.stream().filter(p -> foiVista(p, momento)).toList();

        MensagemSaida ultimaSaida = mensagemSaidaRepository
                .findFirstByAtendimentoIdOrderByIdDesc(atendimento.getId())
                .orElse(null);

        MensagemSaida ultimaVista = mensagemSaidaRepository
                .findFirstByAtendimentoIdAndEnviadaEmLessThanOrderByEnviadaEmDescIdDesc(atendimento.getId(), momento)
                .orElse(null);

        PerguntaPendente perguntaDaUltimaSaida = ultimaVista != null && ultimaVista.getPergunta() != null
                ? vistas.stream().filter(p -> p.getId().equals(ultimaVista.getPergunta().getId())).findFirst().orElse(null)
                : null;

        return new Contexto(mensagem, atendimento, contato, InterpretadorResposta.interpretar(texto), texto,
                ativas, vistas, ultimaSaida, perguntaDaUltimaSaida, nova);
    }

    private boolean foiVista(PerguntaPendente pergunta, LocalDateTime momento) {

        Optional<MensagemSaida> primeiroEnvio = mensagemSaidaRepository
                .findFirstByPerguntaIdAndEnviadaEmIsNotNullOrderByEnviadaEmAsc(pergunta.getId());

        if (primeiroEnvio.isPresent()) {
            return primeiroEnvio.get().getEnviadaEm().isBefore(momento);
        }

        if (mensagemSaidaRepository.existsByPerguntaId(pergunta.getId())) {
            return false;
        }

        return pergunta.getCriadaEm().isBefore(momento);
    }

    private void tratarTexto(Contexto ctx) {

        Interpretacao i = ctx.interpretacao();
        WhatsappContato contato = ctx.contato();

        if (ctx.atendimento().getStatus() == StatusAtendimento.AGUARDANDO_ATENDENTE
                && "0".equals(i.textoNormalizado())) {
            sairDaFila(ctx);
            return;
        }

        if (ctx.humano()) {
            tratarEmAtendimentoHumano(ctx);
            return;
        }

        if (i.pedeAtendente()) {
            contato.limparEscolha();
            escalarComResposta(ctx, "Cliente pediu atendimento humano",
                    "Tudo bem, vou te transferir para um atendente. Só um momento!");
            return;
        }

        if ("0".equals(i.textoNormalizado()) && !ctx.vistas().isEmpty()
                && !ETAPAS_DE_CAPTACAO.contains(ctx.atendimento().getEtapaFluxo())) {

            contato.limparEscolha();
            enviar(ctx, fluxoAtendimentoService.irParaMenu(ctx.atendimento(), "Ok, voltando ao menu principal."), null);
            return;
        }

        if (Boolean.TRUE.equals(contato.getEscolhaPendente())) {

            List<PerguntaPendente> exibida = listaExibida(ctx);

            if (exibida.stream().anyMatch(Objects::nonNull)) {
                tratarEscolha(ctx, exibida);
                return;
            }

            contato.limparEscolha();
        }

        PerguntaPendente foco = perguntaEmFoco(ctx);

        if (foco == null && ctx.vistas().size() >= 2
                && ctx.vistas().stream().anyMatch(p -> emContexto(p, ctx))) {

            PerguntaPendente escolhida = desambiguar(i, ctx.vistas());

            if (escolhida == null) {

                List<PerguntaPendente> apresentaveis = apresentaveis(ctx.vistas());

                if (apresentaveis.size() >= 2) {
                    enviarLista(ctx, apresentaveis, "");
                    return;
                }

                escolhida = apresentaveis.isEmpty() ? null : apresentaveis.get(0);
            }

            foco = escolhida;
        }

        if (foco != null && tratarPergunta(foco, ctx, false)) {
            continuarPendencias(ctx, foco);
            return;
        }

        if (tratarPedidoDeRemarcarOuCancelar(ctx)) {
            return;
        }

        if (tratarRespostaSemPergunta(ctx)) {
            return;
        }

        responderPeloFluxo(ctx);
    }

    private PerguntaPendente perguntaEmFoco(Contexto ctx) {

        WhatsappContato contato = ctx.contato();

        if (contato.getPerguntaFocoId() != null) {

            Optional<PerguntaPendente> foco = ctx.vistas().stream()
                    .filter(p -> p.getId().equals(contato.getPerguntaFocoId()))
                    .findFirst();

            if (foco.isPresent()) {
                return foco.get();
            }

            contato.limparEscolha();
        }

        if (ctx.vistas().size() == 1 && emContexto(ctx.vistas().get(0), ctx)) {
            return ctx.vistas().get(0);
        }

        return null;
    }

    private boolean emContexto(PerguntaPendente pergunta, Contexto ctx) {

        return (ctx.perguntaDaUltimaSaida() != null && ctx.perguntaDaUltimaSaida().getId().equals(pergunta.getId()))
                || ctx.nova()
                || (pergunta.getTipo().ehSugestaoDeData() && ctx.interpretacao().contemData());
    }

    private void tratarEmAtendimentoHumano(Contexto ctx) {

        PerguntaPendente pergunta = ctx.perguntaDaUltimaSaida();

        if (pergunta != null && ctx.vistas().size() == 1 && decisiva(pergunta, ctx.interpretacao())) {
            tratarPergunta(pergunta, ctx, true);
        }

    }

    private void sairDaFila(Contexto ctx) {

        AtendimentoWhatsapp atendimento = ctx.atendimento();

        atendimento.setStatus(StatusAtendimento.EM_FLUXO_BOT);
        atendimento.setAtendente(null);
        atendimento.setTentativasErro(0);

        if (atendimento.getEtapaFluxo() != EtapaFluxo.COLETA_NOME) {
            atendimento.setEtapaFluxo(EtapaFluxo.MENU);
        }

        historicoService.registrarEventoAtendimento(
                atendimento, TipoEventoHistorico.ATENDIMENTO_DEVOLVIDO_BOT,
                "Cliente saiu da fila de atendimento (respondeu 0) e voltou ao atendimento automático", null
        );

        ctx.contato().limparEscolha();

        String saudacao = "Ok, você voltou ao atendimento automático.";
        List<PerguntaPendente> apresentaveis = apresentaveis(ctx.vistas());

        if (apresentaveis.size() >= 2) {
            enviarLista(ctx, apresentaveis, saudacao + "\n\n");
        } else if (apresentaveis.size() == 1) {
            enviar(ctx, saudacao + "\n\n" + apresentaveis.get(0).getTextoPergunta(), apresentaveis.get(0));
        } else {
            enviar(ctx, fluxoAtendimentoService.irParaMenu(atendimento, saudacao), null);
        }
    }

    private boolean decisiva(PerguntaPendente pergunta, Interpretacao i) {

        if (pergunta.getTipo().ehConfirmacaoDeData()) {
            return i.opcao(1) || i.opcao(2) || i.confirmacao() || i.recusaSemData() || i.contemData();
        }

        if (pergunta.getTipo().ehSugestaoDeData()) {
            return i.contemData();
        }

        return i.opcao(1) || i.opcao(2) || i.opcao(4) || i.confirmacao() || i.recusaSemData() || i.pedeAlteracao();
    }

    private List<PerguntaPendente> listaExibida(Contexto ctx) {

        List<Long> ids = ctx.contato().idsDaEscolha();

        if (ids.isEmpty()) {

            List<PerguntaPendente> apresentaveis = listaDeEscolha(apresentaveis(ctx.vistas()));

            return apresentaveis.size() >= 2 ? apresentaveis : List.of();
        }

        return ids.stream()
                .map(id -> ctx.ativas().stream().filter(p -> p.getId().equals(id)).findFirst().orElse(null))
                .toList();
    }

    private void tratarEscolha(Contexto ctx, List<PerguntaPendente> exibida) {

        Interpretacao i = ctx.interpretacao();
        WhatsappContato contato = ctx.contato();
        List<PerguntaPendente> abertas = exibida.stream().filter(Objects::nonNull).toList();

        PerguntaPendente escolhida = null;

        if (i.opcao() != null && i.opcao() >= 1 && i.opcao() <= exibida.size()) {

            escolhida = exibida.get(i.opcao() - 1);

            if (escolhida == null) {
                apresentarRestantes(ctx, abertas, "Esse assunto já foi resolvido. ");
                return;
            }
        }

        if (escolhida == null) {
            escolhida = desambiguar(i, abertas);
        }

        if (escolhida != null) {

            contato.limparEscolha();
            contato.setPerguntaFocoId(escolhida.getId());

            enviar(ctx, escolhida.getTextoPergunta(), escolhida);
            return;
        }

        contato.setTentativasEscolha(contato.getTentativasEscolha() + 1);

        if (contato.getTentativasEscolha() >= maxTentativas) {

            contato.limparEscolha();
            escalarComResposta(ctx, "Cliente não conseguiu escolher entre as pendências abertas",
                    "Não consegui entender qual assunto você quer tratar. Vou te transferir para um atendente.");
            return;
        }

        apresentarRestantes(ctx, abertas, "Não entendi. ");
    }

    private void apresentarRestantes(Contexto ctx, List<PerguntaPendente> abertas, String prefixo) {

        if (abertas.size() == 1) {

            PerguntaPendente unica = abertas.get(0);

            ctx.contato().limparEscolha();
            ctx.contato().setPerguntaFocoId(unica.getId());

            enviar(ctx, prefixo + "Está em aberto: " + unica.getResumo() + ".\n\n" + unica.getTextoPergunta(), unica);
            return;
        }

        int tentativas = ctx.contato().getTentativasEscolha();

        enviarLista(ctx, abertas, prefixo);

        ctx.contato().setTentativasEscolha(tentativas);
    }

    private void enviarLista(Contexto ctx, List<PerguntaPendente> pendencias, String prefixo) {

        List<PerguntaPendente> lista = listaDeEscolha(pendencias);
        StringBuilder texto = new StringBuilder(prefixo)
                .append("Você tem ").append(lista.size()).append(" assuntos em aberto com a gente:\n");

        for (int indice = 0; indice < lista.size(); indice++) {
            texto.append('\n').append(indice + 1).append(" - ").append(lista.get(indice).getResumo());
        }

        texto.append("\n\nResponda com o número do assunto que você quer tratar agora (ou 9 para falar com um atendente).");

        ctx.contato().limparEscolha();
        ctx.contato().definirEscolha(lista.stream().map(PerguntaPendente::getId).toList());

        enviar(ctx, texto.toString(), null);
    }

    private List<PerguntaPendente> listaDeEscolha(List<PerguntaPendente> pendencias) {
        return pendencias.size() > MAXIMO_ITENS_LISTA ? pendencias.subList(0, MAXIMO_ITENS_LISTA) : pendencias;
    }

    private List<PerguntaPendente> apresentaveis(List<PerguntaPendente> pendencias) {
        return pendencias.stream().filter(p -> !ehSilenciosa(p)).toList();
    }

    private PerguntaPendente desambiguar(Interpretacao i, List<PerguntaPendente> perguntas) {

        String texto = " " + i.textoNormalizado() + " ";

        List<PerguntaPendente> porNumero = perguntas.stream()
                .filter(p -> p.getTipo().getReferencia() == TipoReferenciaPergunta.ORCAMENTO)
                .filter(p -> texto.contains(" " + p.getReferenciaId() + " "))
                .toList();

        if (porNumero.size() == 1) {
            return porNumero.get(0);
        }

        boolean medicao = texto.contains("medicao") || texto.contains(" medir") || texto.contains("visita");
        boolean instalacao = texto.contains("instalacao") || texto.contains("instalar") || texto.contains("montagem");
        boolean orcamento = texto.contains("orcamento");

        if ((medicao ? 1 : 0) + (instalacao ? 1 : 0) + (orcamento ? 1 : 0) != 1) {
            return null;
        }

        List<PerguntaPendente> candidatas = perguntas.stream().filter(p -> switch (p.getTipo().getReferencia()) {
            case MEDICAO -> medicao;
            case INSTALACAO -> instalacao;
            case ORCAMENTO -> orcamento;
        }).toList();

        return candidatas.size() == 1 ? candidatas.get(0) : null;
    }

    private void continuarPendencias(Contexto ctx, PerguntaPendente respondida) {

        if (ctx.humano() || ctx.atendimento().getStatus() != StatusAtendimento.EM_FLUXO_BOT) {
            return;
        }

        if (respondida.ativa()) {
            return;
        }

        ctx.contato().limparEscolha();

        LocalDateTime agora = LocalDateTime.now(clock);

        List<PerguntaPendente> restantes = perguntaPendenteService
                .ativas(ctx.mensagem().getEmpresa().getId(), ctx.mensagem().getTelefone())
                .stream()
                .filter(p -> !(p.getTipo().getReferencia() == respondida.getTipo().getReferencia()
                        && p.getReferenciaId().equals(respondida.getReferenciaId())))
                .filter(p -> !ehSilenciosa(p))
                .filter(p -> foiVista(p, agora))
                .toList();

        if (restantes.size() == 1) {

            PerguntaPendente proxima = restantes.get(0);

            enviar(ctx, "Você ainda tem outro assunto em aberto com a gente: " + proxima.getResumo() + ".\n\n"
                    + proxima.getTextoPergunta(), proxima);

        } else if (restantes.size() > 1) {

            enviarLista(ctx, restantes, "");
        }
    }

    private boolean ehSilenciosa(PerguntaPendente pergunta) {

        if (!pergunta.getTipo().ehSugestaoDeData()) {
            return false;
        }

        return carregarAgendamento(pergunta)
                .map(a -> a.getStatus() == StatusAgendamento.CONTRAPROPOSTA_CLIENTE)
                .orElse(true);
    }

    private boolean tratarPergunta(PerguntaPendente pergunta, Contexto ctx, boolean humano) {

        if (pergunta.getTipo().getReferencia() == TipoReferenciaPergunta.ORCAMENTO) {

            Orcamento orcamento = orcamentoRepository.findById(pergunta.getReferenciaId()).orElse(null);

            if (orcamento == null || orcamento.getStatus() != StatusOrcamento.ENVIADO) {
                perguntaPendenteService.encerrar(pergunta, StatusPergunta.SUBSTITUIDA);
                return false;
            }

            return tratarAprovacao(orcamento, pergunta, ctx, humano);
        }

        Agendamento agendamento = carregarAgendamento(pergunta).orElse(null);

        if (agendamento == null) {
            perguntaPendenteService.encerrar(pergunta, StatusPergunta.CANCELADA);
            return false;
        }

        if (pergunta.getTipo().ehConfirmacaoDeData()) {

            if (agendamento.getStatus() != StatusAgendamento.PROPOSTA_ENVIADA) {
                perguntaPendenteService.encerrar(pergunta, StatusPergunta.SUBSTITUIDA);
                return false;
            }

            return tratarConfirmacaoData(agendamento, pergunta, ctx, humano);
        }

        if (!AGUARDANDO_DATA_DO_CLIENTE.contains(agendamento.getStatus())) {
            perguntaPendenteService.encerrar(pergunta, StatusPergunta.SUBSTITUIDA);
            return false;
        }

        return tratarSugestaoData(agendamento, pergunta, ctx, humano);
    }

    private boolean tratarConfirmacaoData(Agendamento agendamento, PerguntaPendente pergunta, Contexto ctx, boolean humano) {

        Interpretacao i = ctx.interpretacao();
        String data = NegociacaoAgendamentoService.formatar(agendamento.getDataAgendada());
        String opcoes = "1 - Confirmar " + data + "\n2 - Sugerir outra data\n3 - Falar com um atendente";

        if (i.opcao(3)) {

            if (!humano) {
                escalarComResposta(ctx, "Cliente pediu atendente durante a proposta de " + rotulo(agendamento),
                        "Tudo bem, vou te transferir para um atendente. A proposta de " + data + " continua valendo.");
            }

            return true;
        }

        boolean citaDataProposta = citaDataProposta(agendamento, ctx);

        if (i.opcao(1) || (i.confirmacao() && !i.contemData())
                || (citaDataProposta && !i.negacao() && !i.pergunta() && !i.incerteza() && !i.pedeRemarcar())) {
            confirmar(agendamento, pergunta, ctx.atendimento());
            return true;
        }

        if (i.opcao(2) || (i.pedeRemarcar() && !i.contemData()) || (citaDataProposta && i.negacao() && !i.pergunta())) {
            recusar(agendamento, pergunta, ctx.atendimento());
            return true;
        }

        if (i.contemData() && !i.confirmacao()) {
            sugerir(agendamento, pergunta, ctx.texto(), ctx.atendimento());
            return true;
        }

        if (i.recusaSemData()) {
            recusar(agendamento, pergunta, ctx.atendimento());
            return true;
        }

        if (humano) {
            return true;
        }

        if (i.incerteza()) {
            enviar(ctx, "Sem pressa! Quando decidir, é só responder:\n" + opcoes, pergunta);
            return true;
        }

        if (i.cortesia() || i.saudacao() || i.vazia()) {
            enviar(ctx, "Ficamos no aguardo da sua resposta sobre " + nome(agendamento) + ":\n" + opcoes, pergunta);
            return true;
        }

        if (i.pergunta() || i.pedeAlteracao()) {

            conversaWhatsappService.escalar(ctx.atendimento(),
                    "Cliente fez uma pergunta durante a proposta de " + rotulo(agendamento) + ": \"" + ctx.texto() + "\"");

            enviar(ctx, "Vou passar a sua pergunta para um atendente. A proposta continua valendo: responda 1 para "
                    + "confirmar " + data + " ou 2 para sugerir outra data.", pergunta);
            return true;
        }

        naoEntendi(ctx, pergunta, "Não entendi. Responda com o número:\n" + opcoes,
                "a proposta de " + data + " continua valendo");
        return true;
    }

    private boolean citaDataProposta(Agendamento agendamento, Contexto ctx) {

        if (!ctx.interpretacao().contemData() || agendamento.getDataAgendada() == null) {
            return false;
        }

        return ExtratorData.extrair(ctx.texto(), fuso.agora(ctx.mensagem().getEmpresa()))
                .map(data -> data.equals(agendamento.getDataAgendada()))
                .orElse(false);
    }

    private boolean tratarSugestaoData(Agendamento agendamento, PerguntaPendente pergunta, Contexto ctx, boolean humano) {

        Interpretacao i = ctx.interpretacao();
        boolean silenciosa = agendamento.getStatus() == StatusAgendamento.CONTRAPROPOSTA_CLIENTE;

        if (i.contemData()) {
            sugerir(agendamento, pergunta, ctx.texto(), ctx.atendimento());
            return true;
        }

        if (silenciosa || humano) {
            return false;
        }

        String pedido = "Me diga o dia e o horário que ficam melhores para você (ex.: sexta às 14h). "
                + "Se preferir falar com um atendente, responda 9.";

        if (i.incerteza() || i.negacao()) {
            enviar(ctx, "Tudo bem! Quando souber, me mande o dia e o horário que ficam melhores "
                    + "(ex.: sexta às 14h). Se preferir falar com um atendente, responda 9.", pergunta);
            return true;
        }

        if (i.pergunta()) {

            conversaWhatsappService.escalar(ctx.atendimento(),
                    "Cliente fez uma pergunta ao escolher nova data de " + rotulo(agendamento) + ": \"" + ctx.texto() + "\"");

            enviar(ctx, "Vou passar a sua pergunta para um atendente. Quando quiser, me mande o dia e o horário "
                    + "que ficam melhores (ex.: sexta às 14h).", pergunta);
            return true;
        }

        if (!i.confirmacao() && !i.cortesia() && !i.saudacao()
                && VOCABULARIO_DE_QUANDO.matcher(i.textoNormalizado()).find()) {
            sugerir(agendamento, pergunta, ctx.texto(), ctx.atendimento());
            return true;
        }

        naoEntendi(ctx, pergunta, pedido, "é só mandar o dia e o horário quando puder");
        return true;
    }

    private boolean tratarAprovacao(Orcamento orcamento, PerguntaPendente pergunta, Contexto ctx, boolean humano) {

        Interpretacao i = semNumeroDoProprioOrcamento(ctx.interpretacao(), ctx.texto(), orcamento);
        String validade = orcamento.getValidoAte() != null ? orcamento.getValidoAte().format(FORMATO_DATA) : null;
        String opcoes = "1 - Aprovar\n2 - Pedir alteração\n3 - Falar com um atendente\n4 - Não tenho interesse";

        if (i.opcao(3)) {

            if (!humano) {
                escalarComResposta(ctx, "Cliente pediu atendente sobre o orçamento nº " + orcamento.getId(),
                        "Tudo bem, vou te transferir para um atendente."
                                + (validade != null ? " O orçamento continua valendo até " + validade + "." : ""));
            }

            return true;
        }

        if (i.opcao(1) || i.confirmacao()) {
            orcamentoService.aprovarPeloCliente(orcamento, pergunta, ctx.atendimento());
            return true;
        }

        if (i.opcao(2) || i.pedeAlteracao()) {
            orcamentoService.registrarAlteracaoSolicitada(orcamento, pergunta, ctx.texto(), ctx.atendimento());
            return true;
        }

        boolean adiamento = i.adiamento() && !i.opcao(4);

        if (!adiamento && (i.opcao(4) || i.recusaSemData())) {
            orcamentoService.recusarPeloCliente(orcamento, pergunta, ctx.texto(), ctx.atendimento());
            return true;
        }

        if (humano) {
            return true;
        }

        if (i.incerteza() || adiamento) {
            enviar(ctx, "Sem pressa!" + (validade != null ? " O orçamento vale até " + validade + "." : "")
                    + " Quando decidir, é só responder:\n" + opcoes, pergunta);
            return true;
        }

        if (i.cortesia() || i.saudacao() || i.vazia()) {
            enviar(ctx, "Ficamos no aguardo da sua resposta sobre o orçamento nº " + orcamento.getId() + ":\n" + opcoes,
                    pergunta);
            return true;
        }

        if (FluxoAtendimentoService.consultaOrcamento(i.textoNormalizado())) {

            enviar(ctx, "Seu orçamento nº " + orcamento.getId() + " está aguardando a sua aprovação."
                    + (orcamento.getValorTotal() != null
                    ? " Valor: " + NumberFormat.getCurrencyInstance(PT_BR).format(orcamento.getValorTotal()) + "." : "")
                    + (validade != null ? " Válido até " + validade + "." : "")
                    + "\n\nResponda:\n" + opcoes, pergunta);
            return true;
        }

        if (i.pergunta()) {

            conversaWhatsappService.escalar(ctx.atendimento(),
                    "Cliente fez uma pergunta sobre o orçamento nº " + orcamento.getId() + ": \"" + ctx.texto() + "\"");

            enviar(ctx, "Vou passar a sua pergunta para um atendente."
                    + (validade != null ? " O orçamento continua valendo até " + validade + ":" : "")
                    + " quando quiser, responda 1 para aprovar.", pergunta);
            return true;
        }

        naoEntendi(ctx, pergunta, "Não entendi. Responda com o número:\n" + opcoes,
                "o orçamento continua valendo");
        return true;
    }

    private static Interpretacao semNumeroDoProprioOrcamento(Interpretacao i, String texto, Orcamento orcamento) {

        String numero = String.valueOf(orcamento.getId());

        if (!(" " + i.textoNormalizado() + " ").contains(" " + numero + " ")) {
            return i;
        }

        return InterpretadorResposta.interpretar(texto.replaceAll("\\b" + numero + "\\b", " "));
    }

    private void naoEntendi(Contexto ctx, PerguntaPendente pergunta, String texto, String continuaValendo) {

        perguntaPendenteService.registrarTentativa(pergunta);

        if (pergunta.getTentativas() >= maxTentativas) {

            pergunta.setTentativas(0);

            escalarComResposta(ctx, "Cliente não conseguiu responder à pergunta automática (" + pergunta.getResumo() + ")",
                    "Não consegui entender a sua resposta. Vou te transferir para um atendente — " + continuaValendo + ".");
            return;
        }

        enviar(ctx, texto, pergunta);
    }

    private boolean tratarPedidoDeRemarcarOuCancelar(Contexto ctx) {

        Interpretacao i = ctx.interpretacao();
        boolean cancelar = (" " + i.textoNormalizado() + " ").matches(".* (cancela|cancelar|cancelamento|desmarcar) .*");

        if ((!i.pedeRemarcar() && !cancelar) || ctx.atendimento().getCliente() == null
                || ETAPAS_DE_CAPTACAO.contains(ctx.atendimento().getEtapaFluxo())) {
            return false;
        }

        Long clienteId = ctx.atendimento().getCliente().getId();

        List<Agendamento> confirmados = new ArrayList<>();
        confirmados.addAll(medicaoRepository.findAllByOrcamentoClienteIdAndStatus(clienteId, StatusAgendamento.AGENDADA));
        confirmados.addAll(instalacaoRepository.findAllByOrdemServicoOrcamentoClienteIdAndStatus(clienteId, StatusAgendamento.AGENDADA));

        if (confirmados.isEmpty()) {
            return false;
        }

        if (cancelar && !i.pedeRemarcar()) {

            Agendamento alvoCancelamento = confirmados.size() == 1
                    ? confirmados.get(0) : escolherAgendamento(i, confirmados);

            if (alvoCancelamento instanceof Medicao medicao) {
                medicaoService.registrarPedidoDeCancelamento(medicao, ctx.texto());
            } else if (alvoCancelamento instanceof Instalacao instalacao) {
                instalacaoService.registrarPedidoDeCancelamento(instalacao, ctx.texto());
            }

            escalarComResposta(ctx, "Cliente pediu para cancelar um agendamento confirmado: \"" + ctx.texto() + "\"",
                    "Certo! Vou passar para um atendente cuidar do cancelamento com você.");
            return true;
        }

        Agendamento alvo = confirmados.size() == 1 ? confirmados.get(0) : escolherAgendamento(i, confirmados);

        if (alvo == null) {
            escalarComResposta(ctx, "Cliente pediu para remarcar, mas tem mais de um agendamento confirmado: \""
                            + ctx.texto() + "\"",
                    "Certo! Vou passar para um atendente remarcar com você.");
            return true;
        }

        if (alvo instanceof Medicao medicao) {
            medicaoService.solicitarReagendamentoPeloCliente(medicao, ctx.texto(), i.contemData(), ctx.atendimento());
        } else if (alvo instanceof Instalacao instalacao) {
            instalacaoService.solicitarReagendamentoPeloCliente(instalacao, ctx.texto(), i.contemData(), ctx.atendimento());
        }

        return true;
    }

    private Agendamento escolherAgendamento(Interpretacao i, List<Agendamento> agendamentos) {

        String texto = i.textoNormalizado();
        boolean medicao = texto.contains("medicao") || texto.contains("visita");
        boolean instalacao = texto.contains("instalacao") || texto.contains("instalar");

        if (medicao == instalacao) {
            return null;
        }

        TipoAgendamento tipo = medicao ? TipoAgendamento.MEDICAO : TipoAgendamento.INSTALACAO;
        List<Agendamento> doTipo = agendamentos.stream().filter(a -> a.getTipoAgendamento() == tipo).toList();

        return doTipo.size() == 1 ? doTipo.get(0) : null;
    }

    private boolean tratarRespostaSemPergunta(Contexto ctx) {

        Interpretacao i = ctx.interpretacao();
        EtapaFluxo etapa = ctx.atendimento().getEtapaFluxo();
        boolean foraDoFluxo = etapa == null || ctx.nova();

        boolean decisiva = i.confirmacao() || i.negacaoClara()
                || (foraDoFluxo && i.opcao() != null && i.opcao() >= 1 && i.opcao() <= 4);

        if (!decisiva || ETAPAS_DE_CAPTACAO.contains(etapa)) {
            return false;
        }

        if (ctx.ativas().size() > ctx.vistas().size()) {
            enviar(ctx, "Recebemos a sua resposta, mas ela cruzou com uma mensagem nova nossa. "
                    + "Confira a mensagem mais recente e responda a ela, por favor.", null);
            return true;
        }

        Optional<PerguntaPendente> recente = perguntaPendenteRepository
                .findFirstByEmpresaIdAndTelefoneAndStatusNotAndAtualizadoEmAfterOrderByAtualizadoEmDescIdDesc(
                        ctx.mensagem().getEmpresa().getId(), ctx.mensagem().getTelefone(),
                        StatusPergunta.ATIVA, LocalDateTime.now(clock).minusDays(30));

        if (recente.isEmpty()) {
            return false;
        }

        StatusAtendimento statusAntes = ctx.atendimento().getStatus();
        String descricao = descreverSituacao(recente.get(), ctx.atendimento());

        if (descricao == null) {
            return false;
        }

        whatsappSaidaService.enfileirar(NovaMensagem.doBot(ctx.atendimento(), descricao)
                .comCategoria(statusAntes != ctx.atendimento().getStatus()
                        ? CategoriaMensagemSaida.ESCALONAMENTO : CategoriaMensagemSaida.BOT));
        return true;
    }

    private String descreverSituacao(PerguntaPendente pergunta, AtendimentoWhatsapp atendimento) {

        if (pergunta.getTipo().getReferencia() == TipoReferenciaPergunta.ORCAMENTO) {

            return orcamentoRepository.findById(pergunta.getReferenciaId())
                    .map(o -> orcamentoService.descreverParaCliente(o, atendimento))
                    .orElse(null);
        }

        Agendamento agendamento = carregarAgendamento(pergunta).orElse(null);

        if (agendamento == null) {
            return null;
        }

        String nome = nome(agendamento);
        String data = NegociacaoAgendamentoService.formatar(agendamento.getDataAgendada());

        return switch (agendamento.getStatus()) {
            case AGENDADA -> "Já está confirmado: " + nome + " ficou para " + data + ". Se precisar remarcar, é só avisar.";
            case CANCELADA -> capitalizar(nome) + " foi cancelada. Se quiser marcar uma nova data, responda 9 para "
                    + "falar com um atendente.";
            case RECUSADA_CLIENTE, CONTRAPROPOSTA_CLIENTE, REAGENDAMENTO_NECESSARIO ->
                    "Nossa equipe vai te propor uma nova data para " + nome + " em breve.";
            case REALIZADA -> capitalizar(nome) + " já foi realizada. Obrigado!";
            case PROPOSTA_ENVIADA -> null;
        };
    }

    private void responderPeloFluxo(Contexto ctx) {

        AtendimentoWhatsapp atendimento = ctx.atendimento();
        Interpretacao i = ctx.interpretacao();

        if (i.cortesia() && ctx.ultimaSaida() != null && !ctx.nova()
                && !ETAPAS_DE_CAPTACAO.contains(atendimento.getEtapaFluxo())) {

            if (i.textoNormalizado().matches(".*\\b(obrigad[oa]|brigad[oa]|valeu|agradeco|grat[oa])\\b.*")) {
                enviar(ctx, "Por nada! Se precisar de alguma coisa, é só chamar.", null);
            }

            return;
        }

        StatusAtendimento statusAntes = atendimento.getStatus();
        boolean tinhaSolicitacao = atendimento.getSolicitacaoOrcamento() != null;

        RespostaFluxo resposta = ctx.nova() || atendimento.getEtapaFluxo() == null
                ? new RespostaFluxo(fluxoAtendimentoService.iniciarConversa(atendimento, ctx.texto()), null)
                : fluxoAtendimentoService.responder(atendimento, ctx.texto());

        if (statusAntes != StatusAtendimento.AGUARDANDO_ATENDENTE
                && atendimento.getStatus() == StatusAtendimento.AGUARDANDO_ATENDENTE) {

            historicoService.registrarEventoAtendimento(
                    atendimento, TipoEventoHistorico.ATENDIMENTO_ESCALADO_ATENDENTE,
                    !tinhaSolicitacao && atendimento.getSolicitacaoOrcamento() != null
                            ? "Solicitação de orçamento recebida pelo bot — aguardando atendente"
                            : "Conversa escalada para atendimento humano pelo bot",
                    null
            );
        }

        if (resposta.texto() != null && !resposta.escolher().isEmpty()) {
            enviarLista(ctx, resposta.escolher(), resposta.texto() + "\n\n");
            return;
        }

        if (resposta.texto() != null) {

            boolean escalou = statusAntes != atendimento.getStatus()
                    && atendimento.getStatus() == StatusAtendimento.AGUARDANDO_ATENDENTE;

            whatsappSaidaService.enfileirar(NovaMensagem.doBot(atendimento, resposta.texto())
                    .comPergunta(resposta.pergunta())
                    .comCategoria(escalou ? CategoriaMensagemSaida.ESCALONAMENTO : CategoriaMensagemSaida.BOT));
        }
    }

    private void tratarReacao(Contexto ctx, MensagemAtendimento registrada) {

        MensagemRecebida mensagem = ctx.mensagem();
        MensagemSaida alvo = mensagemSaidaRepository
                .findFirstByEmpresaIdAndWhatsappMensagemId(mensagem.getEmpresa().getId(), mensagem.getReacaoAMensagemId())
                .orElse(null);

        registrada.setConteudo("Reagiu com " + mensagem.getConteudo()
                + (alvo != null ? " à mensagem \"" + trecho(alvo.getConteudo()) + "\"" : ""));

        PerguntaPendente pergunta = alvo != null ? alvo.getPergunta() : null;
        Interpretacao i = ctx.interpretacao();

        if (pergunta == null || !(i.confirmacao() || i.negacaoClara())) {
            return;
        }

        if (!pergunta.ativa()) {

            if (ctx.humano()) {
                return;
            }

            String situacao = descreverSituacao(pergunta, ctx.atendimento());

            enviar(ctx, situacao != null ? situacao
                    : "Essa mensagem foi atualizada pela nossa equipe. Confira a mais recente, por favor.", null);
            return;
        }

        ctx.contato().limparEscolha();

        if (tratarPergunta(pergunta, ctx, ctx.humano())) {
            continuarPendencias(ctx, pergunta);
        }
    }

    private void tratarAlteracao(Contexto ctx, MensagemAtendimento registrada) {

        MensagemRecebida mensagem = ctx.mensagem();
        boolean apagada = mensagem.getApagaMensagemId() != null;
        String idOriginal = apagada ? mensagem.getApagaMensagemId() : mensagem.getEditaMensagemId();

        MensagemRecebida original = mensagemRecebidaRepository
                .findByEmpresaIdAndWhatsappMensagemId(mensagem.getEmpresa().getId(), idOriginal)
                .orElse(null);

        String textoOriginal = original != null && original.getConteudo() != null ? original.getConteudo() : null;

        registrada.setConteudo(apagada
                ? "Apagou a mensagem" + (textoOriginal != null ? " \"" + trecho(textoOriginal) + "\"" : "")
                : "Editou a mensagem" + (textoOriginal != null ? " \"" + trecho(textoOriginal) + "\"" : "")
                + " para: \"" + ctx.texto() + "\"");

        boolean originalDisseAlgo = original != null
                && original.getStatus() == StatusMensagemRecebida.PROCESSADA
                && !original.ehReacao()
                && disseAlgo(original);

        if (!originalDisseAlgo) {

            if (!apagada) {
                tratarTexto(ctx);
            }

            return;
        }

        if (!apagada && mesmoSentido(InterpretadorResposta.interpretar(textoOriginal), ctx.interpretacao())) {
            return;
        }

        String descricao = apagada
                ? "Cliente apagou a mensagem \"" + trecho(textoOriginal) + "\", que já tinha sido tratada — confirme com ele"
                : "Cliente editou a mensagem \"" + trecho(textoOriginal) + "\" para \"" + ctx.texto()
                + "\" depois que ela já tinha sido tratada — confirme com ele";

        if (ctx.humano()) {
            conversaWhatsappService.escalar(ctx.atendimento(), descricao);
            return;
        }

        escalarComResposta(ctx, descricao, apagada
                ? "Vi que você apagou uma mensagem. Um atendente vai conferir com você o que vale."
                : "Vi que você editou a sua mensagem. Um atendente vai conferir com você o que vale.");
    }

    private static boolean mesmoSentido(Interpretacao a, Interpretacao b) {

        if (a.contemData() || b.contemData()) {
            return a.textoNormalizado().equals(b.textoNormalizado());
        }

        return Objects.equals(a.opcao(), b.opcao())
                && a.confirmacao() == b.confirmacao()
                && a.negacao() == b.negacao()
                && a.negacaoClara() == b.negacaoClara()
                && a.pedeAtendente() == b.pedeAtendente()
                && a.pedeRemarcar() == b.pedeRemarcar()
                && a.pedeAlteracao() == b.pedeAlteracao()
                && a.incerteza() == b.incerteza()
                && a.adiamento() == b.adiamento()
                && a.pergunta() == b.pergunta();
    }

    private static String trecho(String texto) {

        if (texto == null) {
            return "";
        }

        String limpo = texto.replaceAll("\\s+", " ").trim();

        return limpo.length() > 80 ? limpo.substring(0, 80) + "…" : limpo;
    }

    private void tratarImagem(Contexto ctx, MensagemAtendimento registrada) {

        if (ctx.humano()) {
            return;
        }

        if ((ctx.nova() || ctx.atendimento().getEtapaFluxo() == null) && ctx.vistas().isEmpty()) {
            enviar(ctx, fluxoAtendimentoService.iniciarConversa(ctx.atendimento()), null);
            return;
        }

        Optional<MensagemAtendimento> anterior = mensagemAtendimentoRepository
                .findFirstByAtendimentoIdAndIdLessThanOrderByIdDesc(ctx.atendimento().getId(), registrada.getId());

        if (anterior.isPresent()
                && anterior.get().getRemetente() == RemetenteMensagem.CLIENTE
                && anterior.get().getTipo() == TipoMensagem.IMAGEM) {
            return;
        }

        String resposta = fluxoAtendimentoService.aoReceberImagem(ctx.atendimento());

        if (resposta != null) {
            enviar(ctx, resposta, ctx.perguntaDaUltimaSaida());
        }
    }

    private void tratarNaoSuportada(Contexto ctx) {

        if (ctx.humano()) {
            return;
        }

        if (ctx.ultimaSaida() != null && NAO_SUPORTADA.equals(ctx.ultimaSaida().getConteudo())) {
            return;
        }

        enviar(ctx, NAO_SUPORTADA, ctx.perguntaDaUltimaSaida());
    }

    private Optional<Agendamento> carregarAgendamento(PerguntaPendente pergunta) {

        return switch (pergunta.getTipo().getReferencia()) {
            case MEDICAO -> medicaoRepository.findById(pergunta.getReferenciaId()).map(m -> (Agendamento) m);
            case INSTALACAO -> instalacaoRepository.findById(pergunta.getReferenciaId()).map(m -> (Agendamento) m);
            case ORCAMENTO -> Optional.empty();
        };
    }

    private void confirmar(Agendamento agendamento, PerguntaPendente pergunta, AtendimentoWhatsapp atendimento) {

        if (agendamento instanceof Medicao medicao) {
            medicaoService.confirmarPeloCliente(medicao, pergunta, atendimento);
        } else if (agendamento instanceof Instalacao instalacao) {
            instalacaoService.confirmarPeloCliente(instalacao, pergunta, atendimento);
        }
    }

    private void recusar(Agendamento agendamento, PerguntaPendente pergunta, AtendimentoWhatsapp atendimento) {

        if (agendamento instanceof Medicao medicao) {
            medicaoService.recusarPeloCliente(medicao, pergunta, atendimento);
        } else if (agendamento instanceof Instalacao instalacao) {
            instalacaoService.recusarPeloCliente(instalacao, pergunta, atendimento);
        }
    }

    private void sugerir(Agendamento agendamento, PerguntaPendente pergunta, String texto, AtendimentoWhatsapp atendimento) {

        if (agendamento instanceof Medicao medicao) {
            medicaoService.registrarSugestaoCliente(medicao, pergunta, texto, atendimento);
        } else if (agendamento instanceof Instalacao instalacao) {
            instalacaoService.registrarSugestaoCliente(instalacao, pergunta, texto, atendimento);
        }
    }

    private void escalarComResposta(Contexto ctx, String descricao, String resposta) {

        conversaWhatsappService.escalar(ctx.atendimento(), descricao);

        String texto = horarioAtendimento.avisoForaDoHorario(ctx.atendimento().getEmpresa())
                .map(aviso -> resposta + "\n\n" + aviso)
                .orElse(resposta);

        whatsappSaidaService.enfileirar(NovaMensagem.doBot(ctx.atendimento(), texto)
                .comCategoria(CategoriaMensagemSaida.ESCALONAMENTO));
    }

    private void enviar(Contexto ctx, String texto, PerguntaPendente pergunta) {
        whatsappSaidaService.enfileirar(NovaMensagem.doBot(ctx.atendimento(), texto).comPergunta(pergunta));
    }

    private static String rotulo(Agendamento agendamento) {
        return agendamento.getTipoAgendamento() == TipoAgendamento.MEDICAO ? "medição" : "instalação";
    }

    private static String nome(Agendamento agendamento) {
        return agendamento.getTipoAgendamento() == TipoAgendamento.MEDICAO
                ? "a visita técnica para medição"
                : "a instalação";
    }

    private static String capitalizar(String texto) {
        return texto.isEmpty() ? texto : Character.toUpperCase(texto.charAt(0)) + texto.substring(1);
    }
}
