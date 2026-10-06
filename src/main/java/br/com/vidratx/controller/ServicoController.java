package br.com.vidratx.controller;

import br.com.vidratx.dto.ServicoRequest;
import br.com.vidratx.dto.ServicoResponse;
import br.com.vidratx.security.UsuarioAutenticado;
import br.com.vidratx.service.ServicoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/servicos")
@Validated
public class ServicoController {

    private final ServicoService servicoService;

    public ServicoController(
            ServicoService servicoService) {

        this.servicoService = servicoService;
    }

    @GetMapping
    public ResponseEntity<List<ServicoResponse>> listar(
            @RequestParam(
                    required = false,
                    defaultValue = "false"
            )
            boolean apenasAtivos) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                servicoService.listarPorEmpresa(
                        empresaId,
                        apenasAtivos
                )
        );
    }

    @GetMapping("/{servicoId}")
    public ResponseEntity<ServicoResponse> buscarPorId(
            @PathVariable
            @Positive(
                    message =
                            "ID do serviço deve ser maior que zero"
            )
            Long servicoId) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                servicoService.buscarPorId(
                        empresaId,
                        servicoId
                )
        );
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<ServicoResponse> salvar(
            @Valid
            @RequestBody
            ServicoRequest request) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        ServicoResponse response =
                servicoService.salvar(
                        empresaId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{servicoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<ServicoResponse> atualizar(
            @PathVariable
            @Positive(
                    message =
                            "ID do serviço deve ser maior que zero"
            )
            Long servicoId,

            @Valid
            @RequestBody
            ServicoRequest request) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                servicoService.atualizar(
                        empresaId,
                        servicoId,
                        request
                )
        );
    }

    @DeleteMapping("/{servicoId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<Void> excluir(
            @PathVariable
            @Positive(
                    message =
                            "ID do serviço deve ser maior que zero"
            )
            Long servicoId) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        servicoService.excluir(
                empresaId,
                servicoId
        );

        return ResponseEntity
                .noContent()
                .build();
    }
}
