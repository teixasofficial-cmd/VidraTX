package br.com.vidratx.controller;

import br.com.vidratx.dto.PerguntaFrequenteRequest;
import br.com.vidratx.dto.PerguntaFrequenteResponse;
import br.com.vidratx.security.UsuarioAutenticado;
import br.com.vidratx.service.PerguntaFrequenteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/perguntas-frequentes")
public class PerguntaFrequenteController {

    private final PerguntaFrequenteService perguntaFrequenteService;

    public PerguntaFrequenteController(PerguntaFrequenteService perguntaFrequenteService) {
        this.perguntaFrequenteService = perguntaFrequenteService;
    }

    @GetMapping
    public ResponseEntity<List<PerguntaFrequenteResponse>> listar() {
        return ResponseEntity.ok(perguntaFrequenteService.listar(UsuarioAutenticado.getEmpresaId()));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<PerguntaFrequenteResponse> criar(@Valid @RequestBody PerguntaFrequenteRequest request) {

        return ResponseEntity.status(HttpStatus.CREATED)
                .body(perguntaFrequenteService.criar(UsuarioAutenticado.getEmpresaId(), request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<PerguntaFrequenteResponse> atualizar(
            @PathVariable Long id,
            @Valid @RequestBody PerguntaFrequenteRequest request) {

        return ResponseEntity.ok(perguntaFrequenteService.atualizar(UsuarioAutenticado.getEmpresaId(), id, request));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<Void> excluir(@PathVariable Long id) {

        perguntaFrequenteService.excluir(UsuarioAutenticado.getEmpresaId(), id);

        return ResponseEntity.noContent().build();
    }
}
