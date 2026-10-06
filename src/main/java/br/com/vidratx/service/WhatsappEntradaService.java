package br.com.vidratx.service;

import br.com.vidratx.entity.WhatsappInstancia;
import br.com.vidratx.exception.EmpresaInativaException;
import br.com.vidratx.enums.StatusMensagemRecebida;
import br.com.vidratx.enums.StatusMensagemSaida;
import br.com.vidratx.enums.TipoMensagemRecebida;
import br.com.vidratx.repository.MensagemRecebidaRepository;
import br.com.vidratx.repository.MensagemSaidaRepository;
import br.com.vidratx.service.WhatsappInboxService.DadosRecebidos;
import br.com.vidratx.util.TelefoneUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class WhatsappEntradaService {

    private static final Logger log =
            LoggerFactory.getLogger(WhatsappEntradaService.class);

    private static final int TENTATIVAS_POR_CHAMADA = 3;

    private final WhatsappInstanciaService whatsappInstanciaService;
    private final WhatsappInboxService whatsappInboxService;
    private final WhatsappConversaService whatsappConversaService;
    private final WhatsappMidiaStorage whatsappMidiaStorage;
    private final MensagemRecebidaRepository mensagemRecebidaRepository;
    private final MensagemSaidaRepository mensagemSaidaRepository;
    private final TransactionTemplate transactionTemplate;
    private final Clock clock;

    public WhatsappEntradaService(
            WhatsappInstanciaService whatsappInstanciaService,
            WhatsappInboxService whatsappInboxService,
            WhatsappConversaService whatsappConversaService,
            WhatsappMidiaStorage whatsappMidiaStorage,
            MensagemRecebidaRepository mensagemRecebidaRepository,
            MensagemSaidaRepository mensagemSaidaRepository,
            PlatformTransactionManager transactionManager,
            Clock clock) {

        this.whatsappInstanciaService = whatsappInstanciaService;
        this.whatsappInboxService = whatsappInboxService;
        this.whatsappConversaService = whatsappConversaService;
        this.whatsappMidiaStorage = whatsappMidiaStorage;
        this.mensagemRecebidaRepository = mensagemRecebidaRepository;
        this.mensagemSaidaRepository = mensagemSaidaRepository;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.clock = clock;
    }

    public void receberTexto(
            String webhookToken,
            String telefoneBruto,
            String texto,
            String whatsappMensagemId,
            Long timestampEpochSegundos,
            String midiaNaoSuportada,
            String reacaoA,
            String editaMensagemId,
            String apagaMensagemId) {

        WhatsappInstancia instancia = instanciaDeEmpresaAtiva(webhookToken);

        boolean naoSuportada = midiaNaoSuportada != null && !midiaNaoSuportada.isBlank();
        String apaga = normalizarId(apagaMensagemId);

        if (apaga == null && !naoSuportada && (texto == null || texto.isBlank())) {
            return;
        }

        String conteudo = apaga != null
                ? "[mensagem apagada pelo cliente]"
                : naoSuportada ? descreverMidia(midiaNaoSuportada, texto) : texto;

        DadosRecebidos dados = new DadosRecebidos(
                telefoneCanonico(telefoneBruto),
                normalizarId(whatsappMensagemId),
                converterTimestamp(timestampEpochSegundos),
                naoSuportada && apaga == null ? TipoMensagemRecebida.NAO_SUPORTADA : TipoMensagemRecebida.TEXTO,
                conteudo,
                null,
                null,
                normalizarId(reacaoA),
                apaga == null ? normalizarId(editaMensagemId) : null,
                apaga
        );

        registrarEProcessar(instancia, dados, null);
    }

    public void receberMidia(
            String webhookToken,
            String telefoneBruto,
            MultipartFile arquivo,
            String legenda,
            String whatsappMensagemId,
            Long timestampEpochSegundos) {

        if (arquivo == null || arquivo.isEmpty()) {
            throw new IllegalArgumentException("Arquivo de mídia é obrigatório");
        }

        String contentType = arquivo.getContentType();

        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("Apenas imagens são aceitas nesta versão");
        }

        WhatsappInstancia instancia = instanciaDeEmpresaAtiva(webhookToken);

        String id = normalizarId(whatsappMensagemId);

        if (id != null && mensagemRecebidaRepository
                .findByEmpresaIdAndWhatsappMensagemId(instancia.getEmpresa().getId(), id).isPresent()) {
            return;
        }

        String caminho = whatsappMidiaStorage.salvar(instancia.getEmpresa().getId(), arquivo);

        DadosRecebidos dados = DadosRecebidos.simples(
                telefoneCanonico(telefoneBruto),
                id,
                converterTimestamp(timestampEpochSegundos),
                TipoMensagemRecebida.IMAGEM,
                legenda != null && !legenda.isBlank() ? legenda.trim() : null,
                caminho,
                contentType
        );

        registrarEProcessar(instancia, dados, caminho);
    }

    private WhatsappInstancia instanciaDeEmpresaAtiva(String webhookToken) {

        WhatsappInstancia instancia = whatsappInstanciaService.buscarPorTokenOuFalhar(webhookToken);

        if (!Boolean.TRUE.equals(instancia.getEmpresa().getAtiva())) {
            throw new EmpresaInativaException("A empresa está inativa");
        }

        return instancia;
    }

    private void registrarEProcessar(WhatsappInstancia instancia, DadosRecebidos dados, String arquivoGravado) {

        Optional<Long> id;

        try {

            id = whatsappInboxService.registrar(instancia, dados);

        } catch (DataIntegrityViolationException ex) {

            id = Optional.empty();
        }

        if (id.isEmpty()) {

            log.info("Mensagem {} de {} já recebida antes — reentrega descartada",
                    dados.whatsappMensagemId(), dados.telefone());

            if (arquivoGravado != null) {
                whatsappMidiaStorage.excluir(arquivoGravado);
            }

            return;
        }

        processarComRetentativa(id.get());
    }

    public void processarComRetentativa(Long mensagemId) {

        for (int tentativa = 1; tentativa <= TENTATIVAS_POR_CHAMADA; tentativa++) {

            try {

                whatsappConversaService.processar(mensagemId);
                return;

            } catch (ConcurrencyFailureException ex) {

                log.info("Conflito de concorrência ao processar a mensagem {} (tentativa {}/{})",
                        mensagemId, tentativa, TENTATIVAS_POR_CHAMADA);

                if (tentativa == TENTATIVAS_POR_CHAMADA) {

                    if (whatsappInboxService.registrarFalha(mensagemId, ex.getMessage(), false)) {
                        escalarComSeguranca(mensagemId);
                    }

                    return;
                }

                aguardar(150L * tentativa);

            } catch (RuntimeException ex) {

                log.error("Falha ao processar a mensagem recebida {}", mensagemId, ex);

                whatsappInboxService.registrarFalha(mensagemId, ex.toString(), true);
                escalarComSeguranca(mensagemId);

                return;
            }
        }
    }

    private void escalarComSeguranca(Long mensagemId) {

        try {
            whatsappConversaService.escalarPorFalha(mensagemId);
        } catch (RuntimeException ex) {
            log.error("Não foi possível escalar a conversa da mensagem {} após falha", mensagemId, ex);
        }
    }

    @Scheduled(fixedDelayString = "PT30S", initialDelayString = "PT25S")
    public void reprocessarPendentes() {

        LocalDateTime limite = LocalDateTime.now(clock).minusSeconds(20);

        List<Long> ids = transactionTemplate.execute(status ->
                mensagemRecebidaRepository
                        .findTop50ByStatusInAndRecebidaEmBeforeAndTentativasLessThanOrderByIdAsc(
                                List.of(StatusMensagemRecebida.RECEBIDA), limite,
                                WhatsappInboxService.MAX_TENTATIVAS_PROCESSAMENTO
                        )
                        .stream()
                        .map(m -> m.getId())
                        .toList()
        );

        if (ids == null) {
            return;
        }

        for (Long id : ids) {
            processarComRetentativa(id);
        }
    }

    public void atualizarStatusEntrega(String webhookToken, String whatsappMensagemId, String status) {

        WhatsappInstancia instancia = whatsappInstanciaService.buscarPorTokenOuFalhar(webhookToken);

        if (whatsappMensagemId == null || status == null) {
            return;
        }

        transactionTemplate.executeWithoutResult(tx ->
                mensagemSaidaRepository
                        .findFirstByEmpresaIdAndWhatsappMensagemId(instancia.getEmpresa().getId(), whatsappMensagemId)
                        .ifPresent(saida -> {

                            LocalDateTime agora = LocalDateTime.now(clock);

                            switch (status.toUpperCase(Locale.ROOT)) {

                                case "ENTREGUE" -> {
                                    if (saida.getStatus() == StatusMensagemSaida.ENVIADA) {
                                        saida.setStatus(StatusMensagemSaida.ENTREGUE);
                                    }
                                    saida.setEntregueEm(agora);
                                }

                                case "LIDA" -> {
                                    saida.setStatus(StatusMensagemSaida.LIDA);
                                    saida.setLidaEm(agora);
                                    if (saida.getEntregueEm() == null) {
                                        saida.setEntregueEm(agora);
                                    }
                                }

                                case "ERRO" -> {
                                    saida.setStatus(StatusMensagemSaida.FALHOU);
                                    saida.setUltimoErro("O WhatsApp não conseguiu entregar a mensagem");
                                }

                                default -> {
                                    return;
                                }
                            }

                            mensagemSaidaRepository.save(saida);
                        })
        );
    }

    private String telefoneCanonico(String bruto) {

        String telefone = TelefoneUtils.normalizar(bruto);

        if (telefone == null) {
            throw new IllegalArgumentException("Telefone do remetente ausente");
        }

        return telefone;
    }

    private LocalDateTime converterTimestamp(Long epochSegundos) {

        if (epochSegundos == null || epochSegundos <= 0) {
            return null;
        }

        return LocalDateTime.ofInstant(Instant.ofEpochSecond(epochSegundos), clock.getZone());
    }

    private String normalizarId(String id) {
        return id == null || id.isBlank() ? null : id.trim();
    }

    private String descreverMidia(String tipo, String legenda) {

        String rotulo = switch (tipo.toUpperCase(Locale.ROOT)) {
            case "AUDIO" -> "[áudio]";
            case "VIDEO" -> "[vídeo]";
            case "FIGURINHA" -> "[figurinha]";
            case "DOCUMENTO" -> "[documento]";
            case "LOCALIZACAO" -> "[localização]";
            case "CONTATO" -> "[contato]";
            default -> "[mídia não suportada]";
        };

        return legenda != null && !legenda.isBlank() ? rotulo + " " + legenda.trim() : rotulo;
    }

    private void aguardar(long millis) {

        try {
            Thread.sleep(millis);
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        }
    }
}
