package br.com.vidratx.dto;

import java.util.List;

public class AtendimentoWhatsappDetalheResponse {

    private AtendimentoWhatsappResponse atendimento;
    private List<MensagemAtendimentoResponse> mensagens;
    private List<HistoricoResponse> historico;

    public AtendimentoWhatsappResponse getAtendimento() {
        return atendimento;
    }

    public void setAtendimento(AtendimentoWhatsappResponse atendimento) {
        this.atendimento = atendimento;
    }

    public List<MensagemAtendimentoResponse> getMensagens() {
        return mensagens;
    }

    public void setMensagens(List<MensagemAtendimentoResponse> mensagens) {
        this.mensagens = mensagens;
    }

    public List<HistoricoResponse> getHistorico() {
        return historico;
    }

    public void setHistorico(List<HistoricoResponse> historico) {
        this.historico = historico;
    }
}
