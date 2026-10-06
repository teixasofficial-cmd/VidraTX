package br.com.vidratx.dto;

public class AlertaCalculoResponse {

    private String codigo;
    private String mensagem;

    public AlertaCalculoResponse() {
    }

    public AlertaCalculoResponse(String codigo, String mensagem) {
        this.codigo = codigo;
        this.mensagem = mensagem;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }
}
