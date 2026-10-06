package br.com.vidratx.config;

import br.com.vidratx.entity.Empresa;
import java.time.Clock;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

public class FusoEmpresa {

    private final Clock clock;

    public FusoEmpresa(Clock clock) {
        this.clock = clock;
    }

    public ZoneId zona(Empresa empresa) {

        if (empresa == null || empresa.getFusoHorario() == null || empresa.getFusoHorario().isBlank()) {
            return clock.getZone();
        }

        try {
            return ZoneId.of(empresa.getFusoHorario().trim());
        } catch (DateTimeException ex) {
            return clock.getZone();
        }
    }

    public LocalDateTime agora(Empresa empresa) {
        return LocalDateTime.now(clock.withZone(zona(empresa)));
    }

    public LocalDate hoje(Empresa empresa) {
        return LocalDate.now(clock.withZone(zona(empresa)));
    }

    public LocalDateTime noCalendarioDa(Empresa empresa, LocalDateTime instanteDoSistema) {

        if (instanteDoSistema == null) {
            return null;
        }

        return instanteDoSistema.atZone(clock.getZone()).withZoneSameInstant(zona(empresa)).toLocalDateTime();
    }

    public LocalDateTime noSistema(Empresa empresa, LocalDateTime doCalendario) {

        if (doCalendario == null) {
            return null;
        }

        return doCalendario.atZone(zona(empresa)).withZoneSameInstant(clock.getZone()).toLocalDateTime();
    }

    public static String validar(String fuso) {

        if (fuso == null || fuso.isBlank()) {
            return null;
        }

        try {
            return ZoneId.of(fuso.trim()).getId();
        } catch (DateTimeException ex) {
            throw new IllegalArgumentException("Fuso horário inválido: " + fuso);
        }
    }
}
