package br.com.vidratx.util;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TelefoneUtilsTest {

    @ParameterizedTest
    @ValueSource(strings = {
            "(11) 90795-7722", "11907957722", "+55 11 90795-7722", "5511907957722", "0055 11 90795-7722",
            "011 90795-7722", "55 11 9 0795 7722"
    })
    void todasAsGrafiasViramOMesmoNumeroE164(String bruto) {
        assertEquals("5511907957722", TelefoneUtils.normalizar(bruto));
    }

    @Test
    void celularAntigoSemNonoDigitoGanhaONonoDigito() {
        assertEquals("5511987654321", TelefoneUtils.normalizar("551187654321"));
        assertEquals("5511987654321", TelefoneUtils.normalizar("(11) 8765-4321"));
    }

    @Test
    void telefoneFixoNaoGanhaNonoDigito() {
        assertEquals("551133334444", TelefoneUtils.normalizar("(11) 3333-4444"));
    }

    @Test
    void variantesIncluemAFormaSemNonoDigito() {

        assertTrue(TelefoneUtils.variantes("5511987654321").contains("551187654321"));
        assertTrue(TelefoneUtils.variantes("551187654321").contains("5511987654321"));
    }

    @Test
    void validacao() {

        assertTrue(TelefoneUtils.pareceValido("5511907957722"));
        assertTrue(TelefoneUtils.pareceValido("551133334444"));
        assertFalse(TelefoneUtils.pareceValido("5511"));
        assertNull(TelefoneUtils.normalizar("abc"));
    }
}
