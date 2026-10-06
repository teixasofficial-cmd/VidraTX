package br.com.vidratx.controller;

import br.com.vidratx.dto.PagamentoRequest;
import br.com.vidratx.dto.PagamentoResponse;
import br.com.vidratx.dto.ResumoFinanceiroResponse;
import br.com.vidratx.security.UsuarioAutenticado;
import br.com.vidratx.service.PagamentoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orcamentos/{orcamentoId}/pagamentos")
@Validated
public class PagamentoController {

    private final PagamentoService pagamentoService;

    public PagamentoController(
            PagamentoService pagamentoService) {

        this.pagamentoService = pagamentoService;
    }

    @GetMapping
    public ResponseEntity<List<PagamentoResponse>> listar(
            @PathVariable
            @Positive(message = "ID do orçamento deve ser maior que zero")
            Long orcamentoId) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                pagamentoService.listar(empresaId, orcamentoId)
        );
    }

    @GetMapping("/resumo")
    public ResponseEntity<ResumoFinanceiroResponse> resumo(
            @PathVariable
            @Positive(message = "ID do orçamento deve ser maior que zero")
            Long orcamentoId) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                pagamentoService.resumo(empresaId, orcamentoId)
        );
    }

    @PostMapping
    public ResponseEntity<PagamentoResponse> adicionar(
            @PathVariable
            @Positive(message = "ID do orçamento deve ser maior que zero")
            Long orcamentoId,

            @Valid @RequestBody PagamentoRequest request) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        PagamentoResponse response =
                pagamentoService.adicionar(
                        empresaId, orcamentoId, request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PostMapping("/{pagamentoId}/marcar-pago")
    public ResponseEntity<PagamentoResponse> marcarComoPago(
            @PathVariable
            @Positive(message = "ID do orçamento deve ser maior que zero")
            Long orcamentoId,

            @PathVariable
            @Positive(message = "ID do pagamento deve ser maior que zero")
            Long pagamentoId) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                pagamentoService.marcarComoPago(
                        empresaId, orcamentoId, pagamentoId
                )
        );
    }

    @DeleteMapping("/{pagamentoId}")
    public ResponseEntity<Void> remover(
            @PathVariable
            @Positive(message = "ID do orçamento deve ser maior que zero")
            Long orcamentoId,

            @PathVariable
            @Positive(message = "ID do pagamento deve ser maior que zero")
            Long pagamentoId) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        pagamentoService.remover(empresaId, orcamentoId, pagamentoId);

        return ResponseEntity.noContent().build();
    }
}
