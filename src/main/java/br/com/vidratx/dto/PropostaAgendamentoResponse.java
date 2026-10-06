package br.com.vidratx.dto;

import br.com.vidratx.enums.OrigemProposta;
import br.com.vidratx.enums.StatusProposta;

import java.time.LocalDateTime;

public record PropostaAgendamentoResponse(
        Long id,
        Integer versao,
        OrigemProposta origem,
        LocalDateTime dataProposta,
        String textoCliente,
        String equipe,
        StatusProposta status,
        String motivo,
        String criadoPorNome,
        Boolean respondidoPeloCliente,
        String respondidoPorNome,
        LocalDateTime criadoEm,
        LocalDateTime respondidoEm,
        String envioStatus) {
}
