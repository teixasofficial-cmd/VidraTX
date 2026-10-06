package br.com.vidratx.service;

import br.com.vidratx.dto.MaterialNecessarioRequest;
import br.com.vidratx.dto.MaterialNecessarioResponse;
import br.com.vidratx.entity.Material;
import br.com.vidratx.entity.MaterialNecessario;
import br.com.vidratx.entity.Orcamento;
import br.com.vidratx.enums.StatusOrcamento;
import br.com.vidratx.exception.MaterialNaoEncontradoException;
import br.com.vidratx.exception.MaterialNecessarioNaoEncontradoException;
import br.com.vidratx.exception.OrcamentoNaoAprovadoException;
import br.com.vidratx.exception.OrcamentoNaoEncontradoException;
import br.com.vidratx.mapper.MaterialNecessarioMapper;
import br.com.vidratx.repository.MaterialNecessarioRepository;
import br.com.vidratx.repository.MaterialRepository;
import br.com.vidratx.repository.OrcamentoRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MaterialNecessarioService {

    private final MaterialNecessarioRepository materialNecessarioRepository;
    private final OrcamentoRepository orcamentoRepository;
    private final MaterialRepository materialRepository;
    private final MaterialNecessarioMapper materialNecessarioMapper;

    public MaterialNecessarioService(
            MaterialNecessarioRepository materialNecessarioRepository,
            OrcamentoRepository orcamentoRepository,
            MaterialRepository materialRepository,
            MaterialNecessarioMapper materialNecessarioMapper) {

        this.materialNecessarioRepository = materialNecessarioRepository;
        this.orcamentoRepository = orcamentoRepository;
        this.materialRepository = materialRepository;
        this.materialNecessarioMapper = materialNecessarioMapper;
    }

    @Transactional(readOnly = true)
    public List<MaterialNecessarioResponse> listar(
            Long empresaId,
            Long orcamentoId) {

        buscarOrcamento(empresaId, orcamentoId);

        return materialNecessarioRepository
                .findAllByOrcamentoIdOrderByCriadoEmAsc(orcamentoId)
                .stream()
                .map(materialNecessarioMapper::toResponse)
                .toList();
    }

    @Transactional
    public MaterialNecessarioResponse adicionar(
            Long empresaId,
            Long orcamentoId,
            MaterialNecessarioRequest request) {

        Orcamento orcamento =
                buscarOrcamento(empresaId, orcamentoId);

        garantirAprovado(orcamento);

        Material material =
                buscarMaterial(empresaId, request.getMaterialId());

        MaterialNecessario item =
                materialNecessarioMapper.toEntity(request, material);

        item.setOrcamento(orcamento);

        MaterialNecessario salvo =
                materialNecessarioRepository.save(item);

        return materialNecessarioMapper.toResponse(salvo);
    }

    @Transactional
    public MaterialNecessarioResponse atualizar(
            Long empresaId,
            Long orcamentoId,
            Long itemId,
            MaterialNecessarioRequest request) {

        Orcamento orcamento =
                buscarOrcamento(empresaId, orcamentoId);

        garantirAprovado(orcamento);

        MaterialNecessario item =
                buscarItem(orcamentoId, itemId);

        Material material =
                buscarMaterial(empresaId, request.getMaterialId());

        materialNecessarioMapper.updateEntity(item, request, material);

        MaterialNecessario salvo =
                materialNecessarioRepository.save(item);

        return materialNecessarioMapper.toResponse(salvo);
    }

    @Transactional
    public void remover(
            Long empresaId,
            Long orcamentoId,
            Long itemId) {

        Orcamento orcamento =
                buscarOrcamento(empresaId, orcamentoId);

        garantirAprovado(orcamento);

        MaterialNecessario item =
                buscarItem(orcamentoId, itemId);

        materialNecessarioRepository.delete(item);
    }

    private void garantirAprovado(Orcamento orcamento) {

        if (orcamento.getStatus() != StatusOrcamento.APROVADO) {

            throw new OrcamentoNaoAprovadoException(
                    "A lista de materiais só pode ser gerenciada em orçamentos aprovados"
            );
        }
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

    private Material buscarMaterial(
            Long empresaId,
            Long materialId) {

        return materialRepository
                .findByIdAndEmpresaId(materialId, empresaId)
                .orElseThrow(() ->
                        new MaterialNaoEncontradoException(
                                "Material não encontrado"
                        )
                );
    }

    private MaterialNecessario buscarItem(
            Long orcamentoId,
            Long itemId) {

        return materialNecessarioRepository
                .findByIdAndOrcamentoId(itemId, orcamentoId)
                .orElseThrow(() ->
                        new MaterialNecessarioNaoEncontradoException(
                                "Item de material não encontrado"
                        )
                );
    }
}
