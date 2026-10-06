package br.com.vidratx.entity;

import br.com.vidratx.enums.StatusInstanciaWhatsapp;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class WhatsappInstanciaStatusTest {

    private static final LocalDateTime QUEDA = LocalDateTime.of(2026, 9, 1, 10, 0);

    @Test
    void perderUmaConexaoQueExistiaMarcaOInicioDaQueda() {

        WhatsappInstancia instancia = new WhatsappInstancia();
        instancia.alterarStatus(StatusInstanciaWhatsapp.CONECTADO, QUEDA.minusHours(1));

        instancia.alterarStatus(StatusInstanciaWhatsapp.DESCONECTADO, QUEDA);

        assertEquals(QUEDA, instancia.getDesconectadoEm());
    }

    @Test
    void tentarReconectarContinuaSendoAMesmaQueda() {

        WhatsappInstancia instancia = new WhatsappInstancia();
        instancia.alterarStatus(StatusInstanciaWhatsapp.CONECTADO, QUEDA.minusHours(1));
        instancia.alterarStatus(StatusInstanciaWhatsapp.DESCONECTADO, QUEDA);

        instancia.alterarStatus(StatusInstanciaWhatsapp.CONECTANDO, QUEDA.plusMinutes(10));

        assertEquals(QUEDA, instancia.getDesconectadoEm());
    }

    @Test
    void reconectarEncerraAQueda() {

        WhatsappInstancia instancia = new WhatsappInstancia();
        instancia.alterarStatus(StatusInstanciaWhatsapp.CONECTADO, QUEDA.minusHours(1));
        instancia.alterarStatus(StatusInstanciaWhatsapp.DESCONECTADO, QUEDA);

        instancia.alterarStatus(StatusInstanciaWhatsapp.CONECTADO, QUEDA.plusMinutes(40));

        assertNull(instancia.getDesconectadoEm());
    }

    @Test
    void empresaQueNuncaConectouNaoTemQueda() {

        WhatsappInstancia instancia = new WhatsappInstancia();

        instancia.alterarStatus(StatusInstanciaWhatsapp.CONECTANDO, QUEDA);
        instancia.alterarStatus(StatusInstanciaWhatsapp.DESCONECTADO, QUEDA.plusMinutes(5));

        assertNull(instancia.getDesconectadoEm());
    }
}
