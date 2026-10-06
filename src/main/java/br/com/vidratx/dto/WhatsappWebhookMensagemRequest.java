package br.com.vidratx.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class WhatsappWebhookMensagemRequest {

    @NotBlank(message = "Telefone é obrigatório")
    @Size(
            max = 20,
            message = "Telefone deve ter no máximo 20 caracteres"
    )
    private String telefone;

    @Size(
            max = 4000,
            message = "Mensagem deve ter no máximo 4000 caracteres"
    )
    private String mensagem;

    @Size(max = 128, message = "Id da mensagem deve ter no máximo 128 caracteres")
    private String mensagemId;

    private Long timestamp;

    @Size(max = 30, message = "Tipo de mídia inválido")
    private String midiaNaoSuportada;

    @Size(max = 128, message = "Id da mensagem reagida deve ter no máximo 128 caracteres")
    private String reacaoA;

    @Size(max = 128, message = "Id da mensagem editada deve ter no máximo 128 caracteres")
    private String editaMensagemId;

    @Size(max = 128, message = "Id da mensagem apagada deve ter no máximo 128 caracteres")
    private String apagaMensagemId;

    public String getTelefone() {
        return telefone;
    }

    public void setTelefone(String telefone) {
        this.telefone = telefone;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public String getMensagemId() {
        return mensagemId;
    }

    public void setMensagemId(String mensagemId) {
        this.mensagemId = mensagemId;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }

    public String getMidiaNaoSuportada() {
        return midiaNaoSuportada;
    }

    public void setMidiaNaoSuportada(String midiaNaoSuportada) {
        this.midiaNaoSuportada = midiaNaoSuportada;
    }

    public String getReacaoA() {
        return reacaoA;
    }

    public void setReacaoA(String reacaoA) {
        this.reacaoA = reacaoA;
    }

    public String getEditaMensagemId() {
        return editaMensagemId;
    }

    public void setEditaMensagemId(String editaMensagemId) {
        this.editaMensagemId = editaMensagemId;
    }

    public String getApagaMensagemId() {
        return apagaMensagemId;
    }

    public void setApagaMensagemId(String apagaMensagemId) {
        this.apagaMensagemId = apagaMensagemId;
    }
}
