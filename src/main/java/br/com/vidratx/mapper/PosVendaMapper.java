package br.com.vidratx.mapper;

import br.com.vidratx.dto.PosVendaRequest;
import br.com.vidratx.dto.PosVendaResponse;
import br.com.vidratx.entity.PosVenda;
import org.springframework.stereotype.Component;

@Component
public class PosVendaMapper {

    public PosVenda toEntity(PosVendaRequest request) {

        PosVenda posVenda = new PosVenda();

        posVenda.setProblema(request.getProblema().trim());

        return posVenda;
    }

    public PosVendaResponse toResponse(PosVenda posVenda) {

        PosVendaResponse response = new PosVendaResponse();

        response.setId(posVenda.getId());

        if (posVenda.getOrdemServico() != null) {

            response.setOrdemServicoId(
                    posVenda.getOrdemServico().getId()
            );

            if (posVenda.getOrdemServico().getOrcamento() != null
                    && posVenda.getOrdemServico()
                    .getOrcamento().getCliente() != null) {

                response.setClienteNome(
                        posVenda.getOrdemServico()
                                .getOrcamento()
                                .getCliente()
                                .getNome()
                );
            }
        }

        response.setStatus(posVenda.getStatus());
        response.setProblema(posVenda.getProblema());
        response.setAtendimento(posVenda.getAtendimento());
        response.setSolucao(posVenda.getSolucao());
        response.setCriadoEm(posVenda.getCriadoEm());
        response.setAtualizadoEm(posVenda.getAtualizadoEm());
        response.setResolvidoEm(posVenda.getResolvidoEm());
        response.setEncerradoEm(posVenda.getEncerradoEm());

        return response;
    }
}
