package br.com.vidratx.service;

import br.com.vidratx.entity.Empresa;
import br.com.vidratx.entity.MensagemSaida;
import br.com.vidratx.entity.WhatsappInstancia;
import br.com.vidratx.enums.CategoriaMensagemSaida;
import br.com.vidratx.enums.StatusInstanciaWhatsapp;
import br.com.vidratx.enums.StatusMensagemSaida;
import br.com.vidratx.repository.MensagemAtendimentoRepository;
import br.com.vidratx.repository.MensagemSaidaRepository;
import br.com.vidratx.repository.WhatsappInstanciaRepository;
import br.com.vidratx.service.WhatsappGatewayClient.ResultadoEnvio;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InOrder;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WhatsappSaidaDespachanteTest {

    private static final String TELEFONE = "5511988887777";

    private MensagemSaidaRepository mensagemSaidaRepository;
    private WhatsappGatewayClient gateway;
    private WhatsappSaidaDespachante despachante;
    private Empresa empresa;

    @BeforeEach
    void montar() {

        mensagemSaidaRepository = mock(MensagemSaidaRepository.class);
        gateway = mock(WhatsappGatewayClient.class);

        WhatsappInstanciaRepository instancias = mock(WhatsappInstanciaRepository.class);

        empresa = new Empresa();
        ReflectionTestUtils.setField(empresa, "id", 7L);

        WhatsappInstancia instancia = new WhatsappInstancia();
        instancia.setStatus(StatusInstanciaWhatsapp.CONECTADO);
        instancia.setWebhookToken("token-da-empresa");

        when(instancias.findByEmpresaId(7L)).thenReturn(Optional.of(instancia));

        despachante = new WhatsappSaidaDespachante(
                mensagemSaidaRepository,
                mock(MensagemAtendimentoRepository.class),
                instancias,
                gateway,
                mock(HistoricoService.class),
                mock(PlatformTransactionManager.class),
                Clock.fixed(Instant.parse("2026-09-23T15:00:00Z"), ZoneId.of("America/Sao_Paulo")),
                6,
                48
        );
    }

    private void naFila(long id, String conteudo) {

        MensagemSaida saida = new MensagemSaida();
        ReflectionTestUtils.setField(saida, "id", id);
        saida.setEmpresa(empresa);
        saida.setTelefone(TELEFONE);
        saida.setConteudo(conteudo);
        saida.setCategoria(CategoriaMensagemSaida.BOT);
        saida.setStatus(StatusMensagemSaida.ENVIANDO);

        when(mensagemSaidaRepository.findById(id)).thenReturn(Optional.of(saida));
        when(mensagemSaidaRepository.reivindicar(eq(id), any(), any())).thenReturn(1);
    }

    @Test
    void mensagemComAnteriorDoMesmoContatoNaFrenteEspera() {

        naFila(2L, "Olá! Como posso ajudar?");
        when(mensagemSaidaRepository.contarAnterioresNaFrente(eq(2L), any())).thenReturn(1L);

        despachante.despachar(2L);

        verify(mensagemSaidaRepository, never()).reivindicar(eq(2L), any(), any());
        verify(gateway, never()).enviarMensagem(anyString(), anyString(), anyString(), anyString());
    }

    @Test
    void aoTerminarUmEnvioDespachaAProximaDoContatoNaOrdem() {

        naFila(1L, "Podemos agendar a medição para sexta às 14h?");
        naFila(2L, "Olá! Como posso ajudar?");

        when(mensagemSaidaRepository.contarAnterioresNaFrente(any(), any())).thenReturn(0L);
        when(mensagemSaidaRepository.proximasDoContato(eq(1L), any())).thenReturn(List.of(2L));
        when(mensagemSaidaRepository.proximasDoContato(eq(2L), any())).thenReturn(List.of());
        when(gateway.enviarMensagem(anyString(), anyString(), anyString(), anyString()))
                .thenReturn(ResultadoEnvio.enviada("WA-1"), ResultadoEnvio.enviada("WA-2"));

        despachante.despachar(1L);

        InOrder ordem = inOrder(gateway);
        ordem.verify(gateway).enviarMensagem(eq("token-da-empresa"), eq(TELEFONE),
                eq("Podemos agendar a medição para sexta às 14h?"), eq("saida-1"));
        ordem.verify(gateway).enviarMensagem(eq("token-da-empresa"), eq(TELEFONE),
                eq("Olá! Como posso ajudar?"), eq("saida-2"));
    }
}
