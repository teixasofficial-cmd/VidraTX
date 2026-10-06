package br.com.vidratx.mapper;

import br.com.vidratx.dto.PagamentoRequest;
import br.com.vidratx.dto.PagamentoResponse;
import br.com.vidratx.entity.Pagamento;
import org.springframework.stereotype.Component;

@Component
public class PagamentoMapper {

    public Pagamento toEntity(PagamentoRequest request) {

        Pagamento pagamento = new Pagamento();

        pagamento.setValor(request.getValor());
        pagamento.setFormaPagamento(request.getFormaPagamento());
        pagamento.setVencimento(request.getVencimento());

        pagamento.setObservacao(
                normalizar(request.getObservacao())
        );

        return pagamento;
    }

    public PagamentoResponse toResponse(Pagamento pagamento) {

        PagamentoResponse response = new PagamentoResponse();

        response.setId(pagamento.getId());

        if (pagamento.getOrcamento() != null) {
            response.setOrcamentoId(pagamento.getOrcamento().getId());
        }

        response.setValor(pagamento.getValor());
        response.setFormaPagamento(pagamento.getFormaPagamento());
        response.setVencimento(pagamento.getVencimento());
        response.setSituacao(pagamento.getSituacao());
        response.setPagoEm(pagamento.getPagoEm());
        response.setObservacao(pagamento.getObservacao());
        response.setCriadoEm(pagamento.getCriadoEm());
        response.setAtualizadoEm(pagamento.getAtualizadoEm());

        return response;
    }

    private String normalizar(String valor) {

        if (valor == null || valor.isBlank()) {
            return null;
        }

        return valor.trim();
    }
}
