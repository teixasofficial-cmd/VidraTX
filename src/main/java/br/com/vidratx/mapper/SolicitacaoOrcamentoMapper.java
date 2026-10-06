package br.com.vidratx.mapper;

import br.com.vidratx.dto.SolicitacaoOrcamentoResponse;
import br.com.vidratx.entity.SolicitacaoOrcamento;
import org.springframework.stereotype.Component;

@Component
public class SolicitacaoOrcamentoMapper {

    public SolicitacaoOrcamentoResponse toResponse(SolicitacaoOrcamento solicitacao) {

        SolicitacaoOrcamentoResponse response = new SolicitacaoOrcamentoResponse();

        response.setId(solicitacao.getId());

        if (solicitacao.getCliente() != null) {
            response.setClienteId(solicitacao.getCliente().getId());
            response.setClienteNome(solicitacao.getCliente().getNome());
        }

        response.setCanal(solicitacao.getCanal());
        response.setStatus(solicitacao.getStatus());
        response.setDescricao(solicitacao.getDescricao());
        response.setCriadoEm(solicitacao.getCriadoEm());
        response.setAtualizadoEm(solicitacao.getAtualizadoEm());

        return response;
    }
}
