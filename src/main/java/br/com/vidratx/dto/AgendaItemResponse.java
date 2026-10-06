package br.com.vidratx.dto;

import java.time.LocalDateTime;

public record AgendaItemResponse(
        String tipo,
        Long id,
        Long orcamentoId,
        Long ordemServicoId,
        String clienteNome,
        String endereco,
        String equipe,
        String status,
        LocalDateTime inicio,
        LocalDateTime fim,
        boolean cancelamentoSolicitado) {
}
