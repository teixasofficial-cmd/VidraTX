package br.com.vidratx.controller;

import br.com.vidratx.dto.OnboardingRequest;
import br.com.vidratx.dto.OnboardingResponse;
import br.com.vidratx.service.OnboardingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
public class OnboardingController {

    private final OnboardingService onboardingService;

    public OnboardingController(
            OnboardingService onboardingService) {

        this.onboardingService =
                onboardingService;
    }

    @PostMapping("/cadastro")
    public ResponseEntity<OnboardingResponse> cadastrar(
            @Valid @RequestBody OnboardingRequest request) {

        OnboardingResponse response =
                onboardingService.cadastrar(request);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

}
