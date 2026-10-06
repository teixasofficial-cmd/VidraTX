package br.com.vidratx.service;

import br.com.vidratx.entity.Orcamento;
import br.com.vidratx.entity.PerguntaPendente;
import br.com.vidratx.enums.CategoriaMensagemSaida;
import br.com.vidratx.enums.RemetenteMensagem;
import br.com.vidratx.enums.StatusOrcamento;
import br.com.vidratx.enums.TipoEventoHistorico;
import br.com.vidratx.enums.TipoPergunta;
import br.com.vidratx.repository.OrcamentoRepository;
import br.com.vidratx.service.WhatsappSaidaService.NovaMensagem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class OrcamentoFollowUpService {

    private static final Logger log =
            LoggerFactory.getLogger(OrcamentoFollowUpService.class);

    private static final DateTimeFormatter FORMATO_DATA =
            DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale.forLanguageTag("pt-BR"));

    private final OrcamentoRepository orcamentoRepository;
    private final PerguntaPendenteService perguntaPendenteService;
    private final WhatsappSaidaService whatsappSaidaService;
    private final WhatsappContatoService whatsappContatoService;
    private final HistoricoService historicoService;
    private final TransactionTemplate transactionTemplate;
    private final Clock clock;
    private final long horasSemResposta;

    public OrcamentoFollowUpService(
            OrcamentoRepository orcamentoRepository,
            PerguntaPendenteService perguntaPendenteService,
            WhatsappSaidaService whatsappSaidaService,
            WhatsappContatoService whatsappContatoService,
            HistoricoService historicoService,
            PlatformTransactionManager transactionManager,
            Clock clock,
            @Value("${vidratx.whatsapp.followup.horas-sem-resposta:24}") long horasSemResposta) {

        this.orcamentoRepository = orcamentoRepository;
        this.perguntaPendenteService = perguntaPendenteService;
        this.whatsappSaidaService = whatsappSaidaService;
        this.whatsappContatoService = whatsappContatoService;
        this.historicoService = historicoService;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.clock = clock;
        this.horasSemResposta = horasSemResposta;
    }

    @Scheduled(fixedDelayString = "PT30M", initialDelayString = "PT1M")
    public void enviarLembretesPendentes() {
        processarLembretes(LocalDateTime.now(clock));
    }

    public int processarLembretes(LocalDateTime agora) {

        LocalDateTime limite = agora.minusHours(horasSemResposta);

        List<Long> ids = transactionTemplate.execute(status ->
                orcamentoRepository
                        .findAllByStatusAndEnviadoEmBeforeAndLembreteEnviadoEmIsNull(StatusOrcamento.ENVIADO, limite)
                        .stream()
                        .map(Orcamento::getId)
                        .toList()
        );

        if (ids == null) {
            return 0;
        }

        int enviados = 0;

        for (Long id : ids) {

            try {

                Boolean enviado = transactionTemplate.execute(status -> enviarLembrete(id, limite));

                if (Boolean.TRUE.equals(enviado)) {
                    enviados++;
                }

            } catch (RuntimeException ex) {

                log.warn("Falha ao enviar o lembrete do orçamento {}", id, ex);
            }
        }

        return enviados;
    }

    private boolean enviarLembrete(Long orcamentoId, LocalDateTime limite) {

        Orcamento orcamento = orcamentoRepository.findById(orcamentoId).orElse(null);

        if (orcamento == null) {
            return false;
        }

        String telefone = NegociacaoAgendamentoService.telefoneDoCliente(orcamento.getCliente());

        if (telefone == null) {
            return false;
        }

        whatsappContatoService.travar(orcamento.getEmpresa(), telefone);

        if (Boolean.FALSE.equals(orcamento.getEmpresa().getLembreteOrcamentoAtivo())) {
            return false;
        }

        if (orcamento.getStatus() != StatusOrcamento.ENVIADO
                || orcamento.getLembreteEnviadoEm() != null
                || orcamento.getEnviadoEm() == null
                || !orcamento.getEnviadoEm().isBefore(limite)) {
            return false;
        }

        Optional<PerguntaPendente> pergunta = perguntaPendenteService
                .ativas(orcamento.getEmpresa().getId(), telefone)
                .stream()
                .filter(p -> p.getTipo() == TipoPergunta.APROVAR_ORCAMENTO
                        && p.getReferenciaId().equals(orcamento.getId())
                        && p.getVersao().equals(orcamento.getRevisaoEnvio()))
                .findFirst();

        if (pergunta.isEmpty()) {
            return false;
        }

        String texto = "Olá, " + primeiroNome(orcamento.getCliente().getNome())
                + "! Passando para saber se conseguiu analisar o orçamento nº " + orcamento.getId()
                + " (" + OrcamentoService.formatarValor(orcamento.getValorTotal())
                + (orcamento.getValidoAte() != null ? ", válido até " + orcamento.getValidoAte().format(FORMATO_DATA) : "")
                + ").\n\nResponda:\n1 - Aprovar\n2 - Pedir alteração\n3 - Falar com um atendente\n4 - Não tenho interesse";

        whatsappSaidaService.enfileirar(new NovaMensagem(
                orcamento.getEmpresa(), telefone, orcamento.getCliente(), texto, CategoriaMensagemSaida.LEMBRETE,
                RemetenteMensagem.BOT, "ORCAMENTO", orcamento.getId(), pergunta.get(), null
        ));

        orcamento.setLembreteEnviadoEm(LocalDateTime.now(clock));
        orcamentoRepository.save(orcamento);

        historicoService.registrarEventoOrcamento(
                orcamento, TipoEventoHistorico.ORCAMENTO_ALTERADO,
                "Lembrete automático enviado ao cliente (sem resposta há mais de " + horasSemResposta + "h)", null
        );

        log.info("Lembrete de follow-up enfileirado para o orçamento {}", orcamento.getId());

        return true;
    }

    private String primeiroNome(String nomeCompleto) {
        return nomeCompleto == null || nomeCompleto.isBlank() ? "" : nomeCompleto.trim().split("\\s+")[0];
    }
}
