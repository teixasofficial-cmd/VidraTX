package br.com.vidratx.repository;

import br.com.vidratx.entity.WhatsappInstancia;
import br.com.vidratx.enums.StatusInstanciaWhatsapp;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface WhatsappInstanciaRepository
        extends JpaRepository<WhatsappInstancia, Long> {

    Optional<WhatsappInstancia> findByEmpresaId(
            Long empresaId
    );

    Optional<WhatsappInstancia> findByWebhookToken(
            String webhookToken
    );

    List<WhatsappInstancia> findAllByStatusNotAndDesconectadoEmBeforeAndAlertaDesconexaoEmIsNullAndEmpresaAtivaTrue(
            StatusInstanciaWhatsapp status,
            LocalDateTime limite
    );

    List<WhatsappInstancia> findAllByStatusAndAlertaDesconexaoEmIsNotNull(
            StatusInstanciaWhatsapp status
    );

    long countByStatusNotAndDesconectadoEmIsNotNullAndEmpresaAtivaTrue(
            StatusInstanciaWhatsapp status
    );
}
