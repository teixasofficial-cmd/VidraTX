package br.com.vidratx.controller;

import br.com.vidratx.dto.MaterialNecessarioRequest;
import br.com.vidratx.dto.MaterialNecessarioResponse;
import br.com.vidratx.security.UsuarioAutenticado;
import br.com.vidratx.service.MaterialNecessarioService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orcamentos/{orcamentoId}/materiais")
@Validated
public class MaterialNecessarioController {

    private final MaterialNecessarioService materialNecessarioService;

    public MaterialNecessarioController(
            MaterialNecessarioService materialNecessarioService) {

        this.materialNecessarioService = materialNecessarioService;
    }

    @GetMapping
    public ResponseEntity<List<MaterialNecessarioResponse>> listar(
            @PathVariable
            @Positive(message = "ID do orçamento deve ser maior que zero")
            Long orcamentoId) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                materialNecessarioService.listar(
                        empresaId,
                        orcamentoId
                )
        );
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<MaterialNecessarioResponse> adicionar(
            @PathVariable
            @Positive(message = "ID do orçamento deve ser maior que zero")
            Long orcamentoId,

            @Valid @RequestBody MaterialNecessarioRequest request) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        MaterialNecessarioResponse response =
                materialNecessarioService.adicionar(
                        empresaId,
                        orcamentoId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{itemId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<MaterialNecessarioResponse> atualizar(
            @PathVariable
            @Positive(message = "ID do orçamento deve ser maior que zero")
            Long orcamentoId,

            @PathVariable
            @Positive(message = "ID do item deve ser maior que zero")
            Long itemId,

            @Valid @RequestBody MaterialNecessarioRequest request) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                materialNecessarioService.atualizar(
                        empresaId,
                        orcamentoId,
                        itemId,
                        request
                )
        );
    }

    @DeleteMapping("/{itemId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<Void> remover(
            @PathVariable
            @Positive(message = "ID do orçamento deve ser maior que zero")
            Long orcamentoId,

            @PathVariable
            @Positive(message = "ID do item deve ser maior que zero")
            Long itemId) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        materialNecessarioService.remover(
                empresaId,
                orcamentoId,
                itemId
        );

        return ResponseEntity.noContent().build();
    }
}
