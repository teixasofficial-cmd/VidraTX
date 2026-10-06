package br.com.vidratx.mapper;

import br.com.vidratx.dto.MedicaoRequest;
import br.com.vidratx.dto.MedicaoResponse;
import br.com.vidratx.entity.Medicao;
import br.com.vidratx.entity.Orcamento;
import org.springframework.stereotype.Component;

@Component
public class MedicaoMapper {

    public Medicao toEntity(
            MedicaoRequest request,
            Orcamento orcamento) {

        Medicao medicao = new Medicao();

        medicao.setOrcamento(orcamento);
        medicao.setDataAgendada(request.getDataAgendada());

        medicao.setEndereco(
                normalizar(request.getEndereco())
        );

        medicao.setObservacoes(
                normalizar(request.getObservacoes())
        );

        return medicao;
    }

    public MedicaoResponse toResponse(
            Medicao medicao) {

        MedicaoResponse response =
                new MedicaoResponse();

        response.setId(medicao.getId());

        if (medicao.getOrcamento() != null) {

            response.setOrcamentoId(
                    medicao.getOrcamento().getId()
            );

            if (medicao.getOrcamento().getCliente() != null) {

                response.setClienteNome(
                        medicao.getOrcamento().getCliente().getNome()
                );
            }
        }

        response.setStatus(medicao.getStatus());
        response.setDataAgendada(medicao.getDataAgendada());
        response.setDataRealizada(medicao.getDataRealizada());
        response.setContrapropostaTexto(medicao.getContrapropostaTexto());
        response.setEndereco(medicao.getEndereco());
        response.setObservacoes(medicao.getObservacoes());
        response.setCriadoEm(medicao.getCriadoEm());
        response.setAtualizadoEm(medicao.getAtualizadoEm());

        return response;
    }

    private String normalizar(String valor) {

        if (valor == null || valor.isBlank()) {
            return null;
        }

        return valor.trim();
    }
}
