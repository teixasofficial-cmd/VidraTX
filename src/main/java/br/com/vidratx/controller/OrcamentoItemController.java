package br.com.vidratx.controller;

import br.com.vidratx.dto.AjusteLinhaRequest;
import br.com.vidratx.dto.AjustePrecoFinalRequest;
import br.com.vidratx.dto.OrcamentoItemRequest;
import br.com.vidratx.dto.OrcamentoItemResponse;
import br.com.vidratx.dto.OrcamentoTotaisResponse;
import br.com.vidratx.dto.ParcelasCartaoRequest;
import br.com.vidratx.security.UsuarioAutenticado;
import br.com.vidratx.service.OrcamentoCalculoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orcamentos/{orcamentoId}")
@Validated
public class OrcamentoItemController {

    private final OrcamentoCalculoService orcamentoCalculoService;

    public OrcamentoItemController(OrcamentoCalculoService orcamentoCalculoService) {
        this.orcamentoCalculoService = orcamentoCalculoService;
    }

    @GetMapping("/itens")
    public ResponseEntity<List<OrcamentoItemResponse>> listarItens(
            @PathVariable @Positive Long orcamentoId) {

        Long empresaId = UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(orcamentoCalculoService.listarItens(empresaId, orcamentoId));
    }

    @PostMapping("/itens")
    public ResponseEntity<OrcamentoItemResponse> adicionarItem(
            @PathVariable @Positive Long orcamentoId,
            @Valid @RequestBody OrcamentoItemRequest request) {

        Long empresaId = UsuarioAutenticado.getEmpresaId();

        OrcamentoItemResponse response = orcamentoCalculoService.adicionarItem(
                empresaId, orcamentoId, request, UsuarioAutenticado.get(), UsuarioAutenticado.isAdminOuGerente()
        );

        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PutMapping("/itens/{itemId}")
    public ResponseEntity<OrcamentoItemResponse> atualizarItem(
            @PathVariable @Positive Long orcamentoId,
            @PathVariable @Positive Long itemId,
            @Valid @RequestBody OrcamentoItemRequest request) {

        Long empresaId = UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(orcamentoCalculoService.atualizarItem(
                empresaId, orcamentoId, itemId, request, UsuarioAutenticado.get(),
                UsuarioAutenticado.isAdminOuGerente()
        ));
    }

    @DeleteMapping("/itens/{itemId}")
    public ResponseEntity<Void> removerItem(
            @PathVariable @Positive Long orcamentoId,
            @PathVariable @Positive Long itemId) {

        Long empresaId = UsuarioAutenticado.getEmpresaId();

        orcamentoCalculoService.removerItem(empresaId, orcamentoId, itemId, UsuarioAutenticado.get());

        return ResponseEntity.noContent().build();
    }

    @PutMapping("/itens/{itemId}/linhas/{linhaId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<OrcamentoTotaisResponse> ajustarLinha(
            @PathVariable @Positive Long orcamentoId,
            @PathVariable @Positive Long itemId,
            @PathVariable @Positive Long linhaId,
            @Valid @RequestBody AjusteLinhaRequest request) {

        Long empresaId = UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(orcamentoCalculoService.ajustarLinha(
                empresaId, orcamentoId, itemId, linhaId, request.getValorFinal(), UsuarioAutenticado.get()
        ));
    }

    @PostMapping("/aceitar-sugestoes")
    public ResponseEntity<OrcamentoTotaisResponse> aceitarTodasSugestoes(
            @PathVariable @Positive Long orcamentoId) {

        Long empresaId = UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(orcamentoCalculoService.aceitarTodasSugestoes(
                empresaId, orcamentoId, UsuarioAutenticado.get()
        ));
    }

    @PutMapping("/preco-final")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<OrcamentoTotaisResponse> ajustarPrecoFinal(
            @PathVariable @Positive Long orcamentoId,
            @Valid @RequestBody AjustePrecoFinalRequest request) {

        Long empresaId = UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(orcamentoCalculoService.ajustarPrecoFinal(
                empresaId, orcamentoId, request, UsuarioAutenticado.get()
        ));
    }

    @PutMapping("/parcelas")
    @PreAuthorize("hasAnyRole('ADMIN', 'GERENTE')")
    public ResponseEntity<OrcamentoTotaisResponse> definirParcelas(
            @PathVariable @Positive Long orcamentoId,
            @Valid @RequestBody ParcelasCartaoRequest request) {

        Long empresaId = UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(orcamentoCalculoService.definirParcelas(
                empresaId, orcamentoId, request.getParcelas(), UsuarioAutenticado.get()
        ));
    }

    @GetMapping("/totais")
    public ResponseEntity<OrcamentoTotaisResponse> buscarTotais(
            @PathVariable @Positive Long orcamentoId) {

        Long empresaId = UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(orcamentoCalculoService.buscarTotais(empresaId, orcamentoId));
    }
}
