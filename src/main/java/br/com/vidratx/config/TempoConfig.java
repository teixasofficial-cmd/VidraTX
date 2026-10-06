package br.com.vidratx.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;
import java.util.TimeZone;

@Configuration
public class TempoConfig {

    public static final String FUSO_PADRAO = "America/Sao_Paulo";

    public static ZoneId zona() {

        String configurado = System.getenv("VIDRATX_FUSO_HORARIO");

        return ZoneId.of(configurado == null || configurado.isBlank() ? FUSO_PADRAO : configurado.trim());
    }

    public static void fixarFusoPadrao() {
        TimeZone.setDefault(TimeZone.getTimeZone(zona()));
    }

    @Bean
    public Clock clock() {
        return Clock.system(zona());
    }
}
