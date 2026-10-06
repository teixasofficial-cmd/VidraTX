package br.com.vidratx.service;

import br.com.vidratx.entity.WhatsappInstancia;
import br.com.vidratx.enums.StatusInstanciaWhatsapp;
import br.com.vidratx.repository.WhatsappInstanciaRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
public class MonitoramentoWhatsappService {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd/MM 'às' HH:mm");

    private final WhatsappInstanciaRepository whatsappInstanciaRepository;
    private final AlertaOperacionalService alertaOperacionalService;
    private final TransactionTemplate transactionTemplate;
    private final Clock clock;
    private final Duration limite;

    public MonitoramentoWhatsappService(
            WhatsappInstanciaRepository whatsappInstanciaRepository,
            AlertaOperacionalService alertaOperacionalService,
            PlatformTransactionManager transactionManager,
            Clock clock,
            @Value("${vidratx.alerta.whatsapp-desconectado-minutos:30}") long minutosDesconectado) {

        this.whatsappInstanciaRepository = whatsappInstanciaRepository;
        this.alertaOperacionalService = alertaOperacionalService;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.clock = clock;
        this.limite = Duration.ofMinutes(minutosDesconectado);
    }

    @Scheduled(fixedDelayString = "PT5M", initialDelayString = "PT2M")
    public void verificarConexoes() {

        LocalDateTime agora = LocalDateTime.now(clock);

        List<String> avisos = transactionTemplate.execute(status -> {

            List<String> mensagens = new ArrayList<>();

            for (WhatsappInstancia instancia : whatsappInstanciaRepository
                    .findAllByStatusNotAndDesconectadoEmBeforeAndAlertaDesconexaoEmIsNullAndEmpresaAtivaTrue(
                            StatusInstanciaWhatsapp.CONECTADO, agora.minus(limite))) {

                instancia.setAlertaDesconexaoEm(agora);
                whatsappInstanciaRepository.save(instancia);

                mensagens.add("WhatsApp de " + empresa(instancia) + " desconectado desde "
                        + FORMATO.format(instancia.getDesconectadoEm())
                        + ". As mensagens para os clientes ficam na fila até reconectar"
                        + " (Configurações › WhatsApp, no painel da empresa).");
            }

            for (WhatsappInstancia instancia : whatsappInstanciaRepository
                    .findAllByStatusAndAlertaDesconexaoEmIsNotNull(StatusInstanciaWhatsapp.CONECTADO)) {

                instancia.setAlertaDesconexaoEm(null);
                whatsappInstanciaRepository.save(instancia);

                mensagens.add("WhatsApp de " + empresa(instancia) + " conectado de novo.");
            }

            return mensagens;
        });

        if (avisos != null) {
            avisos.forEach(alertaOperacionalService::enviar);
        }
    }

    private static String empresa(WhatsappInstancia instancia) {
        return instancia.getEmpresa().getNomeFantasia() + " (" + instancia.getEmpresa().getSlug() + ")";
    }
}
