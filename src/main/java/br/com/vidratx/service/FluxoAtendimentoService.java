package br.com.vidratx.service;

import br.com.vidratx.conversa.Interpretacao;
import br.com.vidratx.conversa.InterpretadorResposta;
import br.com.vidratx.entity.AtendimentoWhatsapp;
import br.com.vidratx.entity.Cliente;
import br.com.vidratx.entity.Orcamento;
import br.com.vidratx.entity.PerguntaFrequente;
import br.com.vidratx.entity.PerguntaPendente;
import br.com.vidratx.entity.SolicitacaoOrcamento;
import br.com.vidratx.enums.CanalSolicitacao;
import br.com.vidratx.enums.EtapaFluxo;
import br.com.vidratx.enums.StatusAtendimento;
import br.com.vidratx.enums.StatusOrcamento;
import br.com.vidratx.enums.TipoPergunta;
import br.com.vidratx.repository.ClienteRepository;
import br.com.vidratx.repository.OrcamentoRepository;
import br.com.vidratx.repository.SolicitacaoOrcamentoRepository;
import br.com.vidratx.event.SolicitacaoRecebidaPeloBotEvent;
import br.com.vidratx.util.MedidaParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Pattern;

@Service
public class FluxoAtendimentoService {

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");

    private static final String SEPARADOR_DETALHES = "\nDetalhes: ";
    private static final String PREFIXO_SERVICO = "Serviço desejado: ";
    private static final String PREFIXO_MEDIDA = "\nMedida entendida: ";

    private static final Map<String, String> SERVICOS = new LinkedHashMap<>();

    static {
        SERVICOS.put("box", "box de banheiro");
        SERVICOS.put("espelho", "espelho");
        SERVICOS.put("guarda corpo", "guarda-corpo");
        SERVICOS.put("sacada", "fechamento de sacada");
        SERVICOS.put("janela", "janela");
        SERVICOS.put("porta", "porta de vidro");
        SERVICOS.put("tampo", "tampo de vidro");
    }

    private static final Pattern PEDE_ORCAMENTO = Pattern.compile(
            "\\b(orcamento|orcamentos|orcar|preco|precos|quanto custa|quanto fica|quanto sai|valor)\\b");

    private static final Pattern QUER_SERVICO = Pattern.compile(
            "\\b(quero|queria|preciso|gostaria|fazer|trocar|instalar|colocar)\\b");

    private static final Pattern CONSULTA_ORCAMENTO = Pattern.compile(
            "\\b(aquele|meu|o|cade|como esta|como ta|andamento|resposta|ficou|enviaram|mandaram|status)\\b"
                    + ".*\\borcamento|\\borcamento\\b.*\\b(ficou|saiu|chegou|pronto)\\b");

    private static final Pattern PREFIXOS_NOME = Pattern.compile(
            "^(meu nome e|meu nome eh|me chamo|pode me chamar de|eu sou o|eu sou a|eu sou|sou o|sou a|sou"
                    + "|aqui e o|aqui e a|aqui e|e o|e a)\\s+"
    );

    private static final Set<String> NAO_E_NOME = Set.of(
            "orcamento", "orcamentos", "preco", "valor", "quero", "queria", "preciso", "gostaria", "box", "vidro",
            "vidros", "espelho", "janela", "janelas", "porta", "portas", "sacada", "guarda", "corpo", "medida",
            "medidas", "instalacao", "orcar", "quanto", "custa", "sim", "nao", "ok", "obrigado", "obrigada", "oi",
            "ola", "bom", "boa", "dia", "tarde", "noite", "tudo", "bem", "atendente", "ajuda", "informacao",
            "servico", "fazer", "trocar", "vcs", "voces", "tem", "teria", "pra", "para", "com", "banheiro",
            "cozinha", "blindex", "temperado", "visita", "medicao", "opa", "eai", "alo"
    );

    private static final Set<String> CONECTIVOS_NOME = Set.of("de", "da", "do", "das", "dos", "e");

    private static final int MAXIMO_ORCAMENTOS_CONSULTA = 5;

    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy", PT_BR);

    private final ClienteRepository clienteRepository;
    private final SolicitacaoOrcamentoRepository solicitacaoOrcamentoRepository;
    private final OrcamentoRepository orcamentoRepository;
    private final PerguntaPendenteService perguntaPendenteService;
    private final PerguntaFrequenteService perguntaFrequenteService;
    private final HorarioAtendimento horarioAtendimento;
    private final ApplicationEventPublisher eventos;
    private final int maxTentativas;

    public FluxoAtendimentoService(
            ClienteRepository clienteRepository,
            SolicitacaoOrcamentoRepository solicitacaoOrcamentoRepository,
            OrcamentoRepository orcamentoRepository,
            PerguntaPendenteService perguntaPendenteService,
            PerguntaFrequenteService perguntaFrequenteService,
            HorarioAtendimento horarioAtendimento,
            ApplicationEventPublisher eventos,
            @Value("${vidratx.whatsapp.max-tentativas:3}") int maxTentativas) {

        this.clienteRepository = clienteRepository;
        this.solicitacaoOrcamentoRepository = solicitacaoOrcamentoRepository;
        this.orcamentoRepository = orcamentoRepository;
        this.perguntaPendenteService = perguntaPendenteService;
        this.perguntaFrequenteService = perguntaFrequenteService;
        this.horarioAtendimento = horarioAtendimento;
        this.eventos = eventos;
        this.maxTentativas = maxTentativas;
    }

    public record RespostaFluxo(String texto, PerguntaPendente pergunta, List<PerguntaPendente> escolher) {

        public RespostaFluxo(String texto, PerguntaPendente pergunta) {
            this(texto, pergunta, List.of());
        }

        static RespostaFluxo de(String texto) {
            return new RespostaFluxo(texto, null);
        }
    }

    public String iniciarConversa(AtendimentoWhatsapp atendimento) {
        return iniciarConversa(atendimento, null);
    }

    public String iniciarConversa(AtendimentoWhatsapp atendimento, String primeiraMensagem) {

        String palavras = InterpretadorResposta.palavras(primeiraMensagem);
        boolean querOrcamento = pedeOrcamento(palavras);
        String servico = querOrcamento ? servicoCitado(palavras) : null;

        Optional<PerguntaFrequente> duvida = querOrcamento || primeiraMensagem == null
                ? Optional.empty()
                : perguntaFrequenteService.encontrar(atendimento.getEmpresa().getId(), primeiraMensagem);

        if (atendimento.getCliente() != null) {

            String saudacao = "Olá, " + primeiroNome(atendimento.getCliente().getNome()) + "!";

            if (querOrcamento) {
                return comecarOrcamento(atendimento, servico, saudacao);
            }

            return irParaMenu(atendimento, duvida
                    .map(p -> saudacao + " " + p.getResposta())
                    .orElse(saudacao + " Que bom te ver de novo por aqui."));
        }

        atendimento.setEtapaFluxo(EtapaFluxo.COLETA_NOME);
        atendimento.setTentativasErro(0);

        if (querOrcamento) {

            atendimento.setDadosColetados(PREFIXO_SERVICO + (servico != null ? servico : ""));

            return "Olá! Vamos fazer o seu orçamento. Antes, qual é o seu nome?";
        }

        return duvida
                .map(p -> "Olá! " + p.getResposta() + "\n\nPara continuar, qual é o seu nome?")
                .orElse("Olá! Bem-vindo(a). Antes de começar, qual é o seu nome?");
    }

    static boolean pedeOrcamento(String palavras) {

        return PEDE_ORCAMENTO.matcher(palavras).find()
                || (servicoCitado(palavras) != null && QUER_SERVICO.matcher(palavras).find());
    }

    static boolean consultaOrcamento(String palavras) {
        return CONSULTA_ORCAMENTO.matcher(palavras).find()
                || palavras.matches(".*\\b(quanto ficou|qual o valor|qual e o valor|qual valor)\\b.*");
    }

    static String servicoCitado(String palavras) {

        String texto = " " + palavras + " ";

        return SERVICOS.entrySet().stream()
                .filter(e -> texto.contains(" " + e.getKey() + " ") || texto.contains(" " + e.getKey() + "s "))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(null);
    }

    private String comecarOrcamento(AtendimentoWhatsapp atendimento, String servico, String saudacao) {

        atendimento.setTentativasErro(0);

        if (servico == null || servico.isBlank()) {

            atendimento.setDadosColetados(null);
            atendimento.setEtapaFluxo(EtapaFluxo.COLETA_SERVICO);

            return saudacao + " Qual tipo de serviço você precisa? "
                    + "(ex.: box de banheiro, espelho, janela, guarda-corpo...)";
        }

        atendimento.setDadosColetados(PREFIXO_SERVICO + servico);
        atendimento.setEtapaFluxo(EtapaFluxo.COLETA_DESCRICAO);

        return saudacao + " Vamos ao seu orçamento de " + servico + ". " + textoPedidoDetalhes();
    }

    private static String textoPedidoDetalhes() {
        return "Me mande as medidas aproximadas (largura x altura, ex.: 1,20 x 1,90), "
                + "o bairro e o que mais ajudar no orçamento.";
    }

    public String processarMensagem(AtendimentoWhatsapp atendimento, String mensagemRecebida) {
        return responder(atendimento, mensagemRecebida).texto();
    }

    public RespostaFluxo responder(AtendimentoWhatsapp atendimento, String mensagemRecebida) {

        String texto = mensagemRecebida == null ? "" : mensagemRecebida.trim();

        if (atendimento.getEtapaFluxo() == null) {
            atendimento.setEtapaFluxo(EtapaFluxo.MENU);
        }

        Interpretacao interpretacao = InterpretadorResposta.interpretar(texto);

        String respostaComandoGlobal = processarComandoGlobal(atendimento, texto, interpretacao);

        if (respostaComandoGlobal != null) {
            return RespostaFluxo.de(respostaComandoGlobal);
        }

        return switch (atendimento.getEtapaFluxo()) {
            case COLETA_NOME -> RespostaFluxo.de(processarColetaNome(atendimento, texto));
            case MENU -> processarMenu(atendimento, texto);
            case COLETA_SERVICO -> RespostaFluxo.de(processarColetaServico(atendimento, texto));
            case COLETA_DESCRICAO -> RespostaFluxo.de(processarColetaDescricao(atendimento, texto));
            case CONFIRMACAO -> RespostaFluxo.de(processarConfirmacao(atendimento, interpretacao));
            case CORRECAO -> RespostaFluxo.de(processarCorrecao(atendimento, interpretacao));
            case DUVIDA -> RespostaFluxo.de(processarDuvida(atendimento, texto));
        };
    }

    public String aoReceberImagem(AtendimentoWhatsapp atendimento) {

        if (atendimento.getEtapaFluxo() == null || atendimento.getEtapaFluxo() == EtapaFluxo.MENU) {
            return "Recebi a sua foto! Ela fica aqui na conversa para a nossa equipe.";
        }

        return switch (atendimento.getEtapaFluxo()) {
            case COLETA_NOME -> "Recebi a sua foto! Antes de continuar, qual é o seu nome?";
            case COLETA_SERVICO -> "Foto recebida! Ela vai junto com a sua solicitação. "
                    + "Qual tipo de serviço você precisa? (ex.: box de banheiro, espelho, janela, guarda-corpo...)";
            case COLETA_DESCRICAO -> "Foto recebida! Ela vai junto com a sua solicitação. Agora me conte por escrito "
                    + "os detalhes: medidas aproximadas, endereço e o que mais ajudar no orçamento.";
            case CONFIRMACAO -> "Foto recebida! Ela vai junto com a sua solicitação.\n\n" + textoConfirmacao(atendimento);
            case CORRECAO -> "Foto recebida! Ela vai junto com a sua solicitação.\n\n" + textoCorrecao();
            case MENU, DUVIDA -> "Recebi a sua foto! Ela fica aqui na conversa para a nossa equipe.";
        };
    }

    private String processarComandoGlobal(AtendimentoWhatsapp atendimento, String texto, Interpretacao interpretacao) {

        if (interpretacao.pedeAtendente()) {
            return escalarParaAtendente(atendimento, "Tudo bem, vou te transferir para um atendente. Só um momento!");
        }

        boolean podeVoltar = atendimento.getEtapaFluxo() != EtapaFluxo.COLETA_NOME
                && atendimento.getEtapaFluxo() != EtapaFluxo.MENU;

        if (texto.equals("0") && podeVoltar) {

            atendimento.setDadosColetados(null);
            atendimento.setTentativasErro(0);

            return irParaMenu(atendimento, "Ok, voltando ao menu principal.");
        }

        return null;
    }

    private String processarColetaNome(AtendimentoWhatsapp atendimento, String texto) {

        Optional<String> nome = extrairNome(texto);

        if (nome.isEmpty()) {
            return reprometerOuEscalar(atendimento, "Antes de continuar, qual é o seu nome? Me diga só o nome, por favor.");
        }

        Cliente cliente = new Cliente();

        cliente.setEmpresa(atendimento.getEmpresa());
        cliente.setNome(nome.get());
        cliente.setTelefone(atendimento.getTelefone());
        cliente.setWhatsapp(atendimento.getTelefone());

        clienteRepository.save(cliente);

        atendimento.setCliente(cliente);
        atendimento.setTentativasErro(0);

        String saudacao = "Prazer, " + primeiroNome(nome.get()) + "!";
        String dados = atendimento.getDadosColetados();

        if (dados != null && dados.startsWith(PREFIXO_SERVICO) && !dados.contains(SEPARADOR_DETALHES)) {
            return comecarOrcamento(atendimento, dados.substring(PREFIXO_SERVICO.length()), saudacao);
        }

        return irParaMenu(atendimento, saudacao);
    }

    static Optional<String> extrairNome(String texto) {

        if (texto == null) {
            return Optional.empty();
        }

        String limpo = texto.trim().replaceAll("[.!,;]+$", "").trim();

        if (limpo.isEmpty() || limpo.length() > 60 || limpo.matches(".*[0-9?@#/:].*")
                || !limpo.matches("(?s).*\\p{L}{2,}.*")) {
            return Optional.empty();
        }

        String normalizado = InterpretadorResposta.interpretar(limpo).textoNormalizado();
        String semPrefixo = PREFIXOS_NOME.matcher(normalizado).replaceFirst("");

        if (semPrefixo.isBlank()) {
            return Optional.empty();
        }

        List<String> palavras = Arrays.asList(semPrefixo.split(" "));

        if (palavras.size() > 5 || palavras.stream().anyMatch(NAO_E_NOME::contains)) {
            return Optional.empty();
        }

        String[] originais = limpo.split("\\s+");
        List<String> originaisFinais = Arrays.asList(originais)
                .subList(Math.max(0, originais.length - palavras.size()), originais.length);

        StringBuilder nome = new StringBuilder();

        for (String palavra : originaisFinais) {

            String minuscula = palavra.toLowerCase(PT_BR);

            if (!nome.isEmpty()) {
                nome.append(' ');
            }

            if (CONECTIVOS_NOME.contains(minuscula) && !nome.isEmpty()) {
                nome.append(minuscula);
            } else {
                nome.append(Character.toUpperCase(minuscula.charAt(0))).append(minuscula.substring(1));
            }
        }

        return nome.isEmpty() ? Optional.empty() : Optional.of(nome.toString());
    }

    private RespostaFluxo processarMenu(AtendimentoWhatsapp atendimento, String texto) {

        String opcao = InterpretadorResposta.interpretar(texto).textoNormalizado();

        if (opcao.equals("2") || opcao.contains("consultar") || opcao.contains("acompanhar")
                || opcao.contains("andamento") || consultaOrcamento(opcao)) {

            return consultarOrcamento(atendimento);
        }

        if (opcao.equals("1") || pedeOrcamento(opcao)) {
            return RespostaFluxo.de(comecarOrcamento(atendimento, opcao.equals("1") ? null : servicoCitado(opcao), "Certo!"));
        }

        if (opcao.equals("3")) {
            return RespostaFluxo.de(escalarParaAtendente(
                    atendimento, "Tudo bem, vou te transferir para um atendente. Só um momento!"
            ));
        }

        if (opcao.equals("4") || opcao.equals("duvida") || opcao.contains("tirar uma duvida")) {

            atendimento.setEtapaFluxo(EtapaFluxo.DUVIDA);
            atendimento.setTentativasErro(0);

            return RespostaFluxo.de("Pode mandar a sua dúvida que eu te respondo.");
        }

        Optional<PerguntaFrequente> duvida =
                perguntaFrequenteService.encontrar(atendimento.getEmpresa().getId(), texto);

        if (duvida.isPresent()) {
            return RespostaFluxo.de(irParaMenu(atendimento, duvida.get().getResposta() + "\n\nPosso ajudar em mais alguma coisa?"));
        }

        return RespostaFluxo.de(reprometerOuEscalar(
                atendimento,
                "Não entendi sua escolha. Responda com o número da opção:\n\n" + textoMenu()
        ));
    }

    private String processarDuvida(AtendimentoWhatsapp atendimento, String texto) {

        Optional<PerguntaFrequente> duvida =
                perguntaFrequenteService.encontrar(atendimento.getEmpresa().getId(), texto);

        if (duvida.isPresent()) {
            return irParaMenu(atendimento, duvida.get().getResposta() + "\n\nPosso ajudar em mais alguma coisa?");
        }

        atendimento.setEtapaFluxo(EtapaFluxo.MENU);

        return escalarParaAtendente(atendimento,
                "Vou passar a sua dúvida para a nossa equipe, que já te responde por aqui.");
    }

    private String processarColetaServico(AtendimentoWhatsapp atendimento, String texto) {

        if (texto.isBlank() || texto.length() > 500) {
            return reprometerOuEscalar(atendimento, "Não entendi. Qual tipo de serviço você precisa?");
        }

        atendimento.setTentativasErro(0);

        String dados = atendimento.getDadosColetados();

        if (dados != null && dados.contains(SEPARADOR_DETALHES)) {

            atendimento.setDadosColetados(PREFIXO_SERVICO + texto + dados.substring(dados.indexOf(SEPARADOR_DETALHES)));
            atendimento.setEtapaFluxo(EtapaFluxo.CONFIRMACAO);

            return "Corrigido!\n\n" + textoConfirmacao(atendimento);
        }

        atendimento.setDadosColetados(PREFIXO_SERVICO + texto);
        atendimento.setEtapaFluxo(EtapaFluxo.COLETA_DESCRICAO);

        return "Certo! " + textoPedidoDetalhes();
    }

    private String processarColetaDescricao(AtendimentoWhatsapp atendimento, String texto) {

        if (texto.isBlank() || texto.length() > 2000) {
            return reprometerOuEscalar(atendimento, "Não entendi. Pode descrever com mais detalhes?");
        }

        String dados = atendimento.getDadosColetados() == null ? PREFIXO_SERVICO + "(não informado)" : atendimento.getDadosColetados();
        boolean correcao = dados.contains(SEPARADOR_DETALHES);

        String servico = correcao ? dados.substring(0, dados.indexOf(SEPARADOR_DETALHES)) : dados;

        String medida = MedidaParser.tentarInterpretar(texto)
                .map(m -> PREFIXO_MEDIDA + m.descricaoEmMetros() + " (largura × altura)")
                .orElse(PREFIXO_MEDIDA + "não identifiquei — a equipe confirma com você");

        atendimento.setDadosColetados(servico + SEPARADOR_DETALHES + texto + medida);
        atendimento.setEtapaFluxo(EtapaFluxo.CONFIRMACAO);
        atendimento.setTentativasErro(0);

        return (correcao ? "Corrigido!\n\n" : "") + textoConfirmacao(atendimento);
    }

    private String processarConfirmacao(AtendimentoWhatsapp atendimento, Interpretacao interpretacao) {

        if (interpretacao.opcao(1) || interpretacao.confirmacao()) {

            SolicitacaoOrcamento solicitacao = new SolicitacaoOrcamento();

            solicitacao.setEmpresa(atendimento.getEmpresa());
            solicitacao.setCliente(atendimento.getCliente());
            solicitacao.setCanal(CanalSolicitacao.WHATSAPP);
            solicitacao.setDescricao(atendimento.getDadosColetados());

            solicitacaoOrcamentoRepository.save(solicitacao);

            atendimento.setSolicitacaoOrcamento(solicitacao);

            eventos.publishEvent(new SolicitacaoRecebidaPeloBotEvent(solicitacao.getId()));

            return escalarParaAtendente(
                    atendimento,
                    "Perfeito! Recebemos sua solicitação e um atendente vai te responder em breve com o orçamento."
            );
        }

        boolean querCorrigir = interpretacao.opcao(2)
                || interpretacao.negacao()
                || interpretacao.pedeAlteracao()
                || (" " + interpretacao.textoNormalizado() + " ").contains(" mas ")
                || interpretacao.textoNormalizado().split(" ").length >= 3;

        if (querCorrigir) {

            atendimento.setEtapaFluxo(EtapaFluxo.CORRECAO);
            atendimento.setTentativasErro(0);

            return "Sem problemas! " + textoCorrecao();
        }

        return reprometerOuEscalar(atendimento, "Não entendi. " + opcoesConfirmacao());
    }

    private String processarCorrecao(AtendimentoWhatsapp atendimento, Interpretacao interpretacao) {

        String texto = interpretacao.textoNormalizado();

        if (interpretacao.opcao(1) || texto.contains("servico") || texto.contains("tipo")) {

            atendimento.setEtapaFluxo(EtapaFluxo.COLETA_SERVICO);
            atendimento.setTentativasErro(0);

            return "Qual é o tipo de serviço correto? (ex.: box de banheiro, espelho, janela...)";
        }

        if (interpretacao.opcao(2) || texto.contains("detalhe") || texto.contains("medida") || texto.contains("endereco")) {

            atendimento.setEtapaFluxo(EtapaFluxo.COLETA_DESCRICAO);
            atendimento.setTentativasErro(0);

            return "Me mande os detalhes corretos: medidas aproximadas, endereço e o que mais ajudar no orçamento.";
        }

        if (interpretacao.opcao(3) || interpretacao.negacao()) {

            atendimento.setDadosColetados(null);
            atendimento.setTentativasErro(0);

            return irParaMenu(atendimento, "Tudo bem, cancelei essa solicitação.");
        }

        return reprometerOuEscalar(atendimento, "Não entendi. " + textoCorrecao());
    }

    private RespostaFluxo consultarOrcamento(AtendimentoWhatsapp atendimento) {

        Cliente cliente = atendimento.getCliente();

        if (cliente == null) {
            return RespostaFluxo.de(irParaMenu(atendimento, "Ainda não encontrei nenhum orçamento no seu nome."));
        }

        List<Orcamento> orcamentos = orcamentoRepository.findAllByEmpresaIdAndClienteIdOrderByCriadoEmDesc(
                atendimento.getEmpresa().getId(), cliente.getId()
        );

        if (orcamentos.isEmpty()) {
            return RespostaFluxo.de(irParaMenu(atendimento, "Ainda não encontrei nenhum orçamento no seu nome."));
        }

        orcamentos = orcamentos.stream()
                .sorted(Comparator.comparing(Orcamento::getId, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();

        List<Orcamento> emAberto = orcamentos.stream()
                .filter(o -> o.getStatus() != StatusOrcamento.PERDIDO && o.getStatus() != StatusOrcamento.EXPIRADO)
                .limit(MAXIMO_ORCAMENTOS_CONSULTA)
                .toList();

        List<Orcamento> mostrados = emAberto.isEmpty() ? List.of(orcamentos.get(0)) : emAberto;

        List<PerguntaPendente> ativas = perguntaPendenteService
                .ativas(atendimento.getEmpresa().getId(), atendimento.getTelefone());

        List<PerguntaPendente> aprovacoes = mostrados.stream()
                .filter(o -> o.getStatus() == StatusOrcamento.ENVIADO)
                .map(o -> ativas.stream()
                        .filter(p -> p.getTipo() == TipoPergunta.APROVAR_ORCAMENTO && p.getReferenciaId().equals(o.getId()))
                        .findFirst().orElse(null))
                .filter(Objects::nonNull)
                .toList();

        if (mostrados.size() == 1) {

            Orcamento unico = mostrados.get(0);
            StringBuilder mensagem = new StringBuilder("Seu orçamento nº " + unico.getId() + " está "
                    + descreverOrcamento(unico, !aprovacoes.isEmpty()));

            if (!aprovacoes.isEmpty()) {

                mensagem.append("\n\nResponda:\n1 - Aprovar\n2 - Pedir alteração\n3 - Falar com um atendente\n4 - Não tenho interesse");

                return new RespostaFluxo(mensagem.toString(), aprovacoes.get(0));
            }

            return RespostaFluxo.de(irParaMenu(atendimento, mensagem.toString()));
        }

        StringBuilder mensagem = new StringBuilder("Você tem ").append(mostrados.size())
                .append(" orçamentos com a gente:\n");

        for (Orcamento orcamento : mostrados) {

            boolean aguardaResposta = aprovacoes.stream().anyMatch(p -> p.getReferenciaId().equals(orcamento.getId()));

            mensagem.append("\n• nº ").append(orcamento.getId()).append(": ")
                    .append(descreverOrcamento(orcamento, aguardaResposta));
        }

        if (aprovacoes.size() == 1) {

            PerguntaPendente unica = aprovacoes.get(0);

            mensagem.append("\n\nSobre o orçamento nº ").append(unica.getReferenciaId())
                    .append(", responda:\n1 - Aprovar\n2 - Pedir alteração\n3 - Falar com um atendente\n4 - Não tenho interesse");

            return new RespostaFluxo(mensagem.toString(), unica);
        }

        if (aprovacoes.size() > 1) {
            return new RespostaFluxo(mensagem.toString(), null, aprovacoes);
        }

        return RespostaFluxo.de(irParaMenu(atendimento, mensagem.toString()));
    }

    private String descreverOrcamento(Orcamento orcamento, boolean aguardaResposta) {

        StringBuilder descricao = new StringBuilder(descreverStatusOrcamento(orcamento.getStatus())).append('.');

        if ((orcamento.getStatus() == StatusOrcamento.ENVIADO || orcamento.getStatus() == StatusOrcamento.APROVADO)
                && orcamento.getValorTotal() != null) {

            descricao.append(" Valor: ").append(formatarValor(orcamento.getValorTotal())).append('.');
        }

        if (aguardaResposta && orcamento.getValidoAte() != null) {
            descricao.append(" Válido até ").append(orcamento.getValidoAte().format(FORMATO_DATA)).append('.');
        }

        return descricao.toString();
    }

    private String descreverStatusOrcamento(StatusOrcamento status) {

        return switch (status) {
            case NOVO_CONTATO, PRE_ORCAMENTO, VISITA_AGENDADA, MEDIDO, ORCAMENTO_FINAL -> "em elaboração pela nossa equipe";
            case ENVIADO -> "enviado, aguardando sua aprovação";
            case APROVADO -> "aprovado";
            case PERDIDO -> "encerrado";
            case EXPIRADO -> "vencido";
        };
    }

    private String textoConfirmacao(AtendimentoWhatsapp atendimento) {

        return "Confirmando:\n\n" + atendimento.getDadosColetados()
                + "\n\nPosso enviar essa solicitação para a nossa equipe?\n" + opcoesConfirmacao();
    }

    private String opcoesConfirmacao() {
        return "Responda:\n1 - Sim, pode enviar\n2 - Quero corrigir alguma coisa";
    }

    private String textoCorrecao() {
        return "O que você quer corrigir?\n1 - O tipo de serviço\n2 - Os detalhes (medidas, endereço...)\n3 - Cancelar a solicitação";
    }

    private String formatarValor(BigDecimal valor) {
        return NumberFormat.getCurrencyInstance(PT_BR).format(valor);
    }

    private String reprometerOuEscalar(AtendimentoWhatsapp atendimento, String mensagemRetry) {

        int tentativas = atendimento.getTentativasErro() + 1;
        atendimento.setTentativasErro(tentativas);

        if (tentativas >= maxTentativas) {
            return escalarParaAtendente(
                    atendimento, "Não consegui entender suas respostas. Vou te transferir para um atendente."
            );
        }

        return mensagemRetry;
    }

    private String escalarParaAtendente(AtendimentoWhatsapp atendimento, String mensagemFinal) {

        atendimento.setStatus(StatusAtendimento.AGUARDANDO_ATENDENTE);
        atendimento.setTentativasErro(0);

        return horarioAtendimento.avisoForaDoHorario(atendimento.getEmpresa())
                .map(aviso -> mensagemFinal + "\n\n" + aviso)
                .orElse(mensagemFinal);
    }

    public String irParaMenu(AtendimentoWhatsapp atendimento, String saudacao) {

        atendimento.setEtapaFluxo(EtapaFluxo.MENU);
        atendimento.setTentativasErro(0);

        return saudacao + "\n\n" + textoMenu();
    }

    private String textoMenu() {

        return """
                Como posso ajudar?
                1 - Solicitar orçamento
                2 - Acompanhar meu orçamento/pedido
                3 - Falar com atendente
                4 - Tirar uma dúvida""";
    }

    private String primeiroNome(String nomeCompleto) {
        return nomeCompleto == null || nomeCompleto.isBlank() ? "" : nomeCompleto.trim().split("\\s+")[0];
    }
}
