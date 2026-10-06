package br.com.vidratx.controller;

import br.com.vidratx.dto.TrocarSenhaRequest;
import br.com.vidratx.security.SuperAdminAutenticado;
import br.com.vidratx.service.SuperAdminAuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/superadmin/me")
public class SuperAdminMeController {

    private final SuperAdminAuthService superAdminAuthService;

    public SuperAdminMeController(
            SuperAdminAuthService superAdminAuthService) {

        this.superAdminAuthService = superAdminAuthService;
    }

    @PutMapping("/senha")
    public ResponseEntity<Void> trocarMinhaSenha(
            @Valid @RequestBody TrocarSenhaRequest request) {

        superAdminAuthService.trocarSenha(
                SuperAdminAutenticado.getId(),
                request
        );

        return ResponseEntity.noContent().build();
    }
}
