package br.com.vidratx.mapper;

import br.com.vidratx.dto.HistoricoResponse;
import br.com.vidratx.entity.Historico;
import org.springframework.stereotype.Component;

@Component
public class HistoricoMapper {

    public HistoricoResponse toResponse(Historico historico) {

        HistoricoResponse response = new HistoricoResponse();

        response.setId(historico.getId());
        response.setTipo(historico.getTipo());
        response.setDescricao(historico.getDescricao());
        response.setCriadoEm(historico.getCriadoEm());

        if (historico.getUsuario() != null) {
            response.setUsuarioId(historico.getUsuario().getId());
            response.setUsuarioNome(historico.getUsuario().getNome());
        }

        return response;
    }
}
