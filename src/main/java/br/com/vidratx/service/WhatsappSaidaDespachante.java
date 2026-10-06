package br.com.vidratx.service;

import br.com.vidratx.config.FusoEmpresa;
import br.com.vidratx.entity.Empresa;
import br.com.vidratx.entity.MensagemAtendimento;
import br.com.vidratx.entity.MensagemSaida;
import br.com.vidratx.entity.WhatsappInstancia;
import br.com.vidratx.enums.StatusInstanciaWhatsapp;
import br.com.vidratx.enums.StatusMensagemSaida;
import br.com.vidratx.enums.TipoEventoHistorico;
import br.com.vidratx.repository.MensagemAtendimentoRepository;
import br.com.vidratx.repository.MensagemSaidaRepository;
import br.com.vidratx.repository.WhatsappInstanciaRepository;
import br.com.vidratx.service.WhatsappGatewayClient.ResultadoEnvio;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.util.List;

@Component
public class WhatsappSaidaDespachante {

    private static final Logger log =
            LoggerFactory.getLogger(WhatsappSaidaDespachante.class);

    private static final long[] ESPERA_MINUTOS = {1, 2, 5, 15, 30, 60};

    private final MensagemSaidaRepository mensagemSaidaRepository;
    private final MensagemAtendimentoRepository mensagemAtendimentoRepository;
    private final WhatsappInstanciaRepository whatsappInstanciaRepository;
    private final WhatsappGatewayClient whatsappGatewayClient;
    private final HistoricoService historicoService;
    private final TransactionTemplate transactionTemplate;
    private final Clock clock;
    private final FusoEmpresa fuso;
    private final int maxTentativas;
    private final long validadeHoras;

    public WhatsappSaidaDespachante(
            MensagemSaidaRepository mensagemSaidaRepository,
            MensagemAtendimentoRepository mensagemAtendimentoRepository,
            WhatsappInstanciaRepository whatsappInstanciaRepository,
            WhatsappGatewayClient whatsappGatewayClient,
            HistoricoService historicoService,
            PlatformTransactionManager transactionManager,
            Clock clock,
            @Value("${vidratx.whatsapp.saida.max-tentativas:6}") int maxTentativas,
            @Value("${vidratx.whatsapp.saida.validade-horas:48}") long validadeHoras) {

        this.mensagemSaidaRepository = mensagemSaidaRepository;
        this.mensagemAtendimentoRepository = mensagemAtendimentoRepository;
        this.whatsappInstanciaRepository = whatsappInstanciaRepository;
        this.whatsappGatewayClient = whatsappGatewayClient;
        this.historicoService = historicoService;
        this.transactionTemplate = new TransactionTemplate(transactionManager);

        this.transactionTemplate.setPropagationBehavior(TransactionDefinition.PROPAGATION_REQUIRES_NEW);
        this.clock = clock;
        this.fuso = new FusoEmpresa(clock);
        this.maxTentativas = maxTentativas;
        this.validadeHoras = validadeHoras;
    }

    public void despacharAposCommit(Long saidaId) {

        if (TransactionSynchronizationManager.isSynchronizationActive()) {

            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    despacharComSeguranca(saidaId);
                }
            });

            return;
        }

        despacharComSeguranca(saidaId);
    }

    private void despacharComSeguranca(Long saidaId) {

        try {
            despachar(saidaId);
        } catch (RuntimeException ex) {
            log.error("Erro inesperado ao despachar a mensagem de saída {}", saidaId, ex);
        }
    }

    public void despachar(Long saidaId) {

        Long naFrente = transactionTemplate.execute(status ->
                mensagemSaidaRepository.contarAnterioresNaFrente(saidaId, LocalDateTime.now(clock)));

        if (naFrente == null || naFrente > 0) {
            return;
        }

        Boolean reivindicada = transactionTemplate.execute(status ->
                mensagemSaidaRepository.reivindicar(
                        saidaId, List.of(StatusMensagemSaida.PENDENTE), LocalDateTime.now(clock)
                ) == 1
        );

        if (!Boolean.TRUE.equals(reivindicada)) {
            return;
        }

        try {

            Preparo preparo = transactionTemplate.execute(status -> preparar(saidaId));

            if (preparo == null) {
                return;
            }

            ResultadoEnvio resultado = whatsappGatewayClient.enviarMensagem(
                    preparo.token(), preparo.telefone(), preparo.conteudo(), "saida-" + saidaId
            );

            transactionTemplate.executeWithoutResult(status -> registrarResultado(saidaId, resultado));

        } finally {
            despacharProximaDoContato(saidaId);
        }
    }

    private void despacharProximaDoContato(Long saidaId) {

        List<Long> proximas;

        try {
            proximas = transactionTemplate.execute(status ->
                    mensagemSaidaRepository.proximasDoContato(saidaId, LocalDateTime.now(clock)));
        } catch (RuntimeException ex) {
            log.warn("Não foi possível buscar a próxima mensagem do contato depois da {}", saidaId, ex);
            return;
        }

        if (proximas != null && !proximas.isEmpty()) {
            despacharComSeguranca(proximas.get(0));
        }
    }

    private record Preparo(String token, String telefone, String conteudo) {
    }

    private Preparo preparar(Long saidaId) {

        MensagemSaida saida = mensagemSaidaRepository.findById(saidaId).orElse(null);

        if (saida == null) {
            return null;
        }

        if (saida.getPergunta() != null && !saida.getPergunta().ativa()) {

            saida.setStatus(StatusMensagemSaida.CANCELADA);
            saida.setUltimoErro("Não enviada: a pergunta foi substituída ou encerrada antes do envio");
            mensagemSaidaRepository.save(saida);

            return null;
        }

        LocalDateTime agora = LocalDateTime.now(clock);

        if (saida.getPergunta() == null && saida.getCriadoEm() != null
                && saida.getCriadoEm().isBefore(agora.minusHours(validadeHoras))) {

            saida.setUltimoErro("Não enviada: ficou mais de " + validadeHoras
                    + " h na fila (WhatsApp desconectado) e pode estar desatualizada");
            marcarFalha(saida);
            mensagemSaidaRepository.save(saida);

            return null;
        }

        WhatsappInstancia instancia = whatsappInstanciaRepository
                .findByEmpresaId(saida.getEmpresa().getId())
                .orElse(null);

        if (instancia == null || instancia.getStatus() != StatusInstanciaWhatsapp.CONECTADO) {

            saida.setStatus(StatusMensagemSaida.PENDENTE);
            saida.setProximaTentativaEm(null);
            saida.setUltimoErro("WhatsApp da empresa desconectado — a mensagem sai quando a conexão voltar");
            mensagemSaidaRepository.save(saida);

            return null;
        }

        LocalDateTime agoraNaEmpresa = fuso.agora(saida.getEmpresa());

        if (saida.getCategoria().respeitaHorarioComercial() && !dentroDoHorario(saida.getEmpresa(), agoraNaEmpresa)) {

            saida.setStatus(StatusMensagemSaida.PENDENTE);
            saida.setProximaTentativaEm(fuso.noSistema(saida.getEmpresa(),
                    proximoHorarioPermitido(saida.getEmpresa(), agoraNaEmpresa)));
            saida.setUltimoErro(null);
            mensagemSaidaRepository.save(saida);

            return null;
        }

        return new Preparo(instancia.getWebhookToken(), saida.getTelefone(), saida.getConteudo());
    }

    private void registrarResultado(Long saidaId, ResultadoEnvio resultado) {

        MensagemSaida saida = mensagemSaidaRepository.findById(saidaId).orElseThrow();
        LocalDateTime agora = LocalDateTime.now(clock);

        switch (resultado.situacao()) {

            case ENVIADA -> {
                saida.setTentativas(saida.getTentativas() + 1);
                saida.setStatus(StatusMensagemSaida.ENVIADA);
                saida.setEnviadaEm(agora);
                saida.setWhatsappMensagemId(resultado.whatsappMensagemId());
                saida.setUltimoErro(null);
                saida.setProximaTentativaEm(null);

                if (resultado.whatsappMensagemId() != null) {
                    for (MensagemAtendimento registro : mensagemAtendimentoRepository.findAllByMensagemSaidaId(saidaId)) {
                        registro.setWhatsappMensagemId(resultado.whatsappMensagemId());
                        mensagemAtendimentoRepository.save(registro);
                    }
                }
            }

            case SEM_SESSAO -> {
                saida.setStatus(StatusMensagemSaida.PENDENTE);
                saida.setProximaTentativaEm(null);
                saida.setUltimoErro(resultado.erro());

                whatsappInstanciaRepository.findByEmpresaId(saida.getEmpresa().getId())
                        .filter(i -> i.getStatus() == StatusInstanciaWhatsapp.CONECTADO)
                        .ifPresent(i -> {
                            i.alterarStatus(StatusInstanciaWhatsapp.DESCONECTADO, LocalDateTime.now(clock));
                            i.setConectadoEm(null);
                            whatsappInstanciaRepository.save(i);
                        });
            }

            case FALHA_TEMPORARIA -> {
                int tentativas = saida.getTentativas() + 1;
                saida.setTentativas(tentativas);
                saida.setUltimoErro(resultado.erro());

                if (tentativas >= maxTentativas) {
                    marcarFalha(saida);
                } else {
                    saida.setStatus(StatusMensagemSaida.PENDENTE);
                    saida.setProximaTentativaEm(agora.plusMinutes(
                            ESPERA_MINUTOS[Math.min(tentativas - 1, ESPERA_MINUTOS.length - 1)]
                    ));
                }
            }

            case FALHA_PERMANENTE -> {
                saida.setTentativas(saida.getTentativas() + 1);
                saida.setUltimoErro(resultado.erro());
                marcarFalha(saida);
            }
        }

        mensagemSaidaRepository.save(saida);
    }

    private void marcarFalha(MensagemSaida saida) {

        saida.setStatus(StatusMensagemSaida.FALHOU);
        saida.setProximaTentativaEm(null);

        if (saida.getAtendimento() != null) {

            String trecho = saida.getConteudo().length() > 80
                    ? saida.getConteudo().substring(0, 80) + "…"
                    : saida.getConteudo();

            historicoService.registrarEventoAtendimento(
                    saida.getAtendimento(),
                    TipoEventoHistorico.MENSAGEM_NAO_ENTREGUE,
                    "Mensagem não entregue ao cliente (" + saida.getUltimoErro() + "): \"" + trecho + "\"",
                    null
            );
        }

        log.warn("Mensagem de saída {} marcada como FALHOU: {}", saida.getId(), saida.getUltimoErro());
    }

    @Scheduled(fixedDelayString = "PT30S", initialDelayString = "PT20S")
    public void reenviarPendentes() {

        LocalDateTime agora = LocalDateTime.now(clock);

        transactionTemplate.execute(status -> mensagemSaidaRepository.liberarPresas(agora.minusMinutes(5)));

        List<Long> ids = transactionTemplate.execute(status -> mensagemSaidaRepository.idsProntosParaEnvio(agora));

        if (ids == null) {
            return;
        }

        for (Long id : ids) {
            despacharComSeguranca(id);
        }
    }

    public void despacharPendentesDaEmpresa(Long empresaId) {

        List<Long> ids = transactionTemplate.execute(status -> mensagemSaidaRepository.idsPendentesDaEmpresa(empresaId));

        if (ids == null) {
            return;
        }

        for (Long id : ids) {
            despacharComSeguranca(id);
        }
    }

    static boolean dentroDoHorario(Empresa empresa, LocalDateTime agora) {

        if (agora.getDayOfWeek() == DayOfWeek.SUNDAY) {
            return false;
        }

        int hora = agora.getHour();

        return hora >= inicio(empresa) && hora < fim(empresa);
    }

    static LocalDateTime proximoHorarioPermitido(Empresa empresa, LocalDateTime agora) {

        LocalDateTime candidato = agora.getHour() < inicio(empresa)
                ? agora.toLocalDate().atTime(inicio(empresa), 0)
                : agora.toLocalDate().plusDays(1).atTime(inicio(empresa), 0);

        while (candidato.getDayOfWeek() == DayOfWeek.SUNDAY) {
            candidato = candidato.plusDays(1);
        }

        return candidato;
    }

    private static int inicio(Empresa empresa) {
        return empresa.getHoraInicioMensagens() != null ? empresa.getHoraInicioMensagens() : 8;
    }

    private static int fim(Empresa empresa) {
        return empresa.getHoraFimMensagens() != null ? empresa.getHoraFimMensagens() : 20;
    }
}
