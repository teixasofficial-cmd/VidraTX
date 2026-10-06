package br.com.vidratx.controller;

import br.com.vidratx.dto.WhatsappInstanciaResponse;
import br.com.vidratx.security.UsuarioAutenticado;
import br.com.vidratx.service.WhatsappInstanciaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/whatsapp/instancia")
@PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
public class WhatsappInstanciaController {

    private final WhatsappInstanciaService whatsappInstanciaService;

    public WhatsappInstanciaController(
            WhatsappInstanciaService whatsappInstanciaService) {

        this.whatsappInstanciaService = whatsappInstanciaService;
    }

    @GetMapping
    public ResponseEntity<WhatsappInstanciaResponse> status() {

        Long empresaId = UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                whatsappInstanciaService.status(empresaId)
        );
    }

    @GetMapping("/qr")
    public ResponseEntity<WhatsappInstanciaResponse> statusComQr() {

        Long empresaId = UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                whatsappInstanciaService.statusComQr(empresaId)
        );
    }

    @PostMapping("/conectar")
    public ResponseEntity<WhatsappInstanciaResponse> conectar() {

        Long empresaId = UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                whatsappInstanciaService.conectar(empresaId)
        );
    }
}
