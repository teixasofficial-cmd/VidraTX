package br.com.vidratx.service;

import br.com.vidratx.config.FusoEmpresa;
import br.com.vidratx.dto.AtendenteResponse;
import br.com.vidratx.dto.AtendimentoWhatsappDetalheResponse;
import br.com.vidratx.dto.AtendimentoWhatsappResponse;
import br.com.vidratx.entity.AtendimentoWhatsapp;
import br.com.vidratx.entity.Cliente;
import br.com.vidratx.entity.MensagemAtendimento;
import br.com.vidratx.entity.PerguntaPendente;
import br.com.vidratx.entity.Usuario;
import br.com.vidratx.entity.WhatsappContato;
import br.com.vidratx.enums.CategoriaMensagemSaida;
import br.com.vidratx.enums.EtapaFluxo;
import br.com.vidratx.enums.RemetenteMensagem;
import br.com.vidratx.enums.StatusAgendamento;
import br.com.vidratx.enums.StatusAtendimento;
import br.com.vidratx.enums.TipoEventoHistorico;
import br.com.vidratx.enums.TipoMensagem;
import br.com.vidratx.exception.AtendimentoNaoEncontradoException;
import br.com.vidratx.exception.ClienteNaoEncontradoException;
import br.com.vidratx.exception.MensagemNaoEncontradaException;
import br.com.vidratx.exception.PendenciasAbertasException;
import br.com.vidratx.exception.TransicaoInvalidaException;
import br.com.vidratx.exception.UsuarioNaoEncontradoException;
import br.com.vidratx.mapper.AtendimentoWhatsappMapper;
import br.com.vidratx.repository.AtendimentoWhatsappRepository;
import br.com.vidratx.repository.ClienteRepository;
import br.com.vidratx.repository.InstalacaoRepository;
import br.com.vidratx.repository.MedicaoRepository;
import br.com.vidratx.repository.MensagemAtendimentoRepository;
import br.com.vidratx.repository.UsuarioRepository;
import br.com.vidratx.service.WhatsappSaidaService.NovaMensagem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

@Service
public class AtendimentoWhatsappService {

    private static final Logger log =
            LoggerFactory.getLogger(AtendimentoWhatsappService.class);

    private static final Set<StatusAtendimento> COM_PESSOA =
            EnumSet.of(StatusAtendimento.AGUARDANDO_ATENDENTE, StatusAtendimento.EM_ATENDIMENTO_HUMANO);

    private static final int PRAZO_MINIMO_MINUTOS = 5;

    static final String AVISO_ESPERA = "Recebemos a sua mensagem e um atendente vai te responder assim que possível. "
            + "Se preferir voltar ao atendimento automático, responda 0.";

    private final AtendimentoWhatsappRepository atendimentoWhatsappRepository;
    private final MensagemAtendimentoRepository mensagemAtendimentoRepository;
    private final ClienteRepository clienteRepository;
    private final UsuarioRepository usuarioRepository;
    private final MedicaoRepository medicaoRepository;
    private final InstalacaoRepository instalacaoRepository;
    private final AtendimentoWhatsappMapper atendimentoWhatsappMapper;
    private final WhatsappMidiaStorage whatsappMidiaStorage;
    private final HistoricoService historicoService;
    private final WhatsappContatoService whatsappContatoService;
    private final WhatsappSaidaService whatsappSaidaService;
    private final PerguntaPendenteService perguntaPendenteService;
    private final ConversaWhatsappService conversaWhatsappService;
    private final TransactionTemplate transactionTemplate;
    private final Clock clock;
    private final FusoEmpresa fuso;

    public AtendimentoWhatsappService(
            AtendimentoWhatsappRepository atendimentoWhatsappRepository,
            MensagemAtendimentoRepository mensagemAtendimentoRepository,
            ClienteRepository clienteRepository,
            UsuarioRepository usuarioRepository,
            MedicaoRepository medicaoRepository,
            InstalacaoRepository instalacaoRepository,
            AtendimentoWhatsappMapper atendimentoWhatsappMapper,
            WhatsappMidiaStorage whatsappMidiaStorage,
            HistoricoService historicoService,
            WhatsappContatoService whatsappContatoService,
            WhatsappSaidaService whatsappSaidaService,
            PerguntaPendenteService perguntaPendenteService,
            ConversaWhatsappService conversaWhatsappService,
            PlatformTransactionManager transactionManager,
            Clock clock) {

        this.atendimentoWhatsappRepository = atendimentoWhatsappRepository;
        this.mensagemAtendimentoRepository = mensagemAtendimentoRepository;
        this.clienteRepository = clienteRepository;
        this.usuarioRepository = usuarioRepository;
        this.medicaoRepository = medicaoRepository;
        this.instalacaoRepository = instalacaoRepository;
        this.atendimentoWhatsappMapper = atendimentoWhatsappMapper;
        this.whatsappMidiaStorage = whatsappMidiaStorage;
        this.historicoService = historicoService;
        this.whatsappContatoService = whatsappContatoService;
        this.whatsappSaidaService = whatsappSaidaService;
        this.perguntaPendenteService = perguntaPendenteService;
        this.conversaWhatsappService = conversaWhatsappService;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.clock = clock;
        this.fuso = new FusoEmpresa(clock);
    }

    @Transactional(readOnly = true)
    public List<AtendimentoWhatsappResponse> listarPendentes(Long empresaId) {

        return atendimentoWhatsappRepository
                .listarClienteAguardandoResposta(empresaId, COM_PESSOA)
                .stream()
                .map(this::resposta)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<AtendimentoWhatsappResponse> listarTodos(Long empresaId, StatusAtendimento status) {

        List<AtendimentoWhatsapp> atendimentos = status != null
                ? atendimentoWhatsappRepository.findAllByEmpresaIdAndStatusOrderByAtualizadoEmDesc(empresaId, status)
                : atendimentoWhatsappRepository.findAllByEmpresaIdOrderByAtualizadoEmDesc(empresaId);

        return atendimentos.stream().map(this::resposta).toList();
    }

    @Transactional(readOnly = true)
    public long contarPendentes(Long empresaId) {
        return atendimentoWhatsappRepository.contarClienteAguardandoResposta(empresaId, COM_PESSOA);
    }

    @Transactional(readOnly = true)
    public long contarAtrasados(Long empresaId) {

        LocalDateTime agora = LocalDateTime.now(clock);

        return atendimentoWhatsappRepository
                .listarClienteAguardandoResposta(empresaId, COM_PESSOA)
                .stream()
                .filter(a -> a.respostaAtrasada(agora))
                .count();
    }

    @Transactional
    public AtendimentoWhatsappDetalheResponse detalhar(Long empresaId, Long atendimentoId) {

        AtendimentoWhatsapp atendimento = buscar(empresaId, atendimentoId);

        List<MensagemAtendimento> mensagens =
                mensagemAtendimentoRepository.findAllByAtendimentoIdOrderByEnviadoEmAscIdAsc(atendimentoId);

        AtendimentoWhatsappDetalheResponse response = atendimentoWhatsappMapper.toDetalheResponse(
                atendimento, mensagens, historicoService.listarPorAtendimento(atendimentoId)
        );

        response.getAtendimento().setPendencias(pendencias(atendimento));
        atendimentoWhatsappMapper.preencherEspera(response.getAtendimento(), atendimento, LocalDateTime.now(clock));

        return response;
    }

    @Transactional(readOnly = true)
    public ResponseEntity<Resource> carregarMidia(Long empresaId, Long mensagemId) {

        MensagemAtendimento mensagem = mensagemAtendimentoRepository
                .findByIdAndAtendimento_Empresa_Id(mensagemId, empresaId)
                .filter(m -> m.getTipo() == TipoMensagem.IMAGEM && m.getMidiaUrl() != null)
                .orElseThrow(() -> new MensagemNaoEncontradaException("Mídia não encontrada"));

        Resource recurso = whatsappMidiaStorage.carregar(mensagem.getMidiaUrl());

        MediaType tipoConteudo = mensagem.getMidiaContentType() != null
                ? MediaType.parseMediaType(mensagem.getMidiaContentType())
                : MediaType.APPLICATION_OCTET_STREAM;

        return ResponseEntity.ok().contentType(tipoConteudo).body(recurso);
    }

    @Transactional
    public AtendimentoWhatsappResponse assumir(Long empresaId, Long atendimentoId, Usuario atendente) {

        AtendimentoWhatsapp atendimento = buscarComContatoTravado(empresaId, atendimentoId);

        if (atendimento.getStatus() == StatusAtendimento.ENCERRADO) {
            throw new TransicaoInvalidaException("A conversa está encerrada. Use \"Reabrir\" para retomar.");
        }

        if (atendimento.getStatus() == StatusAtendimento.EM_ATENDIMENTO_HUMANO) {

            if (mesmoUsuario(atendimento.getAtendente(), atendente)) {
                return resposta(atendimento);
            }

            throw new TransicaoInvalidaException(
                    "Esta conversa está com " + nome(atendimento.getAtendente()) + ". Use \"Transferir\" para assumi-la."
            );
        }

        assumirInterno(atendimento, atendente, atendente.getNome() + " assumiu o atendimento");

        return resposta(atendimento);
    }

    @Transactional(readOnly = true)
    public List<AtendenteResponse> listarAtendentes(Long empresaId) {

        return usuarioRepository.findAllByEmpresaIdAndAtivoTrue(empresaId).stream()
                .map(usuario -> new AtendenteResponse(usuario.getId(), usuario.getNome()))
                .sorted(Comparator.comparing(AtendenteResponse::nome, String.CASE_INSENSITIVE_ORDER))
                .toList();
    }

    @Transactional
    public AtendimentoWhatsappResponse transferir(
            Long empresaId, Long atendimentoId, Long usuarioDestinoId, Usuario responsavel) {

        AtendimentoWhatsapp atendimento = buscarComContatoTravado(empresaId, atendimentoId);

        if (atendimento.getStatus() == StatusAtendimento.ENCERRADO) {
            throw new TransicaoInvalidaException("A conversa está encerrada. Reabra antes de transferir.");
        }

        Usuario destino = usuarioRepository.findByIdAndEmpresaId(usuarioDestinoId, empresaId)
                .filter(u -> Boolean.TRUE.equals(u.getAtivo()))
                .orElseThrow(() -> new UsuarioNaoEncontradoException("Usuário não encontrado ou inativo"));

        String anterior = atendimento.getAtendente() != null ? atendimento.getAtendente().getNome() : "ninguém";

        atendimento.setStatus(StatusAtendimento.EM_ATENDIMENTO_HUMANO);
        atendimento.setAtendente(destino);
        atendimentoWhatsappRepository.saveAndFlush(atendimento);

        historicoService.registrarEventoAtendimento(
                atendimento, TipoEventoHistorico.ATENDIMENTO_TRANSFERIDO,
                "Conversa transferida de " + anterior + " para " + destino.getNome()
                        + " por " + nome(responsavel),
                responsavel
        );

        return resposta(atendimento);
    }

    @Transactional
    public AtendimentoWhatsappResponse responder(
            Long empresaId, Long atendimentoId, Usuario atendente, String mensagem) {

        AtendimentoWhatsapp atendimento = buscarComContatoTravado(empresaId, atendimentoId);

        if (atendimento.getStatus() == StatusAtendimento.ENCERRADO) {
            throw new TransicaoInvalidaException("A conversa está encerrada. Use \"Reabrir\" para responder.");
        }

        if (atendimento.getStatus() == StatusAtendimento.EM_ATENDIMENTO_HUMANO
                && !mesmoUsuario(atendimento.getAtendente(), atendente)) {

            String anterior = nome(atendimento.getAtendente());

            atendimento.setAtendente(atendente);
            atendimentoWhatsappRepository.saveAndFlush(atendimento);

            historicoService.registrarEventoAtendimento(
                    atendimento, TipoEventoHistorico.ATENDIMENTO_TRANSFERIDO,
                    atendente.getNome() + " assumiu a conversa, que estava com " + anterior + ", ao responder",
                    atendente
            );

        } else if (atendimento.getStatus() != StatusAtendimento.EM_ATENDIMENTO_HUMANO) {
            assumirInterno(atendimento, atendente, atendente.getNome() + " assumiu o atendimento ao responder");
        }

        whatsappSaidaService.enfileirar(new NovaMensagem(
                atendimento.getEmpresa(), atendimento.getTelefone(), atendimento.getCliente(), mensagem.trim(),
                CategoriaMensagemSaida.ATENDENTE, RemetenteMensagem.ATENDENTE, null, null, null, atendimento
        ));

        return resposta(atendimento);
    }

    @Transactional
    public AtendimentoWhatsappResponse encerrar(
            Long empresaId, Long atendimentoId, Usuario atendente, boolean forcar, String motivo) {

        AtendimentoWhatsapp atendimento = buscarComContatoTravado(empresaId, atendimentoId);

        if (atendimento.getStatus() == StatusAtendimento.ENCERRADO) {
            return resposta(atendimento);
        }

        List<String> pendencias = pendencias(atendimento);

        if (!pendencias.isEmpty() && !forcar) {
            throw new PendenciasAbertasException(
                    "O cliente ainda tem " + pendencias.size()
                            + (pendencias.size() == 1 ? " pergunta pendente" : " perguntas pendentes")
                            + ". Encerrar mesmo assim? As perguntas continuam valendo se o cliente responder.",
                    pendencias
            );
        }

        conversaWhatsappService.encerrar(atendimento, ConversaWhatsappService.MOTIVO_MANUAL, atendente,
                "Atendimento encerrado por " + atendente.getNome()
                        + (motivo != null && !motivo.isBlank() ? ": " + motivo.trim() : "")
                        + (pendencias.isEmpty() ? "" : " (com pendências: " + String.join("; ", pendencias) + ")"));

        return resposta(atendimento);
    }

    @Transactional
    public AtendimentoWhatsappResponse reabrir(Long empresaId, Long atendimentoId, Usuario atendente) {

        AtendimentoWhatsapp atendimento = buscarComContatoTravado(empresaId, atendimentoId);

        if (atendimento.getStatus() != StatusAtendimento.ENCERRADO) {
            throw new TransicaoInvalidaException("A conversa não está encerrada");
        }

        atendimentoWhatsappRepository
                .findFirstByEmpresaIdAndTelefoneAndStatusNotOrderByCriadoEmDesc(
                        empresaId, atendimento.getTelefone(), StatusAtendimento.ENCERRADO)
                .ifPresent(aberta -> {
                    throw new TransicaoInvalidaException(
                            "Já existe uma conversa aberta com este número (nº " + aberta.getId() + "). Continue por ela."
                    );
                });

        atendimento.setStatus(StatusAtendimento.EM_ATENDIMENTO_HUMANO);
        atendimento.setAtendente(atendente);
        atendimento.setEncerradoEm(null);
        atendimento.setMotivoEncerramento(null);
        atendimento.setTentativasErro(0);
        atendimentoWhatsappRepository.saveAndFlush(atendimento);

        historicoService.registrarEventoAtendimento(
                atendimento, TipoEventoHistorico.ATENDIMENTO_REABERTO,
                "Conversa reaberta por " + atendente.getNome(), atendente
        );

        return resposta(atendimento);
    }

    @Transactional
    public AtendimentoWhatsappResponse devolverAoBot(Long empresaId, Long atendimentoId, Usuario responsavel) {

        AtendimentoWhatsapp atendimento = buscarComContatoTravado(empresaId, atendimentoId);

        if (!COM_PESSOA.contains(atendimento.getStatus())) {
            throw new TransicaoInvalidaException("Só uma conversa com atendente pode ser devolvida ao bot");
        }

        atendimento.setStatus(StatusAtendimento.EM_FLUXO_BOT);
        atendimento.setAtendente(null);
        atendimento.setTentativasErro(0);

        if (atendimento.getEtapaFluxo() == null || atendimento.getEtapaFluxo() != EtapaFluxo.COLETA_NOME) {
            atendimento.setEtapaFluxo(EtapaFluxo.MENU);
        }

        atendimentoWhatsappRepository.saveAndFlush(atendimento);

        historicoService.registrarEventoAtendimento(
                atendimento, TipoEventoHistorico.ATENDIMENTO_DEVOLVIDO_BOT,
                "Conversa devolvida ao bot por " + nome(responsavel), responsavel
        );

        List<PerguntaPendente> ativas = perguntasApresentaveis(atendimento);
        WhatsappContato contato = whatsappContatoService.travar(atendimento.getEmpresa(), atendimento.getTelefone());

        contato.limparEscolha();

        if (ativas.size() == 1) {

            whatsappSaidaService.enfileirar(NovaMensagem.doBot(atendimento, ativas.get(0).getTextoPergunta())
                    .comPergunta(ativas.get(0)));

        } else if (ativas.size() > 1) {

            StringBuilder texto = new StringBuilder("Você tem ").append(Math.min(ativas.size(), 8))
                    .append(" assuntos em aberto com a gente:\n");

            List<PerguntaPendente> lista = ativas.size() > 8 ? ativas.subList(0, 8) : ativas;

            for (int i = 0; i < lista.size(); i++) {
                texto.append('\n').append(i + 1).append(" - ").append(lista.get(i).getResumo());
            }

            texto.append("\n\nResponda com o número do assunto que você quer tratar agora (ou 9 para falar com um atendente).");

            contato.definirEscolha(lista.stream().map(PerguntaPendente::getId).toList());

            whatsappSaidaService.enfileirar(NovaMensagem.doBot(atendimento, texto.toString()));
        }

        return resposta(atendimento);
    }

    @Transactional
    public AtendimentoWhatsappResponse vincularCliente(
            Long empresaId, Long atendimentoId, Long clienteId, Usuario responsavel) {

        AtendimentoWhatsapp atendimento = buscarComContatoTravado(empresaId, atendimentoId);

        Cliente cliente = clienteRepository.findByIdAndEmpresaId(clienteId, empresaId)
                .orElseThrow(() -> new ClienteNaoEncontradoException("Cliente não encontrado"));

        atendimento.setCliente(cliente);
        atendimentoWhatsappRepository.saveAndFlush(atendimento);

        if (cliente.getWhatsapp() == null || cliente.getWhatsapp().isBlank()) {
            cliente.setWhatsapp(atendimento.getTelefone());
            clienteRepository.save(cliente);
        }

        historicoService.registrarEventoAtendimento(
                atendimento, TipoEventoHistorico.ATENDIMENTO_ASSUMIDO,
                "Conversa vinculada ao cliente " + cliente.getNome() + " por " + nome(responsavel), responsavel
        );

        return resposta(atendimento);
    }

    @Scheduled(fixedDelayString = "PT30M", initialDelayString = "PT5M")
    public void encerrarConversasInativas() {

        LocalDateTime limite = LocalDateTime.now(clock).minusHours(conversaWhatsappService.getHorasInatividade());

        List<Long> ids = transactionTemplate.execute(tx -> atendimentoWhatsappRepository
                .findTop200ByStatusAndAtualizadoEmBefore(StatusAtendimento.EM_FLUXO_BOT, limite)
                .stream().map(AtendimentoWhatsapp::getId).toList());

        if (ids == null) {
            return;
        }

        for (Long id : ids) {

            try {

                transactionTemplate.executeWithoutResult(tx -> atendimentoWhatsappRepository.findById(id).ifPresent(a -> {

                    whatsappContatoService.travar(a.getEmpresa(), a.getTelefone());

                    AtendimentoWhatsapp atual = atendimentoWhatsappRepository.findById(id).orElse(a);

                    if (atual.getStatus() == StatusAtendimento.EM_FLUXO_BOT
                            && atual.getAtualizadoEm() != null && atual.getAtualizadoEm().isBefore(limite)) {

                        conversaWhatsappService.encerrar(atual, ConversaWhatsappService.MOTIVO_INATIVIDADE, null,
                                "Conversa com o bot encerrada automaticamente por inatividade");
                    }
                }));

            } catch (RuntimeException ex) {

                log.warn("Falha ao encerrar a conversa inativa {}", id, ex);
            }
        }
    }

    @Scheduled(fixedDelayString = "${vidratx.whatsapp.aviso-espera.intervalo:PT5M}", initialDelayString = "PT1M")
    public void avisarClientesEsperando() {

        LocalDateTime agora = LocalDateTime.now(clock);

        List<ConversaDoContato> candidatas = transactionTemplate.execute(tx -> atendimentoWhatsappRepository
                .listarEsperandoDesdeAntesDe(StatusAtendimento.AGUARDANDO_ATENDENTE, agora.minusMinutes(PRAZO_MINIMO_MINUTOS))
                .stream()
                .filter(a -> precisaDeAvisoDeEspera(a, agora))
                .limit(200)
                .map(ConversaDoContato::de)
                .toList());

        if (candidatas == null) {
            return;
        }

        for (ConversaDoContato candidata : candidatas) {

            try {

                transactionTemplate.executeWithoutResult(tx -> {

                    whatsappContatoService.travarExistente(candidata.empresaId(), candidata.telefone());

                    atendimentoWhatsappRepository.findById(candidata.id())
                            .filter(a -> a.getStatus() == StatusAtendimento.AGUARDANDO_ATENDENTE)
                            .filter(a -> precisaDeAvisoDeEspera(a, LocalDateTime.now(clock)))
                            .ifPresent(this::enviarAvisoDeEspera);
                });

            } catch (RuntimeException ex) {

                log.warn("Falha ao avisar o cliente da conversa {} sobre a espera", candidata.id(), ex);
            }
        }
    }

    private boolean precisaDeAvisoDeEspera(AtendimentoWhatsapp atendimento, LocalDateTime agora) {

        return atendimento.respostaAtrasada(agora)
                && !atendimento.avisoDeEsperaJaEnviado()
                && WhatsappSaidaDespachante.dentroDoHorario(atendimento.getEmpresa(), fuso.agora(atendimento.getEmpresa()));
    }

    private void enviarAvisoDeEspera(AtendimentoWhatsapp atendimento) {

        atendimento.setAvisoEsperaEnviadoEm(LocalDateTime.now(clock));
        atendimentoWhatsappRepository.save(atendimento);

        whatsappSaidaService.enfileirar(NovaMensagem.doBot(atendimento, AVISO_ESPERA)
                .comCategoria(CategoriaMensagemSaida.ESCALONAMENTO));

        historicoService.registrarEventoAtendimento(
                atendimento, TipoEventoHistorico.ATENDIMENTO_ESCALADO_ATENDENTE,
                "Cliente esperando há " + AtendimentoWhatsappMapper.descreverEspera(
                        atendimento.minutosEsperandoResposta(LocalDateTime.now(clock)))
                        + " (prazo de " + atendimento.prazoRespostaMinutos() + " min): aviso de demora enviado",
                null
        );
    }

    @Scheduled(fixedDelayString = "PT30M", initialDelayString = "PT7M")
    public void encerrarConversasHumanasInativas() {

        LocalDateTime limite = LocalDateTime.now(clock).minusHours(conversaWhatsappService.getHorasInatividadeHumano());

        List<ConversaDoContato> candidatas = transactionTemplate.execute(tx -> atendimentoWhatsappRepository
                .findTop200ByStatusInAndAtualizadoEmBefore(COM_PESSOA, limite)
                .stream()
                .filter(a -> !a.clienteAguardandoResposta())
                .map(ConversaDoContato::de)
                .toList());

        if (candidatas == null) {
            return;
        }

        for (ConversaDoContato candidata : candidatas) {

            try {

                transactionTemplate.executeWithoutResult(tx -> {

                    whatsappContatoService.travarExistente(candidata.empresaId(), candidata.telefone());

                    atendimentoWhatsappRepository.findById(candidata.id())
                            .filter(conversaWhatsappService::humanaInativa)
                            .ifPresent(a -> conversaWhatsappService.encerrar(a, ConversaWhatsappService.MOTIVO_INATIVIDADE,
                                    null, "Conversa com atendente encerrada automaticamente: sem mensagens há mais de "
                                            + conversaWhatsappService.getHorasInatividadeHumano()
                                            + " h e o cliente não estava esperando resposta"));
                });

            } catch (RuntimeException ex) {

                log.warn("Falha ao encerrar a conversa com atendente inativa {}", candidata.id(), ex);
            }
        }
    }

    private record ConversaDoContato(Long id, Long empresaId, String telefone) {

        static ConversaDoContato de(AtendimentoWhatsapp atendimento) {
            return new ConversaDoContato(atendimento.getId(), atendimento.getEmpresa().getId(), atendimento.getTelefone());
        }
    }

    private AtendimentoWhatsappResponse resposta(AtendimentoWhatsapp atendimento) {
        return atendimentoWhatsappMapper.toResponse(atendimento, LocalDateTime.now(clock));
    }

    private void assumirInterno(AtendimentoWhatsapp atendimento, Usuario atendente, String descricao) {

        atendimento.setStatus(StatusAtendimento.EM_ATENDIMENTO_HUMANO);
        atendimento.setAtendente(atendente);
        atendimento.setTentativasErro(0);

        atendimentoWhatsappRepository.saveAndFlush(atendimento);

        historicoService.registrarEventoAtendimento(
                atendimento, TipoEventoHistorico.ATENDIMENTO_ASSUMIDO, descricao, atendente
        );
    }

    private List<String> pendencias(AtendimentoWhatsapp atendimento) {
        return perguntasApresentaveis(atendimento).stream().map(PerguntaPendente::getResumo).toList();
    }

    private List<PerguntaPendente> perguntasApresentaveis(AtendimentoWhatsapp atendimento) {

        return perguntaPendenteService
                .ativas(atendimento.getEmpresa().getId(), atendimento.getTelefone())
                .stream()
                .filter(p -> !silenciosa(p))
                .toList();
    }

    private boolean silenciosa(PerguntaPendente pergunta) {

        if (!pergunta.getTipo().ehSugestaoDeData()) {
            return false;
        }

        StatusAgendamento status = switch (pergunta.getTipo().getReferencia()) {
            case MEDICAO -> medicaoRepository.findById(pergunta.getReferenciaId()).map(m -> m.getStatus()).orElse(null);
            case INSTALACAO -> instalacaoRepository.findById(pergunta.getReferenciaId()).map(i -> i.getStatus()).orElse(null);
            case ORCAMENTO -> null;
        };

        return status == null || status == StatusAgendamento.CONTRAPROPOSTA_CLIENTE;
    }

    private AtendimentoWhatsapp buscarComContatoTravado(Long empresaId, Long atendimentoId) {

        AtendimentoWhatsapp atendimento = buscar(empresaId, atendimentoId);

        whatsappContatoService.travar(atendimento.getEmpresa(), atendimento.getTelefone());

        return atendimento;
    }

    private boolean mesmoUsuario(Usuario a, Usuario b) {
        return a != null && b != null && a.getId().equals(b.getId());
    }

    private String nome(Usuario usuario) {
        return usuario != null ? usuario.getNome() : "outra pessoa";
    }

    private AtendimentoWhatsapp buscar(Long empresaId, Long atendimentoId) {

        return atendimentoWhatsappRepository
                .findByIdAndEmpresaId(atendimentoId, empresaId)
                .orElseThrow(() -> new AtendimentoNaoEncontradoException("Atendimento não encontrado"));
    }
}
