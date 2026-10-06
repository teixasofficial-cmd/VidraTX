package br.com.vidratx.controller;

import br.com.vidratx.dto.HistoricoResponse;
import br.com.vidratx.security.UsuarioAutenticado;
import br.com.vidratx.service.HistoricoService;
import jakarta.validation.constraints.Positive;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/clientes/{clienteId}/historico")
@Validated
public class HistoricoController {

    private final HistoricoService historicoService;

    public HistoricoController(HistoricoService historicoService) {
        this.historicoService = historicoService;
    }

    @GetMapping
    public ResponseEntity<List<HistoricoResponse>> listar(
            @PathVariable
            @Positive(message = "ID do cliente deve ser maior que zero")
            Long clienteId) {

        Long empresaId = UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                historicoService.listarPorCliente(empresaId, clienteId)
        );
    }
}
