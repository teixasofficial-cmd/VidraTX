package br.com.vidratx.controller;

import br.com.vidratx.dto.InstalacaoReagendarRequest;
import br.com.vidratx.dto.InstalacaoRealizarRequest;
import br.com.vidratx.dto.InstalacaoRequest;
import br.com.vidratx.dto.InstalacaoResponse;
import br.com.vidratx.dto.CancelarAgendamentoRequest;
import br.com.vidratx.dto.PropostaAgendamentoResponse;
import br.com.vidratx.dto.RecusarContrapropostaRequest;
import br.com.vidratx.security.UsuarioAutenticado;
import br.com.vidratx.service.InstalacaoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/ordens-servico/{ordemServicoId}/instalacao")
@Validated
public class InstalacaoController {

    private final InstalacaoService instalacaoService;

    public InstalacaoController(
            InstalacaoService instalacaoService) {

        this.instalacaoService = instalacaoService;
    }

    @GetMapping
    public ResponseEntity<InstalacaoResponse> buscar(
            @PathVariable
            @Positive(message = "ID da ordem de serviço deve ser maior que zero")
            Long ordemServicoId) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                instalacaoService.buscar(empresaId, ordemServicoId)
        );
    }

    @PostMapping
    public ResponseEntity<InstalacaoResponse> agendar(
            @PathVariable
            @Positive(message = "ID da ordem de serviço deve ser maior que zero")
            Long ordemServicoId,

            @Valid @RequestBody InstalacaoRequest request) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        InstalacaoResponse response =
                instalacaoService.agendar(
                        empresaId,
                        ordemServicoId,
                        request,
                        UsuarioAutenticado.get()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/reagendar")
    public ResponseEntity<InstalacaoResponse> reagendar(
            @PathVariable
            @Positive(message = "ID da ordem de serviço deve ser maior que zero")
            Long ordemServicoId,

            @Valid @RequestBody InstalacaoReagendarRequest request) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                instalacaoService.reagendar(
                        empresaId,
                        ordemServicoId,
                        request,
                        UsuarioAutenticado.get()
                )
        );
    }

    @PutMapping("/aceitar-contraproposta")
    public ResponseEntity<InstalacaoResponse> aceitarContraproposta(
            @PathVariable
            @Positive(message = "ID da ordem de serviço deve ser maior que zero")
            Long ordemServicoId,

            @Valid @RequestBody InstalacaoReagendarRequest request) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                instalacaoService.aceitarContraproposta(
                        empresaId,
                        ordemServicoId,
                        request,
                        UsuarioAutenticado.get()
                )
        );
    }

    @PostMapping("/realizar")
    public ResponseEntity<InstalacaoResponse> realizar(
            @PathVariable
            @Positive(message = "ID da ordem de serviço deve ser maior que zero")
            Long ordemServicoId,

            @Valid @RequestBody InstalacaoRealizarRequest request) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                instalacaoService.realizar(
                        empresaId,
                        ordemServicoId,
                        request,
                        UsuarioAutenticado.get()
                )
        );
    }

    @PostMapping("/cancelar")
    public ResponseEntity<InstalacaoResponse> cancelar(
            @PathVariable
            @Positive(message = "ID do ordem de serviço deve ser maior que zero")
            Long ordemServicoId,

            @Valid @RequestBody(required = false) CancelarAgendamentoRequest request) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                instalacaoService.cancelar(
                        empresaId,
                        ordemServicoId,
                        request != null ? request.getMotivo() : null,
                        UsuarioAutenticado.get()
                )
        );
    }

    @GetMapping("/propostas")
    public ResponseEntity<List<PropostaAgendamentoResponse>> propostas(
            @PathVariable
            @Positive(message = "ID do ordem de serviço deve ser maior que zero")
            Long ordemServicoId) {

        return ResponseEntity.ok(
                instalacaoService.propostas(UsuarioAutenticado.getEmpresaId(), ordemServicoId)
        );
    }

    @PutMapping("/recusar-contraproposta")
    public ResponseEntity<InstalacaoResponse> recusarContraproposta(
            @PathVariable
            @Positive(message = "ID do ordem de serviço deve ser maior que zero")
            Long ordemServicoId,

            @Valid @RequestBody RecusarContrapropostaRequest request) {

        return ResponseEntity.ok(
                instalacaoService.recusarContraproposta(
                        UsuarioAutenticado.getEmpresaId(),
                        ordemServicoId,
                        request,
                        UsuarioAutenticado.get()
                )
        );
    }

    @PostMapping("/confirmar-manualmente")
    public ResponseEntity<InstalacaoResponse> confirmarManualmente(
            @PathVariable
            @Positive(message = "ID do ordem de serviço deve ser maior que zero")
            Long ordemServicoId) {

        return ResponseEntity.ok(
                instalacaoService.confirmarManualmente(
                        UsuarioAutenticado.getEmpresaId(),
                        ordemServicoId,
                        UsuarioAutenticado.get()
                )
        );
    }

    @PostMapping("/manter-data")
    public ResponseEntity<InstalacaoResponse> manterData(
            @PathVariable
            @Positive(message = "ID do ordem de serviço deve ser maior que zero")
            Long ordemServicoId) {

        return ResponseEntity.ok(
                instalacaoService.manterData(
                        UsuarioAutenticado.getEmpresaId(),
                        ordemServicoId,
                        UsuarioAutenticado.get()
                )
        );
    }
}
