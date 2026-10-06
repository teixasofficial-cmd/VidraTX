package br.com.vidratx.controller;

import br.com.vidratx.dto.SuperAdminLoginRequest;
import br.com.vidratx.dto.SuperAdminLoginResponse;
import br.com.vidratx.service.SuperAdminAuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth/superadmin")
public class SuperAdminAuthController {

    private final SuperAdminAuthService superAdminAuthService;

    public SuperAdminAuthController(
            SuperAdminAuthService superAdminAuthService) {

        this.superAdminAuthService = superAdminAuthService;
    }

    @PostMapping("/login")
    public ResponseEntity<SuperAdminLoginResponse> login(
            @Valid @RequestBody SuperAdminLoginRequest request) {

        return ResponseEntity.ok(
                superAdminAuthService.login(request)
        );
    }
}
