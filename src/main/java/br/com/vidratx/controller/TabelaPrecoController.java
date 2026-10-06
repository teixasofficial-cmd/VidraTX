package br.com.vidratx.controller;

import br.com.vidratx.dto.TabelaPrecoRequest;
import br.com.vidratx.dto.TabelaPrecoResponse;
import br.com.vidratx.security.UsuarioAutenticado;
import br.com.vidratx.service.TabelaPrecoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/tabela-precos")
@Validated
public class TabelaPrecoController {

    private final TabelaPrecoService tabelaPrecoService;

    public TabelaPrecoController(TabelaPrecoService tabelaPrecoService) {
        this.tabelaPrecoService = tabelaPrecoService;
    }

    @GetMapping
    public ResponseEntity<List<TabelaPrecoResponse>> listar(
            @RequestParam(required = false, defaultValue = "false") boolean apenasAtivos) {

        Long empresaId = UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(tabelaPrecoService.listarPorEmpresa(empresaId, apenasAtivos));
    }

    @GetMapping("/{tabelaPrecoId}")
    public ResponseEntity<TabelaPrecoResponse> buscarPorId(
            @PathVariable @Positive(message = "ID do item deve ser maior que zero") Long tabelaPrecoId) {

        Long empresaId = UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(tabelaPrecoService.buscarPorId(empresaId, tabelaPrecoId));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<TabelaPrecoResponse> salvar(@Valid @RequestBody TabelaPrecoRequest request) {

        Long empresaId = UsuarioAutenticado.getEmpresaId();

        TabelaPrecoResponse response = tabelaPrecoService.salvar(empresaId, request);

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/{tabelaPrecoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<TabelaPrecoResponse> atualizar(
            @PathVariable @Positive(message = "ID do item deve ser maior que zero") Long tabelaPrecoId,
            @Valid @RequestBody TabelaPrecoRequest request) {

        Long empresaId = UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(tabelaPrecoService.atualizar(empresaId, tabelaPrecoId, request));
    }

    @DeleteMapping("/{tabelaPrecoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<Void> excluir(
            @PathVariable @Positive(message = "ID do item deve ser maior que zero") Long tabelaPrecoId) {

        Long empresaId = UsuarioAutenticado.getEmpresaId();

        tabelaPrecoService.excluir(empresaId, tabelaPrecoId);

        return ResponseEntity.noContent().build();
    }
}
