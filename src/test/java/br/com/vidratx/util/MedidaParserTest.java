package br.com.vidratx.util;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MedidaParserTest {

    @Test
    void interpretaMedidaEmMetrosComVirgula() {

        Optional<MedidaParser.MedidaParseada> resultado =
                MedidaParser.tentarInterpretar("1,20 x 1,90");

        assertTrue(resultado.isPresent());
        assertEquals(1200, resultado.get().larguraMm());
        assertEquals(1900, resultado.get().alturaMm());
    }

    @Test
    void interpretaMedidaEmCentimetrosSemSeparador() {

        Optional<MedidaParser.MedidaParseada> resultado =
                MedidaParser.tentarInterpretar("120x190");

        assertTrue(resultado.isPresent());
        assertEquals(1200, resultado.get().larguraMm());
        assertEquals(1900, resultado.get().alturaMm());
    }

    @Test
    void interpretaMedidaEmMetrosComPontoEUnidade() {

        Optional<MedidaParser.MedidaParseada> resultado =
                MedidaParser.tentarInterpretar("1.20 por 1.90 m");

        assertTrue(resultado.isPresent());
        assertEquals(1200, resultado.get().larguraMm());
        assertEquals(1900, resultado.get().alturaMm());
    }

    @Test
    void textoNaoInterpretavelNaoInventaValor() {

        assertTrue(MedidaParser.tentarInterpretar("mais ou menos uns dois metros").isEmpty());
        assertTrue(MedidaParser.tentarInterpretar("").isEmpty());
        assertTrue(MedidaParser.tentarInterpretar(null).isEmpty());
    }

    @Test
    void interpretaMetrosECentimetrosJuntosEMilimetros() {

        assertMedida("1m20 x 1m90", 1200, 1900);
        assertMedida("1m2 x 1m9", 1200, 1900);
        assertMedida("1200 x 1900", 1200, 1900);
        assertMedida("1200mm x 1900mm", 1200, 1900);
        assertMedida("120cm por 190cm", 1200, 1900);
        assertMedida("1,20m x 1,90m", 1200, 1900);
        assertMedida("2 x 1", 2000, 1000);
        assertMedida("10 x 10", 100, 100);
    }

    @Test
    void usaOParDeMedidasENaoOPrimeiroNumeroDoTexto() {

        assertMedida("box de 2 folhas, 1,20 x 1,90, vidro 8 mm", 1200, 1900);
        assertMedida("Janela 3 de 1,50 × 1,00", 1500, 1000);
    }

    @Test
    void recusaMedidaImpossivel() {

        assertTrue(MedidaParser.tentarInterpretar("0 x 0").isEmpty());
        assertTrue(MedidaParser.tentarInterpretar("900 x 900").isEmpty());
        assertTrue(MedidaParser.tentarInterpretar("12 x 1900000").isEmpty());
        assertTrue(MedidaParser.tentarInterpretar("abc").isEmpty());
        assertTrue(MedidaParser.tentarInterpretar("vidro 8 mm").isEmpty());
    }

    @Test
    void descreveEmMetrosParaOClienteConferir() {
        assertEquals("1,20 m × 1,90 m", MedidaParser.tentarInterpretar("120x190").orElseThrow().descricaoEmMetros());
    }

    private static void assertMedida(String texto, int largura, int altura) {

        Optional<MedidaParser.MedidaParseada> resultado = MedidaParser.tentarInterpretar(texto);

        assertTrue(resultado.isPresent(), texto);
        assertEquals(largura, resultado.get().larguraMm(), texto);
        assertEquals(altura, resultado.get().alturaMm(), texto);
    }
}
