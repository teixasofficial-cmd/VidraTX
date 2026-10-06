package br.com.vidratx.service;

import br.com.vidratx.dto.MaterialRequest;
import br.com.vidratx.dto.MaterialResponse;
import br.com.vidratx.entity.Empresa;
import br.com.vidratx.entity.Material;
import br.com.vidratx.exception.EmpresaNaoEncontradaException;
import br.com.vidratx.exception.MaterialDuplicadoException;
import br.com.vidratx.exception.MaterialNaoEncontradoException;
import br.com.vidratx.mapper.MaterialMapper;
import br.com.vidratx.repository.EmpresaRepository;
import br.com.vidratx.repository.MaterialRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class MaterialService {

    private final MaterialRepository materialRepository;
    private final EmpresaRepository empresaRepository;
    private final MaterialMapper materialMapper;

    public MaterialService(
            MaterialRepository materialRepository,
            EmpresaRepository empresaRepository,
            MaterialMapper materialMapper) {

        this.materialRepository = materialRepository;
        this.empresaRepository = empresaRepository;
        this.materialMapper = materialMapper;
    }

    @Transactional(readOnly = true)
    public List<MaterialResponse> listarPorEmpresa(
            Long empresaId,
            boolean apenasAtivos) {

        verificarEmpresa(empresaId);

        List<Material> materiais =
                apenasAtivos
                        ? materialRepository
                        .findAllByEmpresaIdAndAtivoTrueOrderByNomeAsc(
                                empresaId
                        )
                        : materialRepository
                        .findAllByEmpresaIdOrderByNomeAsc(
                                empresaId
                        );

        return materiais
                .stream()
                .map(materialMapper::toResponse)
                .toList();
    }

    @Transactional(readOnly = true)
    public MaterialResponse buscarPorId(
            Long empresaId,
            Long materialId) {

        Material material =
                buscarMaterial(empresaId, materialId);

        return materialMapper.toResponse(material);
    }

    @Transactional
    public MaterialResponse salvar(
            Long empresaId,
            MaterialRequest request) {

        Empresa empresa =
                buscarEmpresa(empresaId);

        String nome =
                request.getNome().trim();

        if (materialRepository
                .existsByEmpresaIdAndNome(empresaId, nome)) {

            throw new MaterialDuplicadoException(
                    "Já existe um material com este nome nesta empresa"
            );
        }

        Material material =
                materialMapper.toEntity(request);

        material.setEmpresa(empresa);

        Material salvo =
                materialRepository.save(material);

        return materialMapper.toResponse(salvo);
    }

    @Transactional
    public MaterialResponse atualizar(
            Long empresaId,
            Long materialId,
            MaterialRequest request) {

        Material material =
                buscarMaterial(empresaId, materialId);

        String nome =
                request.getNome().trim();

        if (materialRepository
                .existsByEmpresaIdAndNomeAndIdNot(
                        empresaId, nome, materialId
                )) {

            throw new MaterialDuplicadoException(
                    "Já existe um material com este nome nesta empresa"
            );
        }

        materialMapper.updateEntity(material, request);

        return materialMapper.toResponse(
                materialRepository.save(material)
        );
    }

    @Transactional
    public void excluir(
            Long empresaId,
            Long materialId) {

        Material material =
                buscarMaterial(empresaId, materialId);

        materialRepository.delete(material);
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
}
