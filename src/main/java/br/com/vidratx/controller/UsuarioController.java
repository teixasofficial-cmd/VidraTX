package br.com.vidratx.controller;

import br.com.vidratx.dto.RedefinirSenhaSuperAdminRequest;
import br.com.vidratx.dto.TrocarSenhaRequest;
import br.com.vidratx.dto.UsuarioRequest;
import br.com.vidratx.dto.UsuarioResponse;
import br.com.vidratx.dto.UsuarioUpdateRequest;
import br.com.vidratx.security.UsuarioAutenticado;
import br.com.vidratx.service.UsuarioService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/usuarios")
@Validated
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(
            UsuarioService usuarioService) {

        this.usuarioService =
                usuarioService;
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UsuarioResponse>> listar() {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                usuarioService.listarPorEmpresa(
                        empresaId
                )
        );
    }

    @GetMapping("/{usuarioId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponse> buscarPorId(
            @PathVariable
            @Positive(
                    message =
                            "ID do usuário deve ser maior que zero"
            )
            Long usuarioId) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                usuarioService.buscarPorId(
                        empresaId,
                        usuarioId
                )
        );
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponse> salvar(
            @Valid
            @RequestBody
            UsuarioRequest request) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        UsuarioResponse response =
                usuarioService.salvar(
                        empresaId,
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{usuarioId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UsuarioResponse> atualizar(
            @PathVariable
            @Positive(
                    message =
                            "ID do usuário deve ser maior que zero"
            )
            Long usuarioId,

            @Valid
            @RequestBody
            UsuarioUpdateRequest request) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                usuarioService.atualizar(
                        empresaId,
                        usuarioId,
                        request
                )
        );
    }

    @PutMapping("/{usuarioId}/senha")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> redefinirSenha(
            @PathVariable @Positive Long usuarioId,
            @Valid @RequestBody RedefinirSenhaSuperAdminRequest request) {

        usuarioService.redefinirSenha(UsuarioAutenticado.getEmpresaId(), usuarioId, request.getSenha());

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/me/senha")
    public ResponseEntity<Void> trocarMinhaSenha(
            @Valid
            @RequestBody
            TrocarSenhaRequest request) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        Long usuarioId =
                UsuarioAutenticado.getUsuarioId();

        usuarioService.trocarSenha(
                empresaId,
                usuarioId,
                request
        );

        return ResponseEntity
                .noContent()
                .build();
    }

    @DeleteMapping("/{usuarioId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> excluir(
            @PathVariable
            @Positive(
                    message =
                            "ID do usuário deve ser maior que zero"
            )
            Long usuarioId) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        usuarioService.excluir(
                empresaId,
                usuarioId
        );

        return ResponseEntity
                .noContent()
                .build();
    }
}
