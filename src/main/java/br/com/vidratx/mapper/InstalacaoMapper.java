package br.com.vidratx.mapper;

import br.com.vidratx.dto.InstalacaoRequest;
import br.com.vidratx.dto.InstalacaoResponse;
import br.com.vidratx.entity.Instalacao;
import br.com.vidratx.entity.OrdemServico;
import org.springframework.stereotype.Component;

@Component
public class InstalacaoMapper {

    public Instalacao toEntity(
            InstalacaoRequest request,
            OrdemServico ordemServico) {

        Instalacao instalacao = new Instalacao();

        instalacao.setOrdemServico(ordemServico);
        instalacao.setDataAgendada(request.getDataAgendada());

        instalacao.setEndereco(
                normalizar(request.getEndereco())
        );

        instalacao.setObservacoes(
                normalizar(request.getObservacoes())
        );

        instalacao.setEquipeResponsavel(
                normalizar(request.getEquipeResponsavel())
        );

        return instalacao;
    }

    public InstalacaoResponse toResponse(
            Instalacao instalacao) {

        InstalacaoResponse response =
                new InstalacaoResponse();

        response.setId(instalacao.getId());

        if (instalacao.getOrdemServico() != null) {

            response.setOrdemServicoId(
                    instalacao.getOrdemServico().getId()
            );

            if (instalacao.getOrdemServico().getOrcamento() != null
                    && instalacao.getOrdemServico()
                    .getOrcamento().getCliente() != null) {

                response.setClienteNome(
                        instalacao.getOrdemServico()
                                .getOrcamento()
                                .getCliente()
                                .getNome()
                );
            }
        }

        response.setStatus(instalacao.getStatus());
        response.setDataAgendada(instalacao.getDataAgendada());
        response.setDataRealizada(instalacao.getDataRealizada());
        response.setContrapropostaTexto(instalacao.getContrapropostaTexto());
        response.setEndereco(instalacao.getEndereco());
        response.setEquipeResponsavel(instalacao.getEquipeResponsavel());
        response.setChecklist(instalacao.getChecklist());
        response.setObservacoes(instalacao.getObservacoes());
        response.setCriadoEm(instalacao.getCriadoEm());
        response.setAtualizadoEm(instalacao.getAtualizadoEm());

        return response;
    }

    private String normalizar(String valor) {

        if (valor == null || valor.isBlank()) {
            return null;
        }

        return valor.trim();
    }
}
