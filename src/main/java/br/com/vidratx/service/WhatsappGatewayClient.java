package br.com.vidratx.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpStatusCodeException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;

@Component
public class WhatsappGatewayClient {

    private static final Logger log =
            LoggerFactory.getLogger(WhatsappGatewayClient.class);

    private final RestClient restClient;
    private final String gatewayToken;

    public WhatsappGatewayClient(
            @Value("${vidratx.whatsapp.gateway-url}") String gatewayUrl,
            @Value("${vidratx.whatsapp.gateway-token}") String gatewayToken,
            @Value("${vidratx.whatsapp.gateway-connect-timeout:PT3S}") Duration connectTimeout,
            @Value("${vidratx.whatsapp.gateway-read-timeout:PT15S}") Duration readTimeout) {

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(connectTimeout)
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(readTimeout);

        this.restClient =
                RestClient.builder()
                        .baseUrl(gatewayUrl)
                        .requestFactory(requestFactory)
                        .build();

        this.gatewayToken = gatewayToken;
    }

    public ResultadoEnvio enviarMensagem(
            String instanciaToken,
            String telefone,
            String mensagem,
            String chaveIdempotencia) {

        try {

            RespostaEnvio resposta = restClient.post()
                    .uri("/mensagens/enviar")
                    .contentType(MediaType.APPLICATION_JSON)
                    .header("Authorization", "Bearer " + gatewayToken)
                    .body(new EnvioMensagem(instanciaToken, telefone, mensagem, chaveIdempotencia))
                    .retrieve()
                    .body(RespostaEnvio.class);

            return ResultadoEnvio.enviada(resposta != null ? resposta.mensagemId() : null);

        } catch (HttpStatusCodeException ex) {

            return classificarErroHttp(ex.getStatusCode(), ex.getResponseBodyAsString(), telefone);

        } catch (RestClientException ex) {

            log.warn(
                    "Falha de comunicação com o whatsapp-service ao enviar para {}: {}",
                    telefone, ex.getMessage()
            );

            return ResultadoEnvio.falhaTemporaria("whatsapp-service indisponível: " + ex.getMessage());
        }
    }

    private ResultadoEnvio classificarErroHttp(HttpStatusCode status, String corpo, String telefone) {

        int codigo = status.value();

        log.warn("whatsapp-service respondeu {} ao enviar para {}: {}", codigo, telefone, corpo);

        if (codigo == 409) {
            return ResultadoEnvio.semSessao("WhatsApp da empresa sem sessão ativa");
        }

        if (codigo == 422) {
            return ResultadoEnvio.falhaPermanente("Este número não tem WhatsApp");
        }

        if (codigo == 400 || codigo == 401 || codigo == 403) {
            return ResultadoEnvio.falhaPermanente("Envio recusado pelo whatsapp-service (" + codigo + ")");
        }

        return ResultadoEnvio.falhaTemporaria("whatsapp-service respondeu " + codigo);
    }

    public void iniciarSessao(String instanciaToken) {

        try {

            restClient.post()
                    .uri("/sessoes/{token}/iniciar", instanciaToken)
                    .header("Authorization", "Bearer " + gatewayToken)
                    .retrieve()
                    .toBodilessEntity();

        } catch (RestClientException ex) {

            log.error(
                    "Falha ao solicitar início de sessão ao whatsapp-service",
                    ex
            );
        }
    }

    @SuppressWarnings("unchecked")
    public String buscarQr(String instanciaToken) {

        try {

            Map<String, Object> resposta = restClient.get()
                    .uri("/sessoes/{token}/qr", instanciaToken)
                    .header("Authorization", "Bearer " + gatewayToken)
                    .retrieve()
                    .body(Map.class);

            Object qr = resposta != null ? resposta.get("qr") : null;

            return qr != null ? qr.toString() : null;

        } catch (RestClientException ex) {

            log.warn("Não foi possível obter o QR code do whatsapp-service: {}", ex.getMessage());

            return null;
        }
    }

    private record EnvioMensagem(
            String instanciaToken,
            String telefone,
            String mensagem,
            String idempotencyKey) {
    }

    private record RespostaEnvio(String mensagemId) {
    }

    public record ResultadoEnvio(Situacao situacao, String whatsappMensagemId, String erro) {

        public enum Situacao {
            ENVIADA,
            FALHA_TEMPORARIA,
            FALHA_PERMANENTE,
            SEM_SESSAO
        }

        static ResultadoEnvio enviada(String whatsappMensagemId) {
            return new ResultadoEnvio(Situacao.ENVIADA, whatsappMensagemId, null);
        }

        static ResultadoEnvio falhaTemporaria(String erro) {
            return new ResultadoEnvio(Situacao.FALHA_TEMPORARIA, null, erro);
        }

        static ResultadoEnvio falhaPermanente(String erro) {
            return new ResultadoEnvio(Situacao.FALHA_PERMANENTE, null, erro);
        }

        static ResultadoEnvio semSessao(String erro) {
            return new ResultadoEnvio(Situacao.SEM_SESSAO, null, erro);
        }
    }
}
