package br.com.vidratx.conversa;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExtratorDataTest {

    private static final LocalDateTime AGORA = LocalDateTime.of(2026, 9, 23, 10, 0);

    private static Optional<LocalDateTime> ler(String texto) {
        return ExtratorData.extrair(texto, AGORA);
    }

    @Test
    void leDiaEHoraUnicos() {

        assertEquals(LocalDateTime.of(2026, 9, 25, 14, 0), ler("sexta às 14h").orElseThrow());
        assertEquals(LocalDateTime.of(2026, 10, 15, 14, 30), ler("15/10 às 14:30").orElseThrow());
        assertEquals(LocalDateTime.of(2026, 9, 24, 9, 0), ler("amanhã às 9 da manhã").orElseThrow());
        assertEquals(LocalDateTime.of(2026, 10, 14, 9, 0), ler("dia 14/10 às 9h").orElseThrow());
        assertEquals(LocalDateTime.of(2026, 9, 25, 15, 0), ler("sexta, dia 25, às 15h").orElseThrow());
        assertEquals(LocalDateTime.of(2026, 9, 28, 15, 30), ler("segunda 15h30").orElseThrow());
    }

    @ParameterizedTest
    @ValueSource(strings = {
            "não posso segunda, só quarta às 10h",
            "dia 4 não dá, dia 5 às 15h",
            "amanhã às 14h não consigo, só às 17h",
            "15/10 ou 16/10 às 14h",
            "sexta entre 14h e 16h",
            "sexta às 14h" + "\n" + "ou sábado às 9h"
    })
    void comMaisDeUmaDataOuHorarioNaoAdivinha(String texto) {
        assertTrue(ler(texto).isEmpty(), texto);
    }

    @ParameterizedTest
    @ValueSource(strings = {"sexta", "às 14h", "semana que vem", "qualquer dia de manhã"})
    void semDiaEHoraNaoLeNada(String texto) {
        assertTrue(ler(texto).isEmpty(), texto);
    }
}
