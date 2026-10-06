package br.com.vidratx.service;

import br.com.vidratx.entity.Empresa;
import br.com.vidratx.entity.WhatsappInstancia;
import br.com.vidratx.enums.StatusInstanciaWhatsapp;
import br.com.vidratx.repository.WhatsappInstanciaRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MonitoramentoWhatsappServiceTest {

    private static final ZoneId FUSO = ZoneId.of("America/Sao_Paulo");
    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 9, 1, 12, 0);

    private WhatsappInstanciaRepository repository;
    private AlertaOperacionalService alertas;
    private MonitoramentoWhatsappService monitoramento;

    @BeforeEach
    void setUp() {
        repository = mock(WhatsappInstanciaRepository.class);
        alertas = mock(AlertaOperacionalService.class);
        Clock clock = Clock.fixed(AGORA.atZone(FUSO).toInstant(), FUSO);
        monitoramento = new MonitoramentoWhatsappService(
                repository, alertas, mock(PlatformTransactionManager.class), clock, 30);
    }

    private static WhatsappInstancia instancia(String nome, StatusInstanciaWhatsapp status) {
        Empresa empresa = new Empresa();
        empresa.setNomeFantasia(nome);
        empresa.setSlug(nome.toLowerCase().replace(' ', '-'));
        WhatsappInstancia instancia = new WhatsappInstancia();
        instancia.setEmpresa(empresa);
        instancia.alterarStatus(StatusInstanciaWhatsapp.CONECTADO, AGORA.minusHours(3));
        instancia.alterarStatus(status, AGORA.minusMinutes(45));
        return instancia;
    }

    @Test
    void quedaAlemDoLimiteGeraUmAlertaEMarcaAQueda() {

        WhatsappInstancia caida = instancia("Vidracaria Sol", StatusInstanciaWhatsapp.DESCONECTADO);

        when(repository.findAllByStatusNotAndDesconectadoEmBeforeAndAlertaDesconexaoEmIsNullAndEmpresaAtivaTrue(
                StatusInstanciaWhatsapp.CONECTADO, AGORA.minusMinutes(30))).thenReturn(List.of(caida));

        monitoramento.verificarConexoes();

        assertEquals(AGORA, caida.getAlertaDesconexaoEm());
        ArgumentCaptor<String> mensagem = ArgumentCaptor.forClass(String.class);
        verify(alertas).enviar(mensagem.capture());
        assertTrue(mensagem.getValue().contains("Vidracaria Sol (vidracaria-sol) desconectado desde 01/09 às 11:15"),
                mensagem.getValue());
    }

    @Test
    void semQuedaAlemDoLimiteNadaEEnviado() {

        monitoramento.verificarConexoes();

        verify(alertas, never()).enviar(any());
    }

    @Test
    void whatsappAvisadoQueVoltouGeraAvisoDeRetornoELimpaOAlerta() {

        WhatsappInstancia voltou = instancia("Vidracaria Lua", StatusInstanciaWhatsapp.CONECTADO);
        voltou.setAlertaDesconexaoEm(AGORA.minusMinutes(10));

        when(repository.findAllByStatusAndAlertaDesconexaoEmIsNotNull(StatusInstanciaWhatsapp.CONECTADO))
                .thenReturn(List.of(voltou));

        monitoramento.verificarConexoes();

        assertNull(voltou.getAlertaDesconexaoEm());
        verify(repository, times(1)).save(voltou);
        verify(alertas).enviar(eq("WhatsApp de Vidracaria Lua (vidracaria-lua) conectado de novo."));
    }
}
