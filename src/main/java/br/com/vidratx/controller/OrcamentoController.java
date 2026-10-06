package br.com.vidratx.controller;

import br.com.vidratx.dto.EnviarOrcamentoRequest;
import br.com.vidratx.dto.MensagemAtendimentoResponse;
import br.com.vidratx.dto.MoverPipelineRequest;
import br.com.vidratx.dto.OrcamentoRequest;
import br.com.vidratx.dto.OrcamentoResponse;
import br.com.vidratx.dto.PerderOrcamentoRequest;
import br.com.vidratx.dto.ReabrirOrcamentoRequest;
import br.com.vidratx.enums.StatusOrcamento;
import br.com.vidratx.security.UsuarioAutenticado;
import br.com.vidratx.service.OrcamentoService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/orcamentos")
@Validated
public class OrcamentoController {

    private final OrcamentoService orcamentoService;

    public OrcamentoController(
            OrcamentoService orcamentoService) {

        this.orcamentoService = orcamentoService;
    }

    @GetMapping
    public ResponseEntity<List<OrcamentoResponse>> listar(
            @RequestParam(required = false) StatusOrcamento status,
            @RequestParam(required = false) Long clienteId) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                orcamentoService.listarPorEmpresa(
                        empresaId,
                        status,
                        clienteId
                )
        );
    }

    @GetMapping("/{orcamentoId}")
    public ResponseEntity<OrcamentoResponse> buscarPorId(
            @PathVariable
            @Positive(message = "ID do orçamento deve ser maior que zero")
            Long orcamentoId) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                orcamentoService.buscarPorId(
                        empresaId,
                        orcamentoId
                )
        );
    }

    @GetMapping("/{orcamentoId}/fotos-whatsapp")
    public ResponseEntity<List<MensagemAtendimentoResponse>> fotosWhatsapp(
            @PathVariable
            @Positive(message = "ID do orçamento deve ser maior que zero")
            Long orcamentoId) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                orcamentoService.listarFotosWhatsapp(empresaId, orcamentoId)
        );
    }

    @PostMapping
    public ResponseEntity<OrcamentoResponse> criar(
            @Valid @RequestBody OrcamentoRequest request) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        OrcamentoResponse response =
                orcamentoService.criar(empresaId, request, UsuarioAutenticado.get());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{orcamentoId}")
    public ResponseEntity<OrcamentoResponse> atualizar(
            @PathVariable
            @Positive(message = "ID do orçamento deve ser maior que zero")
            Long orcamentoId,

            @Valid @RequestBody OrcamentoRequest request) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                orcamentoService.atualizar(
                        empresaId,
                        orcamentoId,
                        request,
                        UsuarioAutenticado.get()
                )
        );
    }

    @DeleteMapping("/{orcamentoId}")
    public ResponseEntity<Void> excluir(
            @PathVariable
            @Positive(message = "ID do orçamento deve ser maior que zero")
            Long orcamentoId) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        orcamentoService.excluir(empresaId, orcamentoId);

        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{orcamentoId}/enviar")
    public ResponseEntity<OrcamentoResponse> enviar(
            @PathVariable
            @Positive(message = "ID do orçamento deve ser maior que zero")
            Long orcamentoId,

            @RequestBody(required = false) EnviarOrcamentoRequest request) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                orcamentoService.enviar(
                        empresaId, orcamentoId,
                        request != null ? request.getValorEsperado() : null,
                        request != null ? request.getMedicaoPendente() : null,
                        UsuarioAutenticado.get()
                )
        );
    }

    @PostMapping("/{orcamentoId}/enviar-estimativa")
    public ResponseEntity<OrcamentoResponse> enviarEstimativa(
            @PathVariable
            @Positive(message = "ID do orçamento deve ser maior que zero")
            Long orcamentoId) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                orcamentoService.enviarEstimativa(empresaId, orcamentoId, UsuarioAutenticado.get())
        );
    }

    @PostMapping("/{orcamentoId}/revisar")
    public ResponseEntity<OrcamentoResponse> revisar(
            @PathVariable
            @Positive(message = "ID do orçamento deve ser maior que zero")
            Long orcamentoId) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                orcamentoService.revisar(empresaId, orcamentoId, UsuarioAutenticado.get())
        );
    }

    @PostMapping("/{orcamentoId}/aprovar")
    public ResponseEntity<OrcamentoResponse> aprovar(
            @PathVariable
            @Positive(message = "ID do orçamento deve ser maior que zero")
            Long orcamentoId) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                orcamentoService.aprovar(empresaId, orcamentoId, UsuarioAutenticado.get())
        );
    }

    @PostMapping("/{orcamentoId}/perder")
    public ResponseEntity<OrcamentoResponse> perder(
            @PathVariable
            @Positive(message = "ID do orçamento deve ser maior que zero")
            Long orcamentoId,

            @Valid @RequestBody PerderOrcamentoRequest request) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                orcamentoService.perder(
                        empresaId, orcamentoId,
                        request.getMotivoPerda(), request.getMotivoPerdaOutro(),
                        UsuarioAutenticado.get()
                )
        );
    }

    @PostMapping("/{orcamentoId}/reabrir")
    public ResponseEntity<OrcamentoResponse> reabrir(
            @PathVariable
            @Positive(message = "ID do orçamento deve ser maior que zero")
            Long orcamentoId,

            @Valid @RequestBody(required = false) ReabrirOrcamentoRequest request) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                orcamentoService.reabrir(
                        empresaId, orcamentoId,
                        request != null ? request.getMotivo() : null,
                        UsuarioAutenticado.get()
                )
        );
    }

    @PostMapping("/{orcamentoId}/mover-pipeline")
    public ResponseEntity<OrcamentoResponse> moverPipeline(
            @PathVariable
            @Positive(message = "ID do orçamento deve ser maior que zero")
            Long orcamentoId,

            @Valid @RequestBody MoverPipelineRequest request) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                orcamentoService.moverPipeline(
                        empresaId, orcamentoId, request.getStatus(),
                        request.getMotivoPerda(), request.getMotivoPerdaOutro(),
                        UsuarioAutenticado.get()
                )
        );
    }

    @PostMapping("/{orcamentoId}/expirar")
    public ResponseEntity<OrcamentoResponse> expirar(
            @PathVariable
            @Positive(message = "ID do orçamento deve ser maior que zero")
            Long orcamentoId) {

        Long empresaId =
                UsuarioAutenticado.getEmpresaId();

        return ResponseEntity.ok(
                orcamentoService.expirar(empresaId, orcamentoId, UsuarioAutenticado.get())
        );
    }
}
