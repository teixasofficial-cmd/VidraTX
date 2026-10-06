package br.com.vidratx.controller;

import br.com.vidratx.dto.ClienteRequest;
import br.com.vidratx.dto.ClienteResponse;
import br.com.vidratx.security.UsuarioAutenticado;
import br.com.vidratx.service.AnonimizacaoClienteService;
import br.com.vidratx.service.ClienteService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/clientes")
@Validated
public class ClienteController {

    private final ClienteService clienteService;
    private final AnonimizacaoClienteService anonimizacaoClienteService;

    public ClienteController(
            ClienteService clienteService,
            AnonimizacaoClienteService anonimizacaoClienteService) {

        this.clienteService = clienteService;
        this.anonimizacaoClienteService = anonimizacaoClienteService;
    }

    @GetMapping
    public ResponseEntity<List<ClienteResponse>> listar(
            @RequestParam(required = false)
            String nome) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                clienteService.listarPorEmpresa(
                        empresaId,
                        nome
                )
        );
    }

    @GetMapping("/{clienteId}")
    public ResponseEntity<ClienteResponse> buscarPorId(
            @PathVariable
            @Positive(
                    message =
                            "ID do cliente deve ser maior que zero"
            )
            Long clienteId) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                clienteService.buscarPorId(
                        empresaId,
                        clienteId
                )
        );
    }

    @PostMapping
    public ResponseEntity<ClienteResponse> salvar(
            @Valid
            @RequestBody
            ClienteRequest request) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        ClienteResponse response =
                clienteService.salvar(
                        empresaId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{clienteId}")
    public ResponseEntity<ClienteResponse> atualizar(
            @PathVariable
            @Positive(
                    message =
                            "ID do cliente deve ser maior que zero"
            )
            Long clienteId,

            @Valid
            @RequestBody
            ClienteRequest request) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                clienteService.atualizar(
                        empresaId,
                        clienteId,
                        request
                )
        );
    }

    @DeleteMapping("/{clienteId}")
    public ResponseEntity<Void> excluir(
            @PathVariable
            @Positive(
                    message =
                            "ID do cliente deve ser maior que zero"
            )
            Long clienteId) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        clienteService.excluir(
                empresaId,
                clienteId
        );

        return ResponseEntity
                .noContent()
                .build();
    }

    @PostMapping("/{clienteId}/anonimizar")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> anonimizar(@PathVariable @Positive Long clienteId) {

        anonimizacaoClienteService.anonimizar(UsuarioAutenticado.getEmpresaId(), clienteId);

        return ResponseEntity.noContent().build();
    }
}
