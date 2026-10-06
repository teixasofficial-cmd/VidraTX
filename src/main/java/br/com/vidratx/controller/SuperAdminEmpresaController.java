package br.com.vidratx.controller;

import br.com.vidratx.dto.EmpresaSuperAdminAtualizarRequest;
import br.com.vidratx.dto.EmpresaSuperAdminRequest;
import br.com.vidratx.dto.EmpresaSuperAdminResponse;
import br.com.vidratx.dto.RedefinirSenhaSuperAdminRequest;
import br.com.vidratx.dto.SuperAdminResumoResponse;
import br.com.vidratx.service.SuperAdminEmpresaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/superadmin/empresas")
@Validated
public class SuperAdminEmpresaController {

    private final SuperAdminEmpresaService superAdminEmpresaService;

    public SuperAdminEmpresaController(
            SuperAdminEmpresaService superAdminEmpresaService) {

        this.superAdminEmpresaService = superAdminEmpresaService;
    }

    @GetMapping
    public ResponseEntity<List<EmpresaSuperAdminResponse>> listar() {

        return ResponseEntity.ok(
                superAdminEmpresaService.listar()
        );
    }

    @GetMapping("/resumo")
    public ResponseEntity<SuperAdminResumoResponse> resumo() {

        return ResponseEntity.ok(
                superAdminEmpresaService.resumo()
        );
    }

    @PostMapping
    public ResponseEntity<EmpresaSuperAdminResponse> criar(
            @Valid @RequestBody EmpresaSuperAdminRequest request) {

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(superAdminEmpresaService.criar(request));
    }

    @PutMapping("/{empresaId}")
    public ResponseEntity<EmpresaSuperAdminResponse> atualizar(
            @PathVariable
            @Positive(message = "ID da empresa deve ser maior que zero")
            Long empresaId,

            @Valid @RequestBody EmpresaSuperAdminAtualizarRequest request) {

        return ResponseEntity.ok(
                superAdminEmpresaService.atualizar(empresaId, request)
        );
    }

    @PostMapping("/{empresaId}/alternar-status")
    public ResponseEntity<EmpresaSuperAdminResponse> alternarStatus(
            @PathVariable
            @Positive(message = "ID da empresa deve ser maior que zero")
            Long empresaId) {

        return ResponseEntity.ok(
                superAdminEmpresaService.alternarStatus(empresaId)
        );
    }

    @PostMapping("/{empresaId}/redefinir-senha")
    public ResponseEntity<Void> redefinirSenha(
            @PathVariable
            @Positive(message = "ID da empresa deve ser maior que zero")
            Long empresaId,

            @Valid @RequestBody RedefinirSenhaSuperAdminRequest request) {

        superAdminEmpresaService.redefinirSenha(empresaId, request.getSenha());

        return ResponseEntity.noContent().build();
    }
}
