package br.com.vidratx.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FluxoNomeClienteTest {

    @ParameterizedTest
    @ValueSource(strings = {"quero orçamento", "oi", "Bom dia!", "quanto custa um box?", "123", "👍", "sim",
            "preciso de um espelho pro banheiro"})
    void pedidoSaudacaoOuSimboloNaoViramNome(String texto) {
        assertTrue(FluxoAtendimentoService.extrairNome(texto).isEmpty(), texto);
    }

    @Test
    void nomesValidosSaoNormalizados() {

        assertEquals(Optional.of("Maria Silva"), FluxoAtendimentoService.extrairNome("Maria Silva"));
        assertEquals(Optional.of("Ana Paula"), FluxoAtendimentoService.extrairNome("meu nome é ana paula"));
        assertEquals(Optional.of("João da Silva"), FluxoAtendimentoService.extrairNome("joão da silva"));
        assertEquals(Optional.of("Carlos"), FluxoAtendimentoService.extrairNome("Sou o Carlos."));
    }
}
