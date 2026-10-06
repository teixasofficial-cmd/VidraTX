package br.com.vidratx.service;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AlertaOperacionalServiceTest {

    private HttpServer servidor;
    private final List<String> recebidos = new ArrayList<>();
    private final List<String> tiposDeConteudo = new ArrayList<>();

    @BeforeEach
    void subirServidor() throws IOException {
        servidor = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
        servidor.createContext("/webhook", troca -> {
            tiposDeConteudo.add(troca.getRequestHeaders().getFirst("Content-Type"));
            recebidos.add(new String(troca.getRequestBody().readAllBytes(), StandardCharsets.UTF_8));
            troca.sendResponseHeaders(204, -1);
            troca.close();
        });
        servidor.start();
    }

    @AfterEach
    void derrubarServidor() {
        servidor.stop(0);
    }

    private String url() {
        return "http://127.0.0.1:" + servidor.getAddress().getPort() + "/webhook";
    }

    @Test
    void enviaAMensagemNosCamposDoSlackEDoDiscord() {

        new AlertaOperacionalService(url()).enviar("WhatsApp de Vidraçaria Sol desconectado");

        assertEquals(1, recebidos.size());
        assertTrue(tiposDeConteudo.get(0).startsWith("application/json"), tiposDeConteudo.get(0));
        assertTrue(recebidos.get(0).contains("\"text\":\"WhatsApp de Vidraçaria Sol desconectado\""), recebidos.get(0));
        assertTrue(recebidos.get(0).contains("\"content\":\"WhatsApp de Vidraçaria Sol desconectado\""), recebidos.get(0));
    }

    @Test
    void semWebhookConfiguradoNaoChamaNada() {

        new AlertaOperacionalService("").enviar("qualquer coisa");

        assertEquals(0, recebidos.size());
    }

    @Test
    void webhookForaDoArNaoDerrubaQuemChamou() {

        servidor.stop(0);

        assertDoesNotThrow(() -> new AlertaOperacionalService(url()).enviar("alerta"));
    }
}
