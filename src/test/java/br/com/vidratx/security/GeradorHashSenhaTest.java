package br.com.vidratx.security;

import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class GeradorHashSenhaTest {

    private final ByteArrayOutputStream saida = new ByteArrayOutputStream();
    private final ByteArrayOutputStream erro = new ByteArrayOutputStream();

    private int executar(String entrada) {
        return GeradorHashSenha.executar(
                new ByteArrayInputStream(entrada.getBytes(StandardCharsets.UTF_8)),
                new PrintStream(saida, true, StandardCharsets.UTF_8),
                new PrintStream(erro, true, StandardCharsets.UTF_8));
    }

    private String saida() {
        return saida.toString(StandardCharsets.UTF_8).trim();
    }

    @Test
    void geraHashBcryptDeCusto12QueConfereComASenha() {

        assertEquals(0, executar("SenhaForte@2026\n"));

        String hash = saida();

        assertTrue(hash.startsWith("$2a$12$"), hash);
        assertTrue(new BCryptPasswordEncoder().matches("SenhaForte@2026", hash));
    }

    @Test
    void quebraDeLinhaDoWindowsNaoEntraNaSenha() {

        assertEquals(0, executar("SenhaForte@2026\r\n"));

        assertTrue(new BCryptPasswordEncoder().matches("SenhaForte@2026", saida()));
    }

    @Test
    void recusaSenhaCurtaOuAusenteSemImprimirHash() {

        assertEquals(2, executar("curta\n"));
        assertEquals(2, executar(""));
        assertEquals("", saida());
    }

    @Test
    void recusaSenhaAcimaDoLimiteDoBcrypt() {

        assertEquals(2, executar("a".repeat(73) + "\n"));
        assertEquals("", saida());
        assertTrue(erro.toString(StandardCharsets.UTF_8).contains("72 bytes"));
    }
}
