package br.com.vidratx.service;

import br.com.vidratx.dto.FotoRequest;
import br.com.vidratx.dto.FotoResponse;
import br.com.vidratx.entity.Foto;
import br.com.vidratx.entity.Orcamento;
import br.com.vidratx.exception.FotoNaoEncontradaException;
import br.com.vidratx.exception.OrcamentoNaoEncontradoException;
import br.com.vidratx.mapper.FotoMapper;
import br.com.vidratx.repository.FotoRepository;
import br.com.vidratx.repository.OrcamentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class FotoService {

    private final FotoRepository fotoRepository;
    private final OrcamentoRepository orcamentoRepository;
    private final FotoMapper fotoMapper;

    public FotoService(
            FotoRepository fotoRepository,
            OrcamentoRepository orcamentoRepository,
            FotoMapper fotoMapper) {

        this.fotoRepository = fotoRepository;
        this.orcamentoRepository = orcamentoRepository;
        this.fotoMapper = fotoMapper;
    }

    @Transactional(readOnly = true)
    public List<FotoResponse> listar(
            Long empresaId,
            Long orcamentoId) {

        buscarOrcamento(empresaId, orcamentoId);

        return fotoRepository
                .findAllByOrcamentoIdOrderByCriadoEmAsc(orcamentoId)
                .stream()
                .map(fotoMapper::toResponse)
                .toList();
    }

    @Transactional
    public FotoResponse adicionar(
            Long empresaId,
            Long orcamentoId,
            FotoRequest request) {

        Orcamento orcamento =
                buscarOrcamento(empresaId, orcamentoId);

        Foto foto = fotoMapper.toEntity(request);

        foto.setOrcamento(orcamento);

        Foto salva = fotoRepository.save(foto);

        return fotoMapper.toResponse(salva);
    }

    @Transactional
    public void remover(
            Long empresaId,
            Long orcamentoId,
            Long fotoId) {

        buscarOrcamento(empresaId, orcamentoId);

        Foto foto =
                fotoRepository
                        .findByIdAndOrcamentoId(fotoId, orcamentoId)
                        .orElseThrow(() ->
                                new FotoNaoEncontradaException(
                                        "Foto não encontrada"
                                )
                        );

        fotoRepository.delete(foto);
    }

    private Orcamento buscarOrcamento(
            Long empresaId,
            Long orcamentoId) {

        return orcamentoRepository
                .findByIdAndEmpresaId(orcamentoId, empresaId)
                .orElseThrow(() ->
                        new OrcamentoNaoEncontradoException(
                                "Orçamento não encontrado"
                        )
                );
    }
}
