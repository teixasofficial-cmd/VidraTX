package br.com.vidratx.controller;

import br.com.vidratx.dto.TipologiaAjusteRequest;
import br.com.vidratx.dto.TipologiaResponse;
import br.com.vidratx.security.UsuarioAutenticado;
import br.com.vidratx.service.TipologiaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tipologias")
@Validated
public class TipologiaController {

    private final TipologiaService tipologiaService;

    public TipologiaController(TipologiaService tipologiaService) {
        this.tipologiaService = tipologiaService;
    }

    @GetMapping
    public ResponseEntity<List<TipologiaResponse>> listar(
            @RequestParam(required = false, defaultValue = "false") boolean apenasAtivas) {

        Long empresaId = UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(tipologiaService.listarPorEmpresa(empresaId, apenasAtivas));
    }

    @PutMapping("/{tipologiaId}/folgas")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<TipologiaResponse> ajustarFolgas(
            @PathVariable @Positive(message = "ID da tipologia deve ser maior que zero") Long tipologiaId,
            @Valid @RequestBody TipologiaAjusteRequest request) {

        Long empresaId = UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(tipologiaService.ajustarFolgas(empresaId, tipologiaId, request));
    }
}
