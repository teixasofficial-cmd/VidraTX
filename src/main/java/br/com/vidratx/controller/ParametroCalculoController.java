package br.com.vidratx.controller;

import br.com.vidratx.dto.ParametroCalculoRequest;
import br.com.vidratx.dto.ParametroCalculoResponse;
import br.com.vidratx.security.UsuarioAutenticado;
import br.com.vidratx.service.ParametroCalculoService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/parametros-calculo")
public class ParametroCalculoController {

    private final ParametroCalculoService parametroCalculoService;

    public ParametroCalculoController(ParametroCalculoService parametroCalculoService) {
        this.parametroCalculoService = parametroCalculoService;
    }

    @GetMapping
    public ResponseEntity<ParametroCalculoResponse> buscar() {

        Long empresaId = UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(parametroCalculoService.buscarPorEmpresa(empresaId));
    }

    @PutMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<ParametroCalculoResponse> atualizar(
            @Valid @RequestBody ParametroCalculoRequest request) {

        Long empresaId = UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(parametroCalculoService.atualizar(empresaId, request));
    }
}
