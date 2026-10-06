package br.com.vidratx.controller;

import br.com.vidratx.dto.AtendenteResponse;
import br.com.vidratx.dto.AtendimentoWhatsappDetalheResponse;
import br.com.vidratx.dto.AtendimentoWhatsappResponse;
import br.com.vidratx.dto.AtendimentosPendentesResponse;
import br.com.vidratx.dto.EncerrarAtendimentoRequest;
import br.com.vidratx.dto.ResponderAtendimentoRequest;
import br.com.vidratx.dto.TransferirAtendimentoRequest;
import br.com.vidratx.dto.VincularClienteRequest;
import br.com.vidratx.enums.StatusAtendimento;
import br.com.vidratx.security.UsuarioAutenticado;
import br.com.vidratx.service.AtendimentoWhatsappService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.core.io.Resource;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/atendimentos-whatsapp")
@Validated
public class AtendimentoWhatsappController {

    private final AtendimentoWhatsappService atendimentoWhatsappService;

    public AtendimentoWhatsappController(
            AtendimentoWhatsappService atendimentoWhatsappService) {

        this.atendimentoWhatsappService = atendimentoWhatsappService;
    }

    @GetMapping
    public ResponseEntity<List<AtendimentoWhatsappResponse>> listarTodos(
            @RequestParam(required = false) StatusAtendimento status) {

        Long empresaId = UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                atendimentoWhatsappService.listarTodos(empresaId, status)
        );
    }

    @GetMapping("/atendentes")
    public ResponseEntity<List<AtendenteResponse>> listarAtendentes() {

        return ResponseEntity.ok(
                atendimentoWhatsappService.listarAtendentes(UsuarioAutenticado.getEmpresaId())
        );
    }

    @GetMapping("/pendentes")
    public ResponseEntity<List<AtendimentoWhatsappResponse>> listarPendentes() {

        Long empresaId = UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                atendimentoWhatsappService.listarPendentes(empresaId)
        );
    }

    @GetMapping("/pendentes/contagem")
    public ResponseEntity<AtendimentosPendentesResponse> contarPendentes() {

        Long empresaId = UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                new AtendimentosPendentesResponse(
                        atendimentoWhatsappService.contarPendentes(empresaId)
                )
        );
    }

    @GetMapping("/{atendimentoId}")
    public ResponseEntity<AtendimentoWhatsappDetalheResponse> detalhar(
            @PathVariable
            @Positive(message = "ID do atendimento deve ser maior que zero")
            Long atendimentoId) {

        Long empresaId = UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                atendimentoWhatsappService.detalhar(empresaId, atendimentoId)
        );
    }

    @GetMapping("/midia/{mensagemId}")
    public ResponseEntity<Resource> midia(
            @PathVariable
            @Positive(message = "ID da mensagem deve ser maior que zero")
            Long mensagemId) {

        Long empresaId = UsuarioAutenticado.getEmpresaId();

        return atendimentoWhatsappService.carregarMidia(empresaId, mensagemId);
    }

    @PostMapping("/{atendimentoId}/assumir")
    public ResponseEntity<AtendimentoWhatsappResponse> assumir(
            @PathVariable
            @Positive(message = "ID do atendimento deve ser maior que zero")
            Long atendimentoId) {

        Long empresaId = UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                atendimentoWhatsappService.assumir(
                        empresaId, atendimentoId, UsuarioAutenticado.get()
                )
        );
    }

    @PostMapping("/{atendimentoId}/responder")
    public ResponseEntity<AtendimentoWhatsappResponse> responder(
            @PathVariable
            @Positive(message = "ID do atendimento deve ser maior que zero")
            Long atendimentoId,

            @Valid @RequestBody ResponderAtendimentoRequest request) {

        Long empresaId = UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                atendimentoWhatsappService.responder(
                        empresaId,
                        atendimentoId,
                        UsuarioAutenticado.get(),
                        request.getMensagem()
                )
        );
    }

    @PostMapping("/{atendimentoId}/encerrar")
    public ResponseEntity<AtendimentoWhatsappResponse> encerrar(
            @PathVariable
            @Positive(message = "ID do atendimento deve ser maior que zero")
            Long atendimentoId,

            @Valid @RequestBody(required = false) EncerrarAtendimentoRequest request) {

        Long empresaId = UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                atendimentoWhatsappService.encerrar(
                        empresaId, atendimentoId, UsuarioAutenticado.get(),
                        request != null && request.isForcar(),
                        request != null ? request.getMotivo() : null
                )
        );
    }

    @PostMapping("/{atendimentoId}/transferir")
    public ResponseEntity<AtendimentoWhatsappResponse> transferir(
            @PathVariable
            @Positive(message = "ID do atendimento deve ser maior que zero")
            Long atendimentoId,

            @Valid @RequestBody TransferirAtendimentoRequest request) {

        Long empresaId = UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                atendimentoWhatsappService.transferir(
                        empresaId, atendimentoId, request.getUsuarioId(), UsuarioAutenticado.get()
                )
        );
    }

    @PostMapping("/{atendimentoId}/reabrir")
    public ResponseEntity<AtendimentoWhatsappResponse> reabrir(
            @PathVariable
            @Positive(message = "ID do atendimento deve ser maior que zero")
            Long atendimentoId) {

        Long empresaId = UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                atendimentoWhatsappService.reabrir(empresaId, atendimentoId, UsuarioAutenticado.get())
        );
    }

    @PostMapping("/{atendimentoId}/devolver-ao-bot")
    public ResponseEntity<AtendimentoWhatsappResponse> devolverAoBot(
            @PathVariable
            @Positive(message = "ID do atendimento deve ser maior que zero")
            Long atendimentoId) {

        Long empresaId = UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                atendimentoWhatsappService.devolverAoBot(empresaId, atendimentoId, UsuarioAutenticado.get())
        );
    }

    @PostMapping("/{atendimentoId}/vincular-cliente")
    public ResponseEntity<AtendimentoWhatsappResponse> vincularCliente(
            @PathVariable
            @Positive(message = "ID do atendimento deve ser maior que zero")
            Long atendimentoId,

            @Valid @RequestBody VincularClienteRequest request) {

        Long empresaId = UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                atendimentoWhatsappService.vincularCliente(
                        empresaId, atendimentoId, request.getClienteId(), UsuarioAutenticado.get()
                )
        );
    }
}
