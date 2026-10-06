package br.com.vidratx.mapper;

import br.com.vidratx.dto.ServicoRequest;
import br.com.vidratx.dto.ServicoResponse;
import br.com.vidratx.entity.Servico;
import org.springframework.stereotype.Component;

@Component
public class ServicoMapper {

    public Servico toEntity(ServicoRequest request) {

        Servico servico = new Servico();

        aplicarCampos(servico, request);

        return servico;
    }

    public void updateEntity(
            Servico servico,
            ServicoRequest request) {

        aplicarCampos(servico, request);
    }

    public ServicoResponse toResponse(
            Servico servico) {

        ServicoResponse response =
                new ServicoResponse();

        response.setId(
                servico.getId()
        );

        if (servico.getEmpresa() != null) {

            response.setEmpresaId(
                    servico.getEmpresa().getId()
            );
        }

        response.setNome(
                servico.getNome()
        );

        response.setDescricao(
                servico.getDescricao()
        );

        response.setAtivo(
                servico.getAtivo()
        );

        response.setCriadoEm(
                servico.getCriadoEm()
        );

        response.setAtualizadoEm(
                servico.getAtualizadoEm()
        );

        return response;
    }

    private void aplicarCampos(
            Servico servico,
            ServicoRequest request) {

        servico.setNome(
                request.getNome().trim()
        );

        servico.setDescricao(
                normalizar(request.getDescricao())
        );

        servico.setAtivo(
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
