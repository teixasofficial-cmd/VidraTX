package br.com.vidratx.service;

import br.com.vidratx.config.FusoEmpresa;
import br.com.vidratx.entity.Orcamento;
import br.com.vidratx.enums.StatusOrcamento;
import br.com.vidratx.repository.OrcamentoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrcamentoExpiracaoService {

    private static final Logger log =
            LoggerFactory.getLogger(OrcamentoExpiracaoService.class);

    private final OrcamentoRepository orcamentoRepository;
    private final OrcamentoService orcamentoService;
    private final TransactionTemplate transactionTemplate;
    private final Clock clock;
    private final FusoEmpresa fuso;
    private final long diasParaExpirar;

    public OrcamentoExpiracaoService(
            OrcamentoRepository orcamentoRepository,
            OrcamentoService orcamentoService,
            PlatformTransactionManager transactionManager,
            Clock clock,
            @Value("${vidratx.orcamento.dias-para-expirar:15}") long diasParaExpirar) {

        this.orcamentoRepository = orcamentoRepository;
        this.orcamentoService = orcamentoService;
        this.transactionTemplate = new TransactionTemplate(transactionManager);
        this.clock = clock;
        this.fuso = new FusoEmpresa(clock);
        this.diasParaExpirar = diasParaExpirar;
    }

    @Scheduled(fixedDelayString = "PT1H", initialDelayString = "PT2M")
    public void expirarOrcamentosPendentes() {
        processarExpiracoes(LocalDateTime.now(clock));
    }

    public int processarExpiracoes(LocalDateTime agora) {

        LocalDate hoje = agora.toLocalDate();

        List<Long> ids = transactionTemplate.execute(status -> {

            List<Long> encontrados = new ArrayList<>();

            orcamentoRepository.findAllByStatusAndValidoAteBefore(StatusOrcamento.ENVIADO, hoje.plusDays(1))
                    .forEach(o -> encontrados.add(o.getId()));

            orcamentoRepository.findAllByStatusAndValidoAteIsNullAndEnviadoEmBefore(
                            StatusOrcamento.ENVIADO, agora.minusDays(diasParaExpirar))
                    .forEach(o -> encontrados.add(o.getId()));

            return encontrados;
        });

        if (ids == null) {
            return 0;
        }

        int expirados = 0;

        for (Long id : ids) {

            try {

                Boolean expirou = transactionTemplate.execute(status -> {

                    Orcamento orcamento = orcamentoRepository.findById(id).orElse(null);

                    if (orcamento == null || orcamento.getStatus() != StatusOrcamento.ENVIADO) {
                        return false;
                    }

                    if (orcamento.getValidoAte() != null && !orcamento.getValidoAte()
                            .isBefore(fuso.noCalendarioDa(orcamento.getEmpresa(), agora).toLocalDate())) {
                        return false;
                    }

                    orcamentoService.expirarAutomaticamente(orcamento);

                    return orcamento.getStatus() == StatusOrcamento.EXPIRADO;
                });

                if (Boolean.TRUE.equals(expirou)) {
                    expirados++;
                }

            } catch (RuntimeException ex) {

                log.warn("Falha ao expirar automaticamente o orçamento {}", id, ex);
            }
        }

        return expirados;
    }
}
