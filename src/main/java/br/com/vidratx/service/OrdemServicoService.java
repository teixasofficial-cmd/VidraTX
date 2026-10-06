package br.com.vidratx.service;

import br.com.vidratx.dto.OrdemServicoRequest;
import br.com.vidratx.dto.OrdemServicoResponse;
import br.com.vidratx.dto.OrdemServicoUpdateRequest;
import br.com.vidratx.entity.Empresa;
import br.com.vidratx.entity.Orcamento;
import br.com.vidratx.entity.OrdemServico;
import br.com.vidratx.entity.Usuario;
import br.com.vidratx.enums.StatusOrcamento;
import br.com.vidratx.enums.StatusProducao;
import br.com.vidratx.enums.TipoEventoHistorico;
import br.com.vidratx.exception.EmpresaNaoEncontradaException;
import br.com.vidratx.exception.OrcamentoNaoAprovadoException;
import br.com.vidratx.exception.OrcamentoNaoEncontradoException;
import br.com.vidratx.exception.OrdemServicoNaoEncontradaException;
import br.com.vidratx.exception.ProducaoNaoAplicavelException;
import br.com.vidratx.exception.TransicaoInvalidaException;
import br.com.vidratx.mapper.OrdemServicoMapper;
import br.com.vidratx.repository.EmpresaRepository;
import br.com.vidratx.repository.OrcamentoRepository;
import br.com.vidratx.repository.OrdemServicoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

@Service
public class OrdemServicoService {

    private final OrdemServicoRepository ordemServicoRepository;
    private final OrcamentoRepository orcamentoRepository;
    private final EmpresaRepository empresaRepository;
    private final OrdemServicoMapper ordemServicoMapper;
    private final HistoricoService historicoService;
    private final Clock clock;

    public OrdemServicoService(
            OrdemServicoRepository ordemServicoRepository,
            OrcamentoRepository orcamentoRepository,
            EmpresaRepository empresaRepository,
            OrdemServicoMapper ordemServicoMapper,
            HistoricoService historicoService,
            Clock clock) {

        this.ordemServicoRepository = ordemServicoRepository;
        this.orcamentoRepository = orcamentoRepository;
        this.empresaRepository = empresaRepository;
        this.ordemServicoMapper = ordemServicoMapper;
        this.historicoService = historicoService;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<OrdemServicoResponse> listarPorEmpresa(
            Long empresaId,
            StatusProducao statusProducao) {

        verificarEmpresa(empresaId);

        List<OrdemServico> ordens =
                statusProducao != null
                        ? ordemServicoRepository
                        .findAllByEmpresaIdAndStatusProducaoOrderByCriadoEmDesc(
                                empresaId, statusProducao
                        )
                        : ordemServicoRepository
                        .findAllByEmpresaIdOrderByCriadoEmDesc(
                                empresaId
                        );

        return ordens
                .stream()
                .map(ordemServicoMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public OrdemServicoResponse buscarPorId(
            Long empresaId,
            Long ordemServicoId) {

        return ordemServicoMapper.toResponse(
                buscarOrdemServico(empresaId, ordemServicoId)
        );
    }

    @Transactional
    public OrdemServicoResponse criar(
            Long empresaId,
            OrdemServicoRequest request,
            Usuario responsavel) {

        Empresa empresa =
                buscarEmpresa(empresaId);

        Orcamento orcamento =
                orcamentoRepository
                        .findByIdAndEmpresaId(
                                request.getOrcamentoId(), empresaId
                        )
                        .orElseThrow(() ->
                                new OrcamentoNaoEncontradoException(
                                        "Orçamento não encontrado"
                                )
                        );

        if (orcamento.getStatus() != StatusOrcamento.APROVADO) {

            throw new OrcamentoNaoAprovadoException(
                    "Só é possível abrir uma ordem de serviço para um orçamento aprovado"
            );
        }

        Optional<OrdemServico> existente = ordemServicoRepository.findByOrcamentoId(orcamento.getId());

        if (existente.isPresent()) {
            return ordemServicoMapper.toResponse(existente.get());
        }

        OrdemServico ordemServico =
                ordemServicoMapper.toEntity(request, empresa, orcamento);

        OrdemServico salva =
                ordemServicoRepository.saveAndFlush(ordemServico);

        historicoService.registrarEventoOrcamento(
                orcamento, TipoEventoHistorico.ORDEM_SERVICO_CRIADA,
                "Ordem de serviço nº " + salva.getId() + " aberta", responsavel
        );

        return ordemServicoMapper.toResponse(salva);
    }

    @Transactional(propagation = Propagation.MANDATORY)
    public OrdemServico criarAutomaticamente(Orcamento orcamento, Usuario responsavel) {

        Optional<OrdemServico> existente = ordemServicoRepository.findByOrcamentoId(orcamento.getId());

        if (existente.isPresent()) {
            return existente.get();
        }

        OrdemServico ordemServico = new OrdemServico();

        ordemServico.setEmpresa(orcamento.getEmpresa());
        ordemServico.setOrcamento(orcamento);
        ordemServico.setNecessitaProducao(Boolean.TRUE);

        OrdemServico salva = ordemServicoRepository.saveAndFlush(ordemServico);

        historicoService.registrarEventoOrcamento(
                orcamento, TipoEventoHistorico.ORDEM_SERVICO_CRIADA,
                "Ordem de serviço nº " + salva.getId() + " aberta automaticamente com a aprovação",
                responsavel
        );

        return salva;
    }

    @Transactional
    public OrdemServicoResponse alterarNecessitaProducao(
            Long empresaId,
            Long ordemServicoId,
            boolean necessitaProducao) {

        OrdemServico ordemServico =
                buscarOrdemServico(empresaId, ordemServicoId);

        if (Boolean.valueOf(necessitaProducao).equals(ordemServico.getNecessitaProducao())) {
            return ordemServicoMapper.toResponse(ordemServico);
        }

        if (!necessitaProducao
                && ordemServico.getStatusProducao() != null
                && ordemServico.getStatusProducao() != StatusProducao.AGUARDANDO_PRODUCAO) {

            throw new TransicaoInvalidaException(
                    "A produção já começou (" + ordemServico.getStatusProducao()
                            + "): não dá para dispensá-la"
            );
        }

        ordemServico.setNecessitaProducao(necessitaProducao);
        ordemServico.setStatusProducao(necessitaProducao ? StatusProducao.AGUARDANDO_PRODUCAO : null);

        return ordemServicoMapper.toResponse(
                ordemServicoRepository.save(ordemServico)
        );
    }

    @Transactional
    public OrdemServicoResponse atualizar(
            Long empresaId,
            Long ordemServicoId,
            OrdemServicoUpdateRequest request) {

        OrdemServico ordemServico =
                buscarOrdemServico(empresaId, ordemServicoId);

        ordemServico.setObservacoes(
                normalizar(request.getObservacoes())
        );

        return ordemServicoMapper.toResponse(
                ordemServicoRepository.save(ordemServico)
        );
    }

    @Transactional
    public OrdemServicoResponse iniciarProducao(
            Long empresaId,
            Long ordemServicoId) {

        return transicionar(
                empresaId,
                ordemServicoId,
                StatusProducao.AGUARDANDO_PRODUCAO,
                StatusProducao.EM_PRODUCAO,
                OrdemServico::setProducaoIniciadaEm
        );
    }

    @Transactional
    public OrdemServicoResponse concluirProducao(
            Long empresaId,
            Long ordemServicoId) {

        return transicionar(
                empresaId,
                ordemServicoId,
                StatusProducao.EM_PRODUCAO,
                StatusProducao.PRONTO,
                OrdemServico::setProducaoConcluidaEm
        );
    }

    @Transactional
    public OrdemServicoResponse conferirProducao(
            Long empresaId,
            Long ordemServicoId) {

        return transicionar(
                empresaId,
                ordemServicoId,
                StatusProducao.PRONTO,
                StatusProducao.CONFERIDO,
                OrdemServico::setProducaoConferidaEm
        );
    }

    private OrdemServicoResponse transicionar(
            Long empresaId,
            Long ordemServicoId,
            StatusProducao statusEsperado,
            StatusProducao novoStatus,
            BiConsumer<OrdemServico, LocalDateTime> marcarData) {

        OrdemServico ordemServico =
                buscarOrdemServico(empresaId, ordemServicoId);

        if (!Boolean.TRUE.equals(ordemServico.getNecessitaProducao())) {

            throw new ProducaoNaoAplicavelException(
                    "Esta ordem de serviço não passa pela etapa de produção"
            );
        }

        if (ordemServico.getStatusProducao() != statusEsperado) {

            throw new TransicaoInvalidaException(
                    "Esta ação exige que a produção esteja com status "
                            + statusEsperado
                            + ", mas está "
                            + ordemServico.getStatusProducao()
            );
        }

        ordemServico.setStatusProducao(novoStatus);
        marcarData.accept(ordemServico, LocalDateTime.now(clock));

        OrdemServico salva =
                ordemServicoRepository.save(ordemServico);

        return ordemServicoMapper.toResponse(salva);
    }

    private Empresa buscarEmpresa(Long empresaId) {

        return empresaRepository
                .findById(empresaId)
                .orElseThrow(() ->
                        new EmpresaNaoEncontradaException(
                                "Empresa não encontrada"
                        )
                );
    }

    private void verificarEmpresa(Long empresaId) {
        buscarEmpresa(empresaId);
    }

    private OrdemServico buscarOrdemServico(
            Long empresaId,
            Long ordemServicoId) {

        return ordemServicoRepository
                .findByIdAndEmpresaId(ordemServicoId, empresaId)
                .orElseThrow(() ->
                        new OrdemServicoNaoEncontradaException(
                                "Ordem de serviço não encontrada"
                        )
                );
    }

    private String normalizar(String valor) {

        if (valor == null || valor.isBlank()) {
            return null;
        }

        return valor.trim();
    }
}
