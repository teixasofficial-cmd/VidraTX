package br.com.vidratx.dto;

import br.com.vidratx.entity.PerguntaFrequente;

public record PerguntaFrequenteResponse(Long id, String pergunta, String palavrasChave, String resposta, Boolean ativa) {

    public static PerguntaFrequenteResponse de(PerguntaFrequente p) {
        return new PerguntaFrequenteResponse(p.getId(), p.getPergunta(), p.getPalavrasChave(), p.getResposta(), p.getAtiva());
    }
}
