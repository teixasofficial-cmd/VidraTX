package br.com.vidratx.controller;

import br.com.vidratx.dto.AgendaItemResponse;
import br.com.vidratx.dto.DashboardResumoResponse;
import br.com.vidratx.dto.ProximaAcaoItemResponse;
import br.com.vidratx.security.UsuarioAutenticado;
import br.com.vidratx.service.DashboardService;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/resumo")
    public ResponseEntity<DashboardResumoResponse> resumo() {

        Long empresaId = UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                dashboardService.resumo(empresaId)
        );
    }

    @GetMapping("/proximas-acoes")
    public ResponseEntity<List<ProximaAcaoItemResponse>> proximasAcoes() {

        Long empresaId = UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                dashboardService.proximasAcoes(empresaId)
        );
    }

    @GetMapping("/agenda")
    public ResponseEntity<List<AgendaItemResponse>> agenda(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate de,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate ate) {

        Long empresaId = UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                dashboardService.agenda(empresaId, de, ate)
        );
    }
}
