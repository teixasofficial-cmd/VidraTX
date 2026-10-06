package br.com.vidratx.controller;

import br.com.vidratx.dto.EmpresaPublicResponse;
import br.com.vidratx.service.EmpresaService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/empresas")
public class EmpresaPublicController {

    private final EmpresaService empresaService;

    public EmpresaPublicController(
            EmpresaService empresaService) {

        this.empresaService = empresaService;
    }

    @GetMapping("/{slug}")
    public ResponseEntity<EmpresaPublicResponse> buscarPorSlug(
            @PathVariable String slug) {

        return ResponseEntity.ok(
                empresaService.buscarPublicaPorSlug(slug)
        );
    }
}
