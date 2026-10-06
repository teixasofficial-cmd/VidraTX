package br.com.vidratx.service;

import br.com.vidratx.exception.MensagemNaoEncontradaException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.UUID;

@Component
public class WhatsappMidiaStorage {

    private static final Logger log = LoggerFactory.getLogger(WhatsappMidiaStorage.class);

    private final Path diretorioBase;

    public WhatsappMidiaStorage(
            @Value("${vidratx.whatsapp.midia-dir}") String diretorio) {

        this.diretorioBase = Path.of(diretorio).toAbsolutePath().normalize();
    }

    public String salvar(Long empresaId, MultipartFile arquivo) {

        try {

            Path diretorioEmpresa = diretorioBase.resolve(String.valueOf(empresaId));
            Files.createDirectories(diretorioEmpresa);

            String nomeArquivo = UUID.randomUUID() + extensaoDe(arquivo.getContentType());
            Path destino = diretorioEmpresa.resolve(nomeArquivo);

            arquivo.transferTo(destino);

            return empresaId + "/" + nomeArquivo;

        } catch (IOException ex) {

            throw new UncheckedIOException(
                    "Falha ao salvar mídia recebida do WhatsApp", ex
            );
        }
    }

    public void excluir(String caminhoRelativo) {

        if (caminhoRelativo == null) {
            return;
        }

        Path caminho = diretorioBase.resolve(caminhoRelativo).normalize();

        if (!caminho.startsWith(diretorioBase)) {
            return;
        }

        try {
            Files.deleteIfExists(caminho);
        } catch (IOException ex) {
            log.warn("Não foi possível remover a mídia descartada {}", caminhoRelativo, ex);
        }
    }

    public Resource carregar(String caminhoRelativo) {

        Path caminho = diretorioBase.resolve(caminhoRelativo).normalize();

        if (!caminho.startsWith(diretorioBase)) {
            throw new MensagemNaoEncontradaException("Mídia não encontrada");
        }

        Resource recurso = new FileSystemResource(caminho);

        if (!recurso.exists() || !recurso.isReadable()) {
            throw new MensagemNaoEncontradaException("Mídia não encontrada");
        }

        return recurso;
    }

    private String extensaoDe(String contentType) {

        if ("image/png".equals(contentType)) {
            return ".png";
        }

        if ("image/webp".equals(contentType)) {
            return ".webp";
        }

        if ("image/gif".equals(contentType)) {
            return ".gif";
        }

        return ".jpg";
    }
}
