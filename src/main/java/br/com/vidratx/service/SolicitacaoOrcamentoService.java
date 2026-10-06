package br.com.vidratx.service;

import br.com.vidratx.dto.SolicitacaoOrcamentoResponse;
import br.com.vidratx.entity.SolicitacaoOrcamento;
import br.com.vidratx.enums.StatusSolicitacaoOrcamento;
import br.com.vidratx.exception.SolicitacaoOrcamentoNaoEncontradaException;
import br.com.vidratx.mapper.SolicitacaoOrcamentoMapper;
import br.com.vidratx.repository.SolicitacaoOrcamentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SolicitacaoOrcamentoService {

    private final SolicitacaoOrcamentoRepository solicitacaoOrcamentoRepository;
    private final SolicitacaoOrcamentoMapper solicitacaoOrcamentoMapper;

    public SolicitacaoOrcamentoService(
            SolicitacaoOrcamentoRepository solicitacaoOrcamentoRepository,
            SolicitacaoOrcamentoMapper solicitacaoOrcamentoMapper) {

        this.solicitacaoOrcamentoRepository = solicitacaoOrcamentoRepository;
        this.solicitacaoOrcamentoMapper = solicitacaoOrcamentoMapper;
    }

    @Transactional(readOnly = true)
    public List<SolicitacaoOrcamentoResponse> listar(
            Long empresaId,
            StatusSolicitacaoOrcamento status) {

        List<SolicitacaoOrcamento> solicitacoes = status == null
                ? solicitacaoOrcamentoRepository.findAllByEmpresaIdOrderByCriadoEmDesc(empresaId)
                : solicitacaoOrcamentoRepository
                        .findAllByEmpresaIdAndStatusOrderByCriadoEmDesc(empresaId, status);

        return solicitacoes.stream()
                .map(solicitacaoOrcamentoMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public SolicitacaoOrcamentoResponse buscarPorId(
            Long empresaId,
            Long solicitacaoId) {

        SolicitacaoOrcamento solicitacao = solicitacaoOrcamentoRepository
                .findByIdAndEmpresaId(solicitacaoId, empresaId)
                .orElseThrow(() ->
                        new SolicitacaoOrcamentoNaoEncontradaException(
                                "Solicitação de orçamento não encontrada"
                        )
                );

        return solicitacaoOrcamentoMapper.toResponse(solicitacao);
    }
}
