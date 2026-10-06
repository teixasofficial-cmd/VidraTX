package br.com.vidratx.mapper;

import br.com.vidratx.dto.OrcamentoRequest;
import br.com.vidratx.dto.OrcamentoResponse;
import br.com.vidratx.entity.Cliente;
import br.com.vidratx.entity.Empresa;
import br.com.vidratx.entity.Orcamento;
import br.com.vidratx.util.DinheiroUtils;
import org.springframework.stereotype.Component;

@Component
public class OrcamentoMapper {

    public Orcamento toEntity(
            OrcamentoRequest request,
            Empresa empresa,
            Cliente cliente) {

        Orcamento orcamento = new Orcamento();

        orcamento.setEmpresa(empresa);
        orcamento.setCliente(cliente);

        orcamento.setObservacoes(
                normalizar(request.getObservacoes())
        );

        orcamento.setValidoAte(
                request.getValidoAte()
        );

        return orcamento;
    }

    public void updateEntity(
            Orcamento orcamento,
            OrcamentoRequest request,
            Cliente cliente) {

        orcamento.setCliente(cliente);

        orcamento.setObservacoes(
                normalizar(request.getObservacoes())
        );

        orcamento.setValidoAte(
                request.getValidoAte()
        );
    }

    public OrcamentoResponse toResponse(Orcamento orcamento) {

        OrcamentoResponse response =
                new OrcamentoResponse();

        response.setId(orcamento.getId());

        if (orcamento.getEmpresa() != null) {
            response.setEmpresaId(orcamento.getEmpresa().getId());
        }

        if (orcamento.getCliente() != null) {

            Cliente cliente = orcamento.getCliente();

            response.setClienteId(cliente.getId());
            response.setClienteNome(cliente.getNome());
            response.setClienteTelefone(cliente.getTelefone());
            response.setClienteWhatsapp(cliente.getWhatsapp());
            response.setClienteEmail(cliente.getEmail());
        }

        if (orcamento.getSolicitacaoOrcamento() != null) {

            response.setSolicitacaoOrcamentoId(
                    orcamento.getSolicitacaoOrcamento().getId()
            );

            response.setEspecificacoes(
                    orcamento.getSolicitacaoOrcamento().getDescricao()
            );
        }

        response.setStatus(orcamento.getStatus());
        response.setObservacoes(orcamento.getObservacoes());
        response.setValidoAte(orcamento.getValidoAte());
        response.setValorTotal(orcamento.getValorTotal());
        response.setCriadoEm(orcamento.getCriadoEm());
        response.setAtualizadoEm(orcamento.getAtualizadoEm());
        response.setEnviadoEm(orcamento.getEnviadoEm());
        response.setRespondidoEm(orcamento.getRespondidoEm());
        response.setMotivoPerda(orcamento.getMotivoPerda());
        response.setMotivoPerdaOutro(orcamento.getMotivoPerdaOutro());
        response.setCustoTotal(
                orcamento.getCustoTotalCentavos() != null
                        ? DinheiroUtils.paraReais(orcamento.getCustoTotalCentavos()) : null
        );
        response.setPrecoSugerido(
                orcamento.getPrecoSugeridoCentavos() != null
                        ? DinheiroUtils.paraReais(orcamento.getPrecoSugeridoCentavos()) : null
        );
        response.setAjusteComercial(DinheiroUtils.paraReais(orcamento.getAjusteComercialCentavos()));
        response.setMargemReal(orcamento.getMargemRealPercentual());
        response.setRevisaoEnvio(orcamento.getRevisaoEnvio());
        response.setParcelasCartao(orcamento.getParcelasCartao());
        response.setPrecoFinalManual(
                orcamento.getPrecoFinalManualCentavos() != null
                        ? DinheiroUtils.paraReais(orcamento.getPrecoFinalManualCentavos()) : null
        );
        response.setAlteracaoSolicitadaEm(orcamento.getAlteracaoSolicitadaEm());
        response.setAlteracaoSolicitadaTexto(orcamento.getAlteracaoSolicitadaTexto());
        response.setEstimativaEnviadaEm(orcamento.getEstimativaEnviadaEm());

        return response;
    }

    private String normalizar(String valor) {

        if (valor == null || valor.isBlank()) {
            return null;
        }

        return valor.trim();
    }
}
