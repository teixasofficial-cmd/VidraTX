package br.com.vidratx.mapper;

import br.com.vidratx.dto.MaterialRequest;
import br.com.vidratx.dto.MaterialResponse;
import br.com.vidratx.entity.Material;
import org.springframework.stereotype.Component;

@Component
public class MaterialMapper {

    public Material toEntity(MaterialRequest request) {

        Material material = new Material();

        aplicarCampos(material, request);

        return material;
    }

    public void updateEntity(
            Material material,
            MaterialRequest request) {

        aplicarCampos(material, request);
    }

    public MaterialResponse toResponse(
            Material material) {

        MaterialResponse response =
                new MaterialResponse();

        response.setId(material.getId());

        if (material.getEmpresa() != null) {
            response.setEmpresaId(material.getEmpresa().getId());
        }

        response.setNome(material.getNome());
        response.setDescricao(material.getDescricao());
        response.setAtivo(material.getAtivo());
        response.setCriadoEm(material.getCriadoEm());
        response.setAtualizadoEm(material.getAtualizadoEm());

        return response;
    }

    private void aplicarCampos(
            Material material,
            MaterialRequest request) {

        material.setNome(
                request.getNome().trim()
        );

        material.setDescricao(
                normalizar(request.getDescricao())
        );

        material.setAtivo(
                request.getAtivo() == null
                        ? Boolean.TRUE
                        : request.getAtivo()
        );
    }

    private String normalizar(String valor) {

        if (valor == null || valor.isBlank()) {
            return null;
        }

        return valor.trim();
    }
}
