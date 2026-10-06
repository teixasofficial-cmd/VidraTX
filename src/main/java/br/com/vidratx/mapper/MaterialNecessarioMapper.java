package br.com.vidratx.mapper;

import br.com.vidratx.dto.MaterialNecessarioRequest;
import br.com.vidratx.dto.MaterialNecessarioResponse;
import br.com.vidratx.entity.Material;
import br.com.vidratx.entity.MaterialNecessario;
import org.springframework.stereotype.Component;

@Component
public class MaterialNecessarioMapper {

    public MaterialNecessario toEntity(
            MaterialNecessarioRequest request,
            Material material) {

        MaterialNecessario item = new MaterialNecessario();

        item.setMaterial(material);

        aplicarCampos(item, request);

        return item;
    }

    public void updateEntity(
            MaterialNecessario item,
            MaterialNecessarioRequest request,
            Material material) {

        item.setMaterial(material);

        aplicarCampos(item, request);
    }

    public MaterialNecessarioResponse toResponse(
            MaterialNecessario item) {

        MaterialNecessarioResponse response =
                new MaterialNecessarioResponse();

        response.setId(item.getId());

        if (item.getOrcamento() != null) {
            response.setOrcamentoId(item.getOrcamento().getId());
        }

        if (item.getMaterial() != null) {
            response.setMaterialId(item.getMaterial().getId());
            response.setMaterialNome(item.getMaterial().getNome());
        }

        response.setQuantidade(item.getQuantidade());
        response.setUnidade(item.getUnidade());
        response.setObservacao(item.getObservacao());
        response.setCriadoEm(item.getCriadoEm());
        response.setAtualizadoEm(item.getAtualizadoEm());

        return response;
    }

    private void aplicarCampos(
            MaterialNecessario item,
            MaterialNecessarioRequest request) {

        item.setQuantidade(request.getQuantidade());

        item.setUnidade(
                normalizar(request.getUnidade())
        );

        item.setObservacao(
                normalizar(request.getObservacao())
        );
    }

    private String normalizar(String valor) {

        if (valor == null || valor.isBlank()) {
            return null;
        }

        return valor.trim();
    }
}
