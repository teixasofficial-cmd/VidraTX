package br.com.vidratx.mapper;

import br.com.vidratx.dto.OrdemServicoRequest;
import br.com.vidratx.dto.OrdemServicoResponse;
import br.com.vidratx.entity.Empresa;
import br.com.vidratx.entity.Orcamento;
import br.com.vidratx.entity.OrdemServico;
import org.springframework.stereotype.Component;

@Component
public class OrdemServicoMapper {

    public OrdemServico toEntity(
            OrdemServicoRequest request,
            Empresa empresa,
            Orcamento orcamento) {

        OrdemServico ordemServico = new OrdemServico();

        ordemServico.setEmpresa(empresa);
        ordemServico.setOrcamento(orcamento);

        ordemServico.setNecessitaProducao(
                request.getNecessitaProducao() == null
                        ? Boolean.TRUE
                        : request.getNecessitaProducao()
        );

        ordemServico.setObservacoes(
                normalizar(request.getObservacoes())
        );

        return ordemServico;
    }

    public OrdemServicoResponse toResponse(
            OrdemServico ordemServico) {

        OrdemServicoResponse response =
                new OrdemServicoResponse();

        response.setId(ordemServico.getId());

        if (ordemServico.getEmpresa() != null) {
            response.setEmpresaId(ordemServico.getEmpresa().getId());
        }

        if (ordemServico.getOrcamento() != null) {

            response.setOrcamentoId(
                    ordemServico.getOrcamento().getId()
            );

            if (ordemServico.getOrcamento().getCliente() != null) {

                response.setClienteNome(
                        ordemServico.getOrcamento()
                                .getCliente()
                                .getNome()
                );
            }
        }

        response.setNecessitaProducao(
                ordemServico.getNecessitaProducao()
        );

        response.setStatusProducao(
                ordemServico.getStatusProducao()
        );

        response.setObservacoes(
                ordemServico.getObservacoes()
        );

        response.setCriadoEm(ordemServico.getCriadoEm());
        response.setAtualizadoEm(ordemServico.getAtualizadoEm());
        response.setProducaoIniciadaEm(ordemServico.getProducaoIniciadaEm());
        response.setProducaoConcluidaEm(ordemServico.getProducaoConcluidaEm());
        response.setProducaoConferidaEm(ordemServico.getProducaoConferidaEm());

        return response;
    }

    private String normalizar(String valor) {

        if (valor == null || valor.isBlank()) {
            return null;
        }

        return valor.trim();
    }
}
