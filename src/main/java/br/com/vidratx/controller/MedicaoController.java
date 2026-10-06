package br.com.vidratx.controller;

import br.com.vidratx.dto.MedicaoReagendarRequest;
import br.com.vidratx.dto.MedicaoRealizarRequest;
import br.com.vidratx.dto.MedicaoRequest;
import br.com.vidratx.dto.MedicaoResponse;
import br.com.vidratx.dto.CancelarAgendamentoRequest;
import br.com.vidratx.dto.PropostaAgendamentoResponse;
import br.com.vidratx.dto.RecusarContrapropostaRequest;
import br.com.vidratx.security.UsuarioAutenticado;
import br.com.vidratx.service.MedicaoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orcamentos/{orcamentoId}/medicao")
@Validated
public class MedicaoController {

    private final MedicaoService medicaoService;

    public MedicaoController(
            MedicaoService medicaoService) {

        this.medicaoService = medicaoService;
    }

    @GetMapping
    public ResponseEntity<MedicaoResponse> buscar(
            @PathVariable
            @Positive(message = "ID do orçamento deve ser maior que zero")
            Long orcamentoId) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                medicaoService.buscar(empresaId, orcamentoId)
        );
    }

    @PostMapping
    public ResponseEntity<MedicaoResponse> agendar(
            @PathVariable
            @Positive(message = "ID do orçamento deve ser maior que zero")
            Long orcamentoId,

            @Valid @RequestBody MedicaoRequest request) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        MedicaoResponse response =
                medicaoService.agendar(
                        empresaId,
                        orcamentoId,
                        request,
                        UsuarioAutenticado.get()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/reagendar")
    public ResponseEntity<MedicaoResponse> reagendar(
            @PathVariable
            @Positive(message = "ID do orçamento deve ser maior que zero")
            Long orcamentoId,

            @Valid @RequestBody MedicaoReagendarRequest request) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                medicaoService.reagendar(
                        empresaId,
                        orcamentoId,
                        request,
                        UsuarioAutenticado.get()
                )
        );
    }

    @PutMapping("/aceitar-contraproposta")
    public ResponseEntity<MedicaoResponse> aceitarContraproposta(
            @PathVariable
            @Positive(message = "ID do orçamento deve ser maior que zero")
            Long orcamentoId,

            @Valid @RequestBody MedicaoReagendarRequest request) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                medicaoService.aceitarContraproposta(
                        empresaId,
                        orcamentoId,
                        request,
                        UsuarioAutenticado.get()
                )
        );
    }

    @PostMapping("/realizar")
    public ResponseEntity<MedicaoResponse> realizar(
            @PathVariable
            @Positive(message = "ID do orçamento deve ser maior que zero")
            Long orcamentoId,

            @Valid @RequestBody MedicaoRealizarRequest request) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                medicaoService.realizar(
                        empresaId,
                        orcamentoId,
                        request,
                        UsuarioAutenticado.get()
                )
        );
    }

    @PostMapping("/cancelar")
    public ResponseEntity<MedicaoResponse> cancelar(
            @PathVariable
            @Positive(message = "ID do orçamento deve ser maior que zero")
            Long orcamentoId,

            @Valid @RequestBody(required = false) CancelarAgendamentoRequest request) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                medicaoService.cancelar(
                        empresaId,
                        orcamentoId,
                        request != null ? request.getMotivo() : null,
                        UsuarioAutenticado.get()
                )
        );
    }

    @GetMapping("/propostas")
    public ResponseEntity<List<PropostaAgendamentoResponse>> propostas(
            @PathVariable
            @Positive(message = "ID do orçamento deve ser maior que zero")
            Long orcamentoId) {

        return ResponseEntity.ok(
                medicaoService.propostas(UsuarioAutenticado.getEmpresaId(), orcamentoId)
        );
    }

    @PutMapping("/recusar-contraproposta")
    public ResponseEntity<MedicaoResponse> recusarContraproposta(
            @PathVariable
            @Positive(message = "ID do orçamento deve ser maior que zero")
            Long orcamentoId,

            @Valid @RequestBody RecusarContrapropostaRequest request) {

        return ResponseEntity.ok(
                medicaoService.recusarContraproposta(
                        UsuarioAutenticado.getEmpresaId(),
                        orcamentoId,
                        request,
                        UsuarioAutenticado.get()
                )
        );
    }

    @PostMapping("/confirmar-manualmente")
    public ResponseEntity<MedicaoResponse> confirmarManualmente(
            @PathVariable
            @Positive(message = "ID do orçamento deve ser maior que zero")
            Long orcamentoId) {

        return ResponseEntity.ok(
                medicaoService.confirmarManualmente(
                        UsuarioAutenticado.getEmpresaId(),
                        orcamentoId,
                        UsuarioAutenticado.get()
                )
        );
    }

    @PostMapping("/manter-data")
    public ResponseEntity<MedicaoResponse> manterData(
            @PathVariable
            @Positive(message = "ID do orçamento deve ser maior que zero")
            Long orcamentoId) {

        return ResponseEntity.ok(
                medicaoService.manterData(
                        UsuarioAutenticado.getEmpresaId(),
                        orcamentoId,
                        UsuarioAutenticado.get()
                )
        );
    }
}
