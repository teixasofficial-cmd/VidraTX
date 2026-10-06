package br.com.vidratx.controller;

import br.com.vidratx.dto.LoginRequest;
import br.com.vidratx.dto.LoginResponse;
import br.com.vidratx.security.UsuarioAutenticado;
import br.com.vidratx.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(
            AuthService authService) {

        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(
                authService.login(request)
        );
    }

    @PostMapping("/renovar")
    public ResponseEntity<LoginResponse> renovar() {

        return ResponseEntity.ok(
                authService.renovar(UsuarioAutenticado.getUsuarioId())
        );
    }
}
