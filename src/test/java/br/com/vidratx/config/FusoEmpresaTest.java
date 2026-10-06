package br.com.vidratx.config;

import br.com.vidratx.entity.Empresa;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class FusoEmpresaTest {

    private static final ZoneId BRASILIA = ZoneId.of("America/Sao_Paulo");

    private static final LocalDateTime AGORA_SISTEMA = LocalDateTime.of(2026, 9, 22, 23, 30);

    private final FusoEmpresa fuso = new FusoEmpresa(Clock.fixed(AGORA_SISTEMA.atZone(BRASILIA).toInstant(), BRASILIA));

    private static Empresa empresa(String fusoHorario) {
        Empresa empresa = new Empresa();
        empresa.setFusoHorario(fusoHorario);
        return empresa;
    }

    @Test
    void semFusoUsaOFusoDoSistema() {

        assertEquals(BRASILIA, fuso.zona(empresa(null)));
        assertEquals(BRASILIA, fuso.zona(empresa("  ")));
        assertEquals(BRASILIA, fuso.zona(null));
        assertEquals(AGORA_SISTEMA, fuso.agora(empresa(null)));
    }

    @Test
    void agoraEHojeSaoDoCalendarioDaEmpresa() {

        assertEquals(LocalDateTime.of(2026, 9, 22, 21, 30), fuso.agora(empresa("America/Rio_Branco")));
        assertEquals(LocalDate.of(2026, 9, 22), fuso.hoje(empresa("America/Rio_Branco")));

        assertEquals(LocalDateTime.of(2026, 9, 23, 0, 30), fuso.agora(empresa("America/Noronha")));
        assertEquals(LocalDate.of(2026, 9, 23), fuso.hoje(empresa("America/Noronha")));
    }

    @Test
    void converteEntreSistemaECalendario() {

        Empresa cuiaba = empresa("America/Cuiaba");

        LocalDateTime noveDeCuiaba = LocalDateTime.of(2026, 9, 24, 9, 0);

        assertEquals(LocalDateTime.of(2026, 9, 24, 10, 0), fuso.noSistema(cuiaba, noveDeCuiaba));
        assertEquals(noveDeCuiaba, fuso.noCalendarioDa(cuiaba, fuso.noSistema(cuiaba, noveDeCuiaba)));
        assertNull(fuso.noSistema(cuiaba, null));
        assertNull(fuso.noCalendarioDa(cuiaba, null));
    }

    @Test
    void fusoInvalidoGravadoNaoDerrubaOCalendario() {
        assertEquals(BRASILIA, fuso.zona(empresa("Marte/Olympus")));
    }

    @Test
    void validaOFusoAntesDeGravar() {

        assertEquals("America/Manaus", FusoEmpresa.validar(" America/Manaus "));
        assertNull(FusoEmpresa.validar(""));
        assertNull(FusoEmpresa.validar(null));
        assertThrows(IllegalArgumentException.class, () -> FusoEmpresa.validar("Marte/Olympus"));
    }
}
