package br.com.vidratx.dto;

import java.util.Map;

public class ApiError {

    private int status;
    private String mensagem;
    private Map<String, String> erros;

    public ApiError() {
    }

    public ApiError(
            int status,
            String mensagem) {

        this.status = status;
        this.mensagem = mensagem;
    }

    public ApiError(
            int status,
            String mensagem,
            Map<String, String> erros) {

        this.status = status;
        this.mensagem = mensagem;
        this.erros = erros;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getMensagem() {
        return mensagem;
    }

    public void setMensagem(String mensagem) {
        this.mensagem = mensagem;
    }

    public Map<String, String> getErros() {
        return erros;
    }

    public void setErros(Map<String, String> erros) {
        this.erros = erros;
    }
}
