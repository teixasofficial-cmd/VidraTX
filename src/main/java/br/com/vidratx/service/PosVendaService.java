package br.com.vidratx.service;

import br.com.vidratx.dto.PosVendaAtendimentoRequest;
import br.com.vidratx.dto.PosVendaRequest;
import br.com.vidratx.dto.PosVendaResolverRequest;
import br.com.vidratx.dto.PosVendaResponse;
import br.com.vidratx.entity.OrdemServico;
import br.com.vidratx.entity.PosVenda;
import br.com.vidratx.enums.StatusPosVenda;
import br.com.vidratx.exception.OrdemServicoNaoEncontradaException;
import br.com.vidratx.exception.PosVendaNaoEncontradaException;
import br.com.vidratx.exception.TransicaoInvalidaException;
import br.com.vidratx.mapper.PosVendaMapper;
import br.com.vidratx.repository.OrdemServicoRepository;
import br.com.vidratx.repository.PosVendaRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PosVendaService {

    private final PosVendaRepository posVendaRepository;
    private final OrdemServicoRepository ordemServicoRepository;
    private final PosVendaMapper posVendaMapper;

    public PosVendaService(
            PosVendaRepository posVendaRepository,
            OrdemServicoRepository ordemServicoRepository,
            PosVendaMapper posVendaMapper) {

        this.posVendaRepository = posVendaRepository;
        this.ordemServicoRepository = ordemServicoRepository;
        this.posVendaMapper = posVendaMapper;
    }

    @Transactional(readOnly = true)
    public List<PosVendaResponse> listar(
            Long empresaId,
            Long ordemServicoId) {

        buscarOrdemServico(empresaId, ordemServicoId);

        return posVendaRepository
                .findAllByOrdemServicoIdOrderByCriadoEmDesc(ordemServicoId)
                .stream()
                .map(posVendaMapper::toResponse)
                .toList();
    }

    @Transactional
    public PosVendaResponse abrir(
            Long empresaId,
            Long ordemServicoId,
            PosVendaRequest request) {

        OrdemServico ordemServico =
                buscarOrdemServico(empresaId, ordemServicoId);

        PosVenda posVenda =
                posVendaMapper.toEntity(request);

        posVenda.setOrdemServico(ordemServico);

        PosVenda salva =
                posVendaRepository.save(posVenda);

        return posVendaMapper.toResponse(salva);
    }

    @Transactional
    public PosVendaResponse iniciarAtendimento(
            Long empresaId,
            Long ordemServicoId,
            Long posVendaId,
            PosVendaAtendimentoRequest request) {

        buscarOrdemServico(empresaId, ordemServicoId);

        PosVenda posVenda =
                buscarPosVenda(ordemServicoId, posVendaId);

        exigirStatus(posVenda, StatusPosVenda.ABERTA);

        posVenda.setStatus(StatusPosVenda.EM_ATENDIMENTO);
        posVenda.setAtendimento(request.getAtendimento().trim());

        return posVendaMapper.toResponse(
                posVendaRepository.save(posVenda)
        );
    }

    @Transactional
    public PosVendaResponse resolver(
            Long empresaId,
            Long ordemServicoId,
            Long posVendaId,
            PosVendaResolverRequest request) {

        buscarOrdemServico(empresaId, ordemServicoId);

        PosVenda posVenda =
                buscarPosVenda(ordemServicoId, posVendaId);

        if (posVenda.getStatus() != StatusPosVenda.ABERTA
                && posVenda.getStatus() != StatusPosVenda.EM_ATENDIMENTO) {

            throw new TransicaoInvalidaException(
                    "Só é possível resolver uma solicitação Aberta ou Em atendimento"
            );
        }

        posVenda.setStatus(StatusPosVenda.RESOLVIDA);
        posVenda.setSolucao(request.getSolucao().trim());
        posVenda.setResolvidoEm(LocalDateTime.now());

        return posVendaMapper.toResponse(
                posVendaRepository.save(posVenda)
        );
    }

    @Transactional
    public PosVendaResponse encerrar(
            Long empresaId,
            Long ordemServicoId,
            Long posVendaId) {

        buscarOrdemServico(empresaId, ordemServicoId);

        PosVenda posVenda =
                buscarPosVenda(ordemServicoId, posVendaId);

        exigirStatus(posVenda, StatusPosVenda.RESOLVIDA);

        posVenda.setStatus(StatusPosVenda.ENCERRADA);
        posVenda.setEncerradoEm(LocalDateTime.now());

        return posVendaMapper.toResponse(
                posVendaRepository.save(posVenda)
        );
    }

    private void exigirStatus(
            PosVenda posVenda,
            StatusPosVenda statusEsperado) {

        if (posVenda.getStatus() != statusEsperado) {

            throw new TransicaoInvalidaException(
                    "Esta ação exige que a solicitação esteja com status "
                            + statusEsperado
                            + ", mas está "
                            + posVenda.getStatus()
            );
        }
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

    private PosVenda buscarPosVenda(
            Long ordemServicoId,
            Long posVendaId) {

        return posVendaRepository
                .findByIdAndOrdemServicoId(posVendaId, ordemServicoId)
                .orElseThrow(() ->
                        new PosVendaNaoEncontradaException(
                                "Solicitação de pós-venda não encontrada"
                        )
                );
    }
}
