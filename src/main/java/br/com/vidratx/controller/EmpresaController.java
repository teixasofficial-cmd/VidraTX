package br.com.vidratx.controller;

import br.com.vidratx.dto.AgendaEmpresaDto;
import br.com.vidratx.dto.EmpresaRequest;
import br.com.vidratx.dto.EmpresaResponse;
import br.com.vidratx.security.UsuarioAutenticado;
import br.com.vidratx.service.EmpresaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/empresa")
public class EmpresaController {

    private final EmpresaService empresaService;

    public EmpresaController(
            EmpresaService empresaService) {

        this.empresaService = empresaService;
    }

    @GetMapping
    public ResponseEntity<EmpresaResponse> minhaEmpresa() {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                empresaService.buscarPorId(
                        empresaId
                )
        );
    }

    @PutMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EmpresaResponse> atualizarMinhaEmpresa(
            @Valid
            @RequestBody
            EmpresaRequest request) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                empresaService.atualizar(
                        empresaId,
                        request
                )
        );
    }

    @GetMapping("/agenda")
    public ResponseEntity<AgendaEmpresaDto> agenda() {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                empresaService.buscarAgenda(empresaId)
        );
    }

    @PutMapping("/agenda")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<AgendaEmpresaDto> atualizarAgenda(
            @Valid
            @RequestBody
            AgendaEmpresaDto request) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                empresaService.atualizarAgenda(
                        empresaId,
                        request
                )
        );
    }
}
