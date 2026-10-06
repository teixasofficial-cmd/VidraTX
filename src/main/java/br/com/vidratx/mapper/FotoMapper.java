package br.com.vidratx.mapper;

import br.com.vidratx.dto.FotoRequest;
import br.com.vidratx.dto.FotoResponse;
import br.com.vidratx.entity.Foto;
import org.springframework.stereotype.Component;

@Component
public class FotoMapper {

    public Foto toEntity(FotoRequest request) {

        Foto foto = new Foto();

        foto.setTipo(request.getTipo());
        foto.setUrl(request.getUrl().trim());

        foto.setDescricao(
                normalizar(request.getDescricao())
        );

        return foto;
    }

    public FotoResponse toResponse(Foto foto) {

        FotoResponse response = new FotoResponse();

        response.setId(foto.getId());

        if (foto.getOrcamento() != null) {
            response.setOrcamentoId(foto.getOrcamento().getId());
        }

        response.setTipo(foto.getTipo());
        response.setUrl(foto.getUrl());
        response.setDescricao(foto.getDescricao());
        response.setCriadoEm(foto.getCriadoEm());

        return response;
    }

    private String normalizar(String valor) {

        if (valor == null || valor.isBlank()) {
            return null;
        }

        return valor.trim();
    }
}
