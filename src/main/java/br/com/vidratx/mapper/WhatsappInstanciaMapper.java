package br.com.vidratx.mapper;

import br.com.vidratx.dto.WhatsappInstanciaResponse;
import br.com.vidratx.entity.WhatsappInstancia;
import org.springframework.stereotype.Component;

@Component
public class WhatsappInstanciaMapper {

    public WhatsappInstanciaResponse toResponse(WhatsappInstancia instancia) {

        WhatsappInstanciaResponse response = new WhatsappInstanciaResponse();

        response.setId(instancia.getId());
        response.setNumero(instancia.getNumero());
        response.setStatus(instancia.getStatus());
        response.setConectadoEm(instancia.getConectadoEm());
        response.setCriadoEm(instancia.getCriadoEm());

        return response;
    }
}
