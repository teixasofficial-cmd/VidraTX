package br.com.vidratx.controller;

import br.com.vidratx.dto.PosVendaAtendimentoRequest;
import br.com.vidratx.dto.PosVendaRequest;
import br.com.vidratx.dto.PosVendaResolverRequest;
import br.com.vidratx.dto.PosVendaResponse;
import br.com.vidratx.security.UsuarioAutenticado;
import br.com.vidratx.service.PosVendaService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ordens-servico/{ordemServicoId}/pos-venda")
@Validated
public class PosVendaController {

    private final PosVendaService posVendaService;

    public PosVendaController(
            PosVendaService posVendaService) {

        this.posVendaService = posVendaService;
    }

    @GetMapping
    public ResponseEntity<List<PosVendaResponse>> listar(
            @PathVariable
            @Positive(message = "ID da ordem de serviço deve ser maior que zero")
            Long ordemServicoId) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                posVendaService.listar(empresaId, ordemServicoId)
        );
    }

    @PostMapping
    public ResponseEntity<PosVendaResponse> abrir(
            @PathVariable
            @Positive(message = "ID da ordem de serviço deve ser maior que zero")
            Long ordemServicoId,

            @Valid @RequestBody PosVendaRequest request) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        PosVendaResponse response =
                posVendaService.abrir(
                        empresaId, ordemServicoId, request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/{posVendaId}/iniciar-atendimento")
    public ResponseEntity<PosVendaResponse> iniciarAtendimento(
            @PathVariable
            @Positive(message = "ID da ordem de serviço deve ser maior que zero")
            Long ordemServicoId,

            @PathVariable
            @Positive(message = "ID da solicitação deve ser maior que zero")
            Long posVendaId,

            @Valid @RequestBody PosVendaAtendimentoRequest request) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                posVendaService.iniciarAtendimento(
                        empresaId, ordemServicoId, posVendaId, request
                )
        );
    }

    @PostMapping("/{posVendaId}/resolver")
    public ResponseEntity<PosVendaResponse> resolver(
            @PathVariable
            @Positive(message = "ID da ordem de serviço deve ser maior que zero")
            Long ordemServicoId,

            @PathVariable
            @Positive(message = "ID da solicitação deve ser maior que zero")
            Long posVendaId,

            @Valid @RequestBody PosVendaResolverRequest request) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                posVendaService.resolver(
                        empresaId, ordemServicoId, posVendaId, request
                )
        );
    }

    @PostMapping("/{posVendaId}/encerrar")
    public ResponseEntity<PosVendaResponse> encerrar(
            @PathVariable
            @Positive(message = "ID da ordem de serviço deve ser maior que zero")
            Long ordemServicoId,

            @PathVariable
            @Positive(message = "ID da solicitação deve ser maior que zero")
            Long posVendaId) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                posVendaService.encerrar(
                        empresaId, ordemServicoId, posVendaId
                )
        );
    }
}
