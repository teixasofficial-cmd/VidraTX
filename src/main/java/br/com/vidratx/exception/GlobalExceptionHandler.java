package br.com.vidratx.exception;

import br.com.vidratx.dto.ApiError;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.PessimisticLockingFailureException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log =
            LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiError> tratarErrosValidacao(
            MethodArgumentNotValidException ex) {

        Map<String, String> erros = new LinkedHashMap<>();

        ex.getBindingResult()
                .getFieldErrors()
                .forEach(erro ->
                        erros.put(
                                erro.getField(),
                                erro.getDefaultMessage()
                        )
                );

        return ResponseEntity
                .badRequest()
                .body(new ApiError(
                        HttpStatus.BAD_REQUEST.value(),
                        "Erro de validação",
                        erros
                ));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiError> tratarCorpoInvalido(
            HttpMessageNotReadableException ex) {

        return ResponseEntity
                .badRequest()
                .body(new ApiError(
                        HttpStatus.BAD_REQUEST.value(),
                        "Corpo da requisição inválido ou malformado"
                ));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ApiError> tratarViolacaoDeConstraint(
            ConstraintViolationException ex) {

        Map<String, String> erros = new LinkedHashMap<>();

        ex.getConstraintViolations()
                .forEach(violacao ->
                        erros.put(
                                violacao.getPropertyPath().toString(),
                                violacao.getMessage()
                        )
                );

        return ResponseEntity
                .badRequest()
                .body(new ApiError(
                        HttpStatus.BAD_REQUEST.value(),
                        "Erro de validação",
                        erros
                ));
    }

    @ExceptionHandler(AgendamentoInvalidoException.class)
    public ResponseEntity<ApiError> tratarAgendamentoInvalido(
            AgendamentoInvalidoException ex) {

        Map<String, String> erros = new LinkedHashMap<>();
        erros.put(ex.getCampo(), ex.getMessage());

        return ResponseEntity
                .badRequest()
                .body(new ApiError(
                        HttpStatus.BAD_REQUEST.value(),
                        ex.getMessage(),
                        erros
                ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiError> tratarArgumentoInvalido(
            IllegalArgumentException ex) {

        String mensagem = ex.getMessage();

        if (mensagem == null || mensagem.isBlank()) {
            mensagem = "Argumento inválido";
        }

        return ResponseEntity
                .badRequest()
                .body(new ApiError(
                        HttpStatus.BAD_REQUEST.value(),
                        mensagem
                ));
    }

    @ExceptionHandler(CnpjDuplicadoException.class)
    public ResponseEntity<ApiError> tratarCnpjDuplicado(
            CnpjDuplicadoException ex) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ApiError(
                        HttpStatus.CONFLICT.value(),
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(EmailUsuarioDuplicadoException.class)
    public ResponseEntity<ApiError> tratarEmailUsuarioDuplicado(
            EmailUsuarioDuplicadoException ex) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ApiError(
                        HttpStatus.CONFLICT.value(),
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(EmpresaNaoEncontradaException.class)
    public ResponseEntity<ApiError> tratarEmpresaNaoEncontrada(
            EmpresaNaoEncontradaException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApiError(
                        HttpStatus.NOT_FOUND.value(),
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(EntityNotFoundException.class)
    public ResponseEntity<ApiError> tratarRegistroNaoEncontrado(
            EntityNotFoundException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApiError(
                        HttpStatus.NOT_FOUND.value(),
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(UsuarioNaoEncontradoException.class)
    public ResponseEntity<ApiError> tratarUsuarioNaoEncontrado(
            UsuarioNaoEncontradoException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApiError(
                        HttpStatus.NOT_FOUND.value(),
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(EmpresaInativaException.class)
    public ResponseEntity<ApiError> tratarEmpresaInativa(
            EmpresaInativaException ex) {

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ApiError(
                        HttpStatus.FORBIDDEN.value(),
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(UsuarioInativoException.class)
    public ResponseEntity<ApiError> tratarUsuarioInativo(
            UsuarioInativoException ex) {

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ApiError(
                        HttpStatus.FORBIDDEN.value(),
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ApiError> tratarAcessoNegado(
            AccessDeniedException ex) {

        return ResponseEntity
                .status(HttpStatus.FORBIDDEN)
                .body(new ApiError(
                        HttpStatus.FORBIDDEN.value(),
                        "Você não possui permissão para realizar esta operação"
                ));
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ApiError> tratarBadCredentials(
            BadCredentialsException ex) {

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ApiError(
                        HttpStatus.UNAUTHORIZED.value(),
                        "E-mail ou senha inválidos"
                ));
    }

    @ExceptionHandler(CredenciaisInvalidasException.class)
    public ResponseEntity<ApiError> tratarCredenciaisInvalidas(
            CredenciaisInvalidasException ex) {

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ApiError(
                        HttpStatus.UNAUTHORIZED.value(),
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ApiError> tratarConflitoDeConcorrencia(
            ObjectOptimisticLockingFailureException ex) {

        log.warn(
                "Conflito de concorrência (lock otimista): {}",
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ApiError(
                        HttpStatus.CONFLICT.value(),
                        "Este registro foi alterado por outra ação ao mesmo tempo. "
                                + "Atualize a página e tente novamente."
                ));
    }

    @ExceptionHandler(PessimisticLockingFailureException.class)
    public ResponseEntity<ApiError> tratarTravaIndisponivel(
            PessimisticLockingFailureException ex) {

        log.warn("Trava pessimista indisponível: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ApiError(
                        HttpStatus.CONFLICT.value(),
                        "Este registro está sendo alterado por outra ação neste momento. "
                                + "Tente novamente em alguns segundos."
                ));
    }

    @ExceptionHandler(PendenciasAbertasException.class)
    public ResponseEntity<ApiError> tratarPendenciasAbertas(
            PendenciasAbertasException ex) {

        Map<String, String> pendencias = new LinkedHashMap<>();
        pendencias.put("codigo", "PENDENCIAS_ABERTAS");

        for (int i = 0; i < ex.getPendencias().size(); i++) {
            pendencias.put("pendencia" + (i + 1), ex.getPendencias().get(i));
        }

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ApiError(
                        HttpStatus.CONFLICT.value(),
                        ex.getMessage(),
                        pendencias
                ));
    }

    @ExceptionHandler(PrecoAlteradoException.class)
    public ResponseEntity<ApiError> tratarPrecoAlterado(
            PrecoAlteradoException ex) {

        Map<String, String> valores = new LinkedHashMap<>();
        valores.put("codigo", "PRECO_ALTERADO");
        valores.put("valorEsperado", ex.getValorEsperado().toPlainString());
        valores.put("valorAtual", ex.getValorAtual().toPlainString());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ApiError(
                        HttpStatus.CONFLICT.value(),
                        ex.getMessage(),
                        valores
                ));
    }

    @ExceptionHandler(MedicaoPendenteNoEnvioException.class)
    public ResponseEntity<ApiError> tratarMedicaoPendenteNoEnvio(
            MedicaoPendenteNoEnvioException ex) {

        Map<String, String> detalhes = new LinkedHashMap<>();
        detalhes.put("codigo", "MEDICAO_PENDENTE");
        detalhes.put("statusMedicao", ex.getStatusMedicao());
        detalhes.put("dataMedicao", ex.getDataMedicao());

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ApiError(
                        HttpStatus.CONFLICT.value(),
                        ex.getMessage(),
                        detalhes
                ));
    }

    @ExceptionHandler(ConflitoAgendaException.class)
    public ResponseEntity<ApiError> tratarConflitoAgenda(
            ConflitoAgendaException ex) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ApiError(
                        HttpStatus.CONFLICT.value(),
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiError> tratarRotaInexistente(
            NoResourceFoundException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApiError(
                        HttpStatus.NOT_FOUND.value(),
                        "Recurso não encontrado"
                ));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ApiError> tratarMetodoNaoSuportado(
            HttpRequestMethodNotSupportedException ex) {

        return ResponseEntity
                .status(HttpStatus.METHOD_NOT_ALLOWED)
                .body(new ApiError(
                        HttpStatus.METHOD_NOT_ALLOWED.value(),
                        "Método " + ex.getMethod() + " não é aceito neste endereço"
                ));
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    public ResponseEntity<ApiError> tratarCabecalhoAusente(
            MissingRequestHeaderException ex) {

        HttpStatus status = "Authorization".equalsIgnoreCase(ex.getHeaderName())
                ? HttpStatus.UNAUTHORIZED
                : HttpStatus.BAD_REQUEST;

        return ResponseEntity
                .status(status)
                .body(new ApiError(
                        status.value(),
                        "Cabeçalho obrigatório ausente: " + ex.getHeaderName()
                ));
    }

    @ExceptionHandler({MissingServletRequestParameterException.class, MethodArgumentTypeMismatchException.class})
    public ResponseEntity<ApiError> tratarParametroInvalido(
            Exception ex) {

        return ResponseEntity
                .badRequest()
                .body(new ApiError(
                        HttpStatus.BAD_REQUEST.value(),
                        "Parâmetro ausente ou inválido na requisição"
                ));
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiError> tratarViolacaoIntegridade(
            DataIntegrityViolationException ex) {

        log.warn(
                "Violação de integridade no banco de dados: {}",
                ex.getMessage()
        );

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ApiError(
                        HttpStatus.CONFLICT.value(),
                        "Não foi possível concluir a operação porque os dados violam uma restrição do banco de dados"
                ));
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiError> tratarErroGenerico(
            Exception ex) {

        log.error(
                "Erro não tratado ao processar a requisição",
                ex
        );

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ApiError(
                        HttpStatus.INTERNAL_SERVER_ERROR.value(),
                        "Ocorreu um erro interno no servidor"
                ));
    }

    @ExceptionHandler(SlugDuplicadoException.class)
    public ResponseEntity<ApiError> tratarSlugDuplicado(
            SlugDuplicadoException ex) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ApiError(
                        HttpStatus.CONFLICT.value(),
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(UltimoAdministradorException.class)
    public ResponseEntity<ApiError> tratarUltimoAdministrador(
            UltimoAdministradorException ex) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ApiError(
                        HttpStatus.CONFLICT.value(),
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(MuitasTentativasException.class)
    public ResponseEntity<ApiError> tratarMuitasTentativas(
            MuitasTentativasException ex) {

        return ResponseEntity
                .status(HttpStatus.TOO_MANY_REQUESTS)
                .body(new ApiError(
                        HttpStatus.TOO_MANY_REQUESTS.value(),
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(ClienteNaoEncontradoException.class)
    public ResponseEntity<ApiError> tratarClienteNaoEncontrado(
            ClienteNaoEncontradoException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApiError(
                        HttpStatus.NOT_FOUND.value(),
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(DocumentoDuplicadoException.class)
    public ResponseEntity<ApiError> tratarDocumentoDuplicado(
            DocumentoDuplicadoException ex) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ApiError(
                        HttpStatus.CONFLICT.value(),
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(ServicoNaoEncontradoException.class)
    public ResponseEntity<ApiError> tratarServicoNaoEncontrado(
            ServicoNaoEncontradoException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApiError(
                        HttpStatus.NOT_FOUND.value(),
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(ServicoDuplicadoException.class)
    public ResponseEntity<ApiError> tratarServicoDuplicado(
            ServicoDuplicadoException ex) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ApiError(
                        HttpStatus.CONFLICT.value(),
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(OrcamentoNaoEncontradoException.class)
    public ResponseEntity<ApiError> tratarOrcamentoNaoEncontrado(
            OrcamentoNaoEncontradoException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApiError(
                        HttpStatus.NOT_FOUND.value(),
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(OrcamentoNaoEditavelException.class)
    public ResponseEntity<ApiError> tratarOrcamentoNaoEditavel(
            OrcamentoNaoEditavelException ex) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ApiError(
                        HttpStatus.CONFLICT.value(),
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(TransicaoInvalidaException.class)
    public ResponseEntity<ApiError> tratarTransicaoInvalida(
            TransicaoInvalidaException ex) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ApiError(
                        HttpStatus.CONFLICT.value(),
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(MaterialNaoEncontradoException.class)
    public ResponseEntity<ApiError> tratarMaterialNaoEncontrado(
            MaterialNaoEncontradoException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApiError(
                        HttpStatus.NOT_FOUND.value(),
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(MaterialDuplicadoException.class)
    public ResponseEntity<ApiError> tratarMaterialDuplicado(
            MaterialDuplicadoException ex) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ApiError(
                        HttpStatus.CONFLICT.value(),
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(MaterialNecessarioNaoEncontradoException.class)
    public ResponseEntity<ApiError> tratarMaterialNecessarioNaoEncontrado(
            MaterialNecessarioNaoEncontradoException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApiError(
                        HttpStatus.NOT_FOUND.value(),
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(OrcamentoNaoAprovadoException.class)
    public ResponseEntity<ApiError> tratarOrcamentoNaoAprovado(
            OrcamentoNaoAprovadoException ex) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ApiError(
                        HttpStatus.CONFLICT.value(),
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(OrdemServicoNaoEncontradaException.class)
    public ResponseEntity<ApiError> tratarOrdemServicoNaoEncontrada(
            OrdemServicoNaoEncontradaException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApiError(
                        HttpStatus.NOT_FOUND.value(),
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(OrdemServicoJaExisteException.class)
    public ResponseEntity<ApiError> tratarOrdemServicoJaExiste(
            OrdemServicoJaExisteException ex) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ApiError(
                        HttpStatus.CONFLICT.value(),
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(ProducaoNaoAplicavelException.class)
    public ResponseEntity<ApiError> tratarProducaoNaoAplicavel(
            ProducaoNaoAplicavelException ex) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ApiError(
                        HttpStatus.CONFLICT.value(),
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(InstalacaoNaoEncontradaException.class)
    public ResponseEntity<ApiError> tratarInstalacaoNaoEncontrada(
            InstalacaoNaoEncontradaException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApiError(
                        HttpStatus.NOT_FOUND.value(),
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(InstalacaoJaExisteException.class)
    public ResponseEntity<ApiError> tratarInstalacaoJaExiste(
            InstalacaoJaExisteException ex) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ApiError(
                        HttpStatus.CONFLICT.value(),
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(InstalacaoNaoPermitidaException.class)
    public ResponseEntity<ApiError> tratarInstalacaoNaoPermitida(
            InstalacaoNaoPermitidaException ex) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ApiError(
                        HttpStatus.CONFLICT.value(),
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(MedicaoNaoEncontradaException.class)
    public ResponseEntity<ApiError> tratarMedicaoNaoEncontrada(
            MedicaoNaoEncontradaException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApiError(
                        HttpStatus.NOT_FOUND.value(),
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(MedicaoJaExisteException.class)
    public ResponseEntity<ApiError> tratarMedicaoJaExiste(
            MedicaoJaExisteException ex) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ApiError(
                        HttpStatus.CONFLICT.value(),
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(FotoNaoEncontradaException.class)
    public ResponseEntity<ApiError> tratarFotoNaoEncontrada(
            FotoNaoEncontradaException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApiError(
                        HttpStatus.NOT_FOUND.value(),
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(PagamentoNaoEncontradoException.class)
    public ResponseEntity<ApiError> tratarPagamentoNaoEncontrado(
            PagamentoNaoEncontradoException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApiError(
                        HttpStatus.NOT_FOUND.value(),
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(PagamentoNaoEditavelException.class)
    public ResponseEntity<ApiError> tratarPagamentoNaoEditavel(
            PagamentoNaoEditavelException ex) {

        return ResponseEntity
                .status(HttpStatus.CONFLICT)
                .body(new ApiError(
                        HttpStatus.CONFLICT.value(),
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(PosVendaNaoEncontradaException.class)
    public ResponseEntity<ApiError> tratarPosVendaNaoEncontrada(
            PosVendaNaoEncontradaException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApiError(
                        HttpStatus.NOT_FOUND.value(),
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(WhatsappInstanciaNaoEncontradaException.class)
    public ResponseEntity<ApiError> tratarWhatsappInstanciaNaoEncontrada(
            WhatsappInstanciaNaoEncontradaException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApiError(
                        HttpStatus.NOT_FOUND.value(),
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(AtendimentoNaoEncontradoException.class)
    public ResponseEntity<ApiError> tratarAtendimentoNaoEncontrado(
            AtendimentoNaoEncontradoException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApiError(
                        HttpStatus.NOT_FOUND.value(),
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(SolicitacaoOrcamentoNaoEncontradaException.class)
    public ResponseEntity<ApiError> tratarSolicitacaoOrcamentoNaoEncontrada(
            SolicitacaoOrcamentoNaoEncontradaException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApiError(
                        HttpStatus.NOT_FOUND.value(),
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(MensagemNaoEncontradaException.class)
    public ResponseEntity<ApiError> tratarMensagemNaoEncontrada(
            MensagemNaoEncontradaException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApiError(
                        HttpStatus.NOT_FOUND.value(),
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResponseEntity<ApiError> tratarArquivoMuitoGrande(
            MaxUploadSizeExceededException ex) {

        return ResponseEntity
                .status(HttpStatus.PAYLOAD_TOO_LARGE)
                .body(new ApiError(
                        HttpStatus.PAYLOAD_TOO_LARGE.value(),
                        "Arquivo excede o tamanho máximo permitido (10MB)"
                ));
    }

    @ExceptionHandler(TabelaPrecoNaoEncontradaException.class)
    public ResponseEntity<ApiError> tratarTabelaPrecoNaoEncontrada(
            TabelaPrecoNaoEncontradaException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApiError(
                        HttpStatus.NOT_FOUND.value(),
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(TipologiaNaoEncontradaException.class)
    public ResponseEntity<ApiError> tratarTipologiaNaoEncontrada(
            TipologiaNaoEncontradaException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApiError(
                        HttpStatus.NOT_FOUND.value(),
                        ex.getMessage()
                ));
    }

    @ExceptionHandler(OrcamentoItemNaoEncontradoException.class)
    public ResponseEntity<ApiError> tratarOrcamentoItemNaoEncontrado(
            OrcamentoItemNaoEncontradoException ex) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(new ApiError(
                        HttpStatus.NOT_FOUND.value(),
                        ex.getMessage()
                ));
    }

}
