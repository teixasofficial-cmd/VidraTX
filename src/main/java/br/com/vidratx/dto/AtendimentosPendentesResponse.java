package br.com.vidratx.dto;

public class AtendimentosPendentesResponse {

    private long quantidade;

    public AtendimentosPendentesResponse() {
    }

    public AtendimentosPendentesResponse(long quantidade) {
        this.quantidade = quantidade;
    }

    public long getQuantidade() {
        return quantidade;
    }

    public void setQuantidade(long quantidade) {
        this.quantidade = quantidade;
    }
}
