package br.com.vidratx.controller;

import br.com.vidratx.dto.FotoRequest;
import br.com.vidratx.dto.FotoResponse;
import br.com.vidratx.security.UsuarioAutenticado;
import br.com.vidratx.service.FotoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orcamentos/{orcamentoId}/fotos")
@Validated
public class FotoController {

    private final FotoService fotoService;

    public FotoController(
            FotoService fotoService) {

        this.fotoService = fotoService;
    }

    @GetMapping
    public ResponseEntity<List<FotoResponse>> listar(
            @PathVariable
            @Positive(message = "ID do orçamento deve ser maior que zero")
            Long orcamentoId) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                fotoService.listar(empresaId, orcamentoId)
        );
    }

    @PostMapping
    public ResponseEntity<FotoResponse> adicionar(
            @PathVariable
            @Positive(message = "ID do orçamento deve ser maior que zero")
            Long orcamentoId,

            @Valid @RequestBody FotoRequest request) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        FotoResponse response =
                fotoService.adicionar(empresaId, orcamentoId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @DeleteMapping("/{fotoId}")
    public ResponseEntity<Void> remover(
            @PathVariable
            @Positive(message = "ID do orçamento deve ser maior que zero")
            Long orcamentoId,

            @PathVariable
            @Positive(message = "ID da foto deve ser maior que zero")
            Long fotoId) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        fotoService.remover(empresaId, orcamentoId, fotoId);

        return ResponseEntity.noContent().build();
    }
}
