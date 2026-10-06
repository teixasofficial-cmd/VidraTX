package br.com.vidratx.controller;

import br.com.vidratx.dto.OrcamentoResponse;
import br.com.vidratx.dto.SolicitacaoOrcamentoResponse;
import br.com.vidratx.enums.StatusSolicitacaoOrcamento;
import br.com.vidratx.security.UsuarioAutenticado;
import br.com.vidratx.service.OrcamentoService;
import br.com.vidratx.service.SolicitacaoOrcamentoService;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/solicitacoes-orcamento")
@Validated
public class SolicitacaoOrcamentoController {

    private final SolicitacaoOrcamentoService solicitacaoOrcamentoService;
    private final OrcamentoService orcamentoService;

    public SolicitacaoOrcamentoController(
            SolicitacaoOrcamentoService solicitacaoOrcamentoService,
            OrcamentoService orcamentoService) {

        this.solicitacaoOrcamentoService = solicitacaoOrcamentoService;
        this.orcamentoService = orcamentoService;
    }

    @GetMapping
    public ResponseEntity<List<SolicitacaoOrcamentoResponse>> listar(
            @RequestParam(required = false) StatusSolicitacaoOrcamento status) {

        Long empresaId = UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                solicitacaoOrcamentoService.listar(empresaId, status)
        );
    }

    @GetMapping("/{solicitacaoId}")
    public ResponseEntity<SolicitacaoOrcamentoResponse> buscarPorId(
            @PathVariable
            @Positive(message = "ID da solicitação deve ser maior que zero")
            Long solicitacaoId) {

        Long empresaId = UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                solicitacaoOrcamentoService.buscarPorId(empresaId, solicitacaoId)
        );
    }

    @PostMapping("/{solicitacaoId}/criar-orcamento")
    public ResponseEntity<OrcamentoResponse> criarOrcamento(
            @PathVariable
            @Positive(message = "ID da solicitação deve ser maior que zero")
            Long solicitacaoId) {

        Long empresaId = UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                orcamentoService.criarAPartirDeSolicitacao(
                        empresaId, solicitacaoId, UsuarioAutenticado.get()
                )
        );
    }
}
