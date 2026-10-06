package br.com.vidratx.controller;

import br.com.vidratx.dto.OrdemServicoRequest;
import br.com.vidratx.dto.OrdemServicoResponse;
import br.com.vidratx.dto.OrdemServicoUpdateRequest;
import br.com.vidratx.enums.StatusProducao;
import br.com.vidratx.security.UsuarioAutenticado;
import br.com.vidratx.service.OrdemServicoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ordens-servico")
@Validated
public class OrdemServicoController {

    private final OrdemServicoService ordemServicoService;

    public OrdemServicoController(
            OrdemServicoService ordemServicoService) {

        this.ordemServicoService = ordemServicoService;
    }

    @GetMapping
    public ResponseEntity<List<OrdemServicoResponse>> listar(
            @RequestParam(required = false) StatusProducao statusProducao) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                ordemServicoService.listarPorEmpresa(
                        empresaId,
                        statusProducao
                )
        );
    }

    @GetMapping("/{ordemServicoId}")
    public ResponseEntity<OrdemServicoResponse> buscarPorId(
            @PathVariable
            @Positive(message = "ID da ordem de serviço deve ser maior que zero")
            Long ordemServicoId) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                ordemServicoService.buscarPorId(
                        empresaId,
                        ordemServicoId
                )
        );
    }

    @PostMapping
    public ResponseEntity<OrdemServicoResponse> criar(
            @Valid @RequestBody OrdemServicoRequest request) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        OrdemServicoResponse response =
                ordemServicoService.criar(empresaId, request, UsuarioAutenticado.get());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{ordemServicoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<OrdemServicoResponse> atualizar(
            @PathVariable
            @Positive(message = "ID da ordem de serviço deve ser maior que zero")
            Long ordemServicoId,

            @Valid @RequestBody OrdemServicoUpdateRequest request) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                ordemServicoService.atualizar(
                        empresaId,
                        ordemServicoId,
                        request
                )
        );
    }

    @PostMapping("/{ordemServicoId}/iniciar-producao")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<OrdemServicoResponse> iniciarProducao(
            @PathVariable
            @Positive(message = "ID da ordem de serviço deve ser maior que zero")
            Long ordemServicoId) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                ordemServicoService.iniciarProducao(
                        empresaId,
                        ordemServicoId
                )
        );
    }

    @PostMapping("/{ordemServicoId}/concluir-producao")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<OrdemServicoResponse> concluirProducao(
            @PathVariable
            @Positive(message = "ID da ordem de serviço deve ser maior que zero")
            Long ordemServicoId) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                ordemServicoService.concluirProducao(
                        empresaId,
                        ordemServicoId
                )
        );
    }

    @PostMapping("/{ordemServicoId}/conferir-producao")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<OrdemServicoResponse> conferirProducao(
            @PathVariable
            @Positive(message = "ID da ordem de serviço deve ser maior que zero")
            Long ordemServicoId) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                ordemServicoService.conferirProducao(
                        empresaId,
                        ordemServicoId
                )
        );
    }

    @PutMapping("/{ordemServicoId}/necessita-producao")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<OrdemServicoResponse> alterarNecessitaProducao(
            @PathVariable
            @Positive(message = "ID da ordem de serviço deve ser maior que zero")
            Long ordemServicoId,

            @RequestParam boolean valor) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                ordemServicoService.alterarNecessitaProducao(
                        empresaId,
                        ordemServicoId,
                        valor
                )
        );
    }
}
