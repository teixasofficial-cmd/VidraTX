package br.com.vidratx.controller;

import br.com.vidratx.dto.MaterialRequest;
import br.com.vidratx.dto.MaterialResponse;
import br.com.vidratx.security.UsuarioAutenticado;
import br.com.vidratx.service.MaterialService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/materiais")
@Validated
public class MaterialController {

    private final MaterialService materialService;

    public MaterialController(
            MaterialService materialService) {

        this.materialService = materialService;
    }

    @GetMapping
    public ResponseEntity<List<MaterialResponse>> listar(
            @RequestParam(
                    required = false,
                    defaultValue = "false"
            )
            boolean apenasAtivos) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                materialService.listarPorEmpresa(
                        empresaId,
                        apenasAtivos
                )
        );
    }

    @GetMapping("/{materialId}")
    public ResponseEntity<MaterialResponse> buscarPorId(
            @PathVariable
            @Positive(message = "ID do material deve ser maior que zero")
            Long materialId) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                materialService.buscarPorId(
                        empresaId,
                        materialId
                )
        );
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<MaterialResponse> salvar(
            @Valid @RequestBody MaterialRequest request) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        MaterialResponse response =
                materialService.salvar(empresaId, request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{materialId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<MaterialResponse> atualizar(
            @PathVariable
            @Positive(message = "ID do material deve ser maior que zero")
            Long materialId,

            @Valid @RequestBody MaterialRequest request) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                materialService.atualizar(
                        empresaId,
                        materialId,
                        request
                )
        );
    }

    @DeleteMapping("/{materialId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<Void> excluir(
            @PathVariable
            @Positive(message = "ID do material deve ser maior que zero")
            Long materialId) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        materialService.excluir(empresaId, materialId);

        return ResponseEntity.noContent().build();
    }
}
