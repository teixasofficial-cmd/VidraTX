package br.com.vidratx.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.Map;

@Service
public class AlertaOperacionalService {

    private static final Logger log =
            LoggerFactory.getLogger(AlertaOperacionalService.class);

    private final String webhookUrl;
    private final RestClient restClient;

    public AlertaOperacionalService(@Value("${vidratx.alerta.webhook-url:}") String webhookUrl) {

        this.webhookUrl = webhookUrl == null ? "" : webhookUrl.trim();

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .version(HttpClient.Version.HTTP_1_1)
                .build();

        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(httpClient);
        requestFactory.setReadTimeout(Duration.ofSeconds(5));

        this.restClient = RestClient.builder()
                .requestFactory(requestFactory)
                .build();
    }

    public void enviar(String mensagem) {

        log.warn("Alerta operacional: {}", mensagem);

        if (webhookUrl.isEmpty()) {
            return;
        }

        try {

            restClient.post()
                    .uri(webhookUrl)
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(Map.of("text", mensagem, "content", mensagem))
                    .retrieve()
                    .toBodilessEntity();

        } catch (RestClientException ex) {

            log.error("Não foi possível entregar o alerta ao webhook configurado: {}", ex.getMessage());
        }
    }
}
