package br.com.vidratx.dto;

import java.time.LocalDateTime;

public record ProximaAcaoItemResponse(
        String tipo,
        Long referenciaId,
        Long orcamentoId,
        Long ordemServicoId,
        Long atendimentoId,
        String clienteNome,
        String status,
        String responsavel,
        String descricao,
        LocalDateTime data,
        boolean urgente) {
}
