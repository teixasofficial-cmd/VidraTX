package br.com.vidratx.controller;

import br.com.vidratx.dto.WhatsappWebhookConexaoRequest;
import br.com.vidratx.dto.WhatsappWebhookMensagemRequest;
import br.com.vidratx.dto.WhatsappWebhookStatusRequest;
import br.com.vidratx.exception.CredenciaisInvalidasException;
import br.com.vidratx.service.WhatsappInstanciaService;
import br.com.vidratx.service.WhatsappEntradaService;
import jakarta.validation.Valid;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/whatsapp/webhook")
@Validated
public class WhatsappWebhookController {

    private final WhatsappEntradaService whatsappEntradaService;
    private final WhatsappInstanciaService whatsappInstanciaService;

    public WhatsappWebhookController(
            WhatsappEntradaService whatsappEntradaService,
            WhatsappInstanciaService whatsappInstanciaService) {

        this.whatsappEntradaService = whatsappEntradaService;
        this.whatsappInstanciaService = whatsappInstanciaService;
    }

    @PostMapping("/mensagens")
    public ResponseEntity<Void> receberMensagem(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody WhatsappWebhookMensagemRequest request) {

        whatsappEntradaService.receberTexto(
                extrairToken(authorization),
                request.getTelefone(),
                request.getMensagem(),
                request.getMensagemId(),
                request.getTimestamp(),
                request.getMidiaNaoSuportada(),
                request.getReacaoA(),
                request.getEditaMensagemId(),
                request.getApagaMensagemId()
        );

        return ResponseEntity.ok().build();
    }

    @PostMapping(value = "/midia", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Void> receberMidia(
            @RequestHeader("Authorization") String authorization,
            @RequestParam("telefone") String telefone,
            @RequestParam("arquivo") MultipartFile arquivo,
            @RequestParam(value = "legenda", required = false) String legenda,
            @RequestParam(value = "mensagemId", required = false) String mensagemId,
            @RequestParam(value = "timestamp", required = false) Long timestamp) {

        whatsappEntradaService.receberMidia(
                extrairToken(authorization), telefone, arquivo, legenda, mensagemId, timestamp
        );

        return ResponseEntity.ok().build();
    }

    @PostMapping("/status")
    public ResponseEntity<Void> atualizarStatusEntrega(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody WhatsappWebhookStatusRequest request) {

        whatsappEntradaService.atualizarStatusEntrega(
                extrairToken(authorization), request.getMensagemId(), request.getStatus()
        );

        return ResponseEntity.ok().build();
    }

    @PostMapping("/conexao")
    public ResponseEntity<Void> atualizarConexao(
            @RequestHeader("Authorization") String authorization,
            @Valid @RequestBody WhatsappWebhookConexaoRequest request) {

        whatsappInstanciaService.atualizarStatusConexao(
                extrairToken(authorization),
                request.getStatus(),
                request.getNumero()
        );

        return ResponseEntity.ok().build();
    }

    private String extrairToken(String authorization) {

        if (authorization == null || !authorization.startsWith("Bearer ")) {

            throw new CredenciaisInvalidasException(
                    "Token do webhook ausente ou em formato inválido"
            );
        }

        String token = authorization.substring(7).trim();

        if (token.isBlank()) {

            throw new CredenciaisInvalidasException(
                    "Token do webhook ausente ou em formato inválido"
            );
        }

        return token;
    }
}
