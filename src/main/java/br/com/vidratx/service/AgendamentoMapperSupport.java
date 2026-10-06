package br.com.vidratx.service;

import br.com.vidratx.dto.PropostaAgendamentoResponse;
import br.com.vidratx.entity.Cliente;
import br.com.vidratx.entity.MensagemSaida;
import br.com.vidratx.entity.PropostaAgendamento;

final class AgendamentoMapperSupport {

    private AgendamentoMapperSupport() {
    }

    static PropostaAgendamentoResponse toPropostaResponse(PropostaAgendamento proposta) {

        return new PropostaAgendamentoResponse(
                proposta.getId(),
                proposta.getVersao(),
                proposta.getOrigem(),
                proposta.getDataProposta(),
                proposta.getTextoCliente(),
                proposta.getEquipe(),
                proposta.getStatus(),
                proposta.getMotivo(),
                proposta.getCriadoPor() != null ? proposta.getCriadoPor().getNome() : null,
                proposta.getRespondidoPeloCliente(),
                proposta.getRespondidoPor() != null ? proposta.getRespondidoPor().getNome() : null,
                proposta.getCriadoEm(),
                proposta.getRespondidoEm(),
                proposta.getMensagemSaida() != null ? proposta.getMensagemSaida().getStatus().name() : null
        );
    }

    static String statusEnvio(MensagemSaida envio, Cliente cliente) {

        if (envio != null) {
            return envio.getStatus().name();
        }

        return NegociacaoAgendamentoService.telefoneDoCliente(cliente) == null ? "SEM_WHATSAPP" : null;
    }
}
