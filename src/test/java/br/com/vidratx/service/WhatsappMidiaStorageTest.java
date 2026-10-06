package br.com.vidratx.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WhatsappMidiaStorageTest {

    @Test
    void ignoraTentativaDePathTraversalNoNomeDoArquivo(@TempDir Path tempDir) throws IOException {

        WhatsappMidiaStorage storage = new WhatsappMidiaStorage(tempDir.toString());

        MockMultipartFile arquivoMalicioso = new MockMultipartFile(
                "arquivo",
                "foto.png/../../../fora-do-diretorio",
                "image/png",
                "conteudo".getBytes()
        );

        String caminhoRelativo = storage.salvar(1L, arquivoMalicioso);

        assertTrue(
                caminhoRelativo.matches("1/[0-9a-f-]+\\.png"),
                "esperava um UUID.png, veio: " + caminhoRelativo
        );

        Path arquivoSalvo = tempDir.resolve(caminhoRelativo).normalize();

        assertTrue(Files.exists(arquivoSalvo));
        assertTrue(arquivoSalvo.startsWith(tempDir));
        assertFalse(Files.exists(tempDir.resolveSibling("fora-do-diretorio")));
    }
}
