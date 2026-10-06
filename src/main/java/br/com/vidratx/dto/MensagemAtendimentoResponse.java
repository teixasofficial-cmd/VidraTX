package br.com.vidratx.dto;

import br.com.vidratx.enums.RemetenteMensagem;
import br.com.vidratx.enums.TipoMensagem;

import java.time.LocalDateTime;

public class MensagemAtendimentoResponse {

    private Long id;
    private RemetenteMensagem remetente;
    private TipoMensagem tipo;
    private String conteudo;
    private String midiaUrl;
    private LocalDateTime enviadoEm;
    private String envioStatus;
    private String envioErro;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public RemetenteMensagem getRemetente() {
        return remetente;
    }

    public void setRemetente(RemetenteMensagem remetente) {
        this.remetente = remetente;
    }

    public TipoMensagem getTipo() {
        return tipo;
    }

    public void setTipo(TipoMensagem tipo) {
        this.tipo = tipo;
    }

    public String getConteudo() {
        return conteudo;
    }

    public void setConteudo(String conteudo) {
        this.conteudo = conteudo;
    }

    public String getMidiaUrl() {
        return midiaUrl;
    }

    public void setMidiaUrl(String midiaUrl) {
        this.midiaUrl = midiaUrl;
    }

    public LocalDateTime getEnviadoEm() {
        return enviadoEm;
    }

    public void setEnviadoEm(LocalDateTime enviadoEm) {
        this.enviadoEm = enviadoEm;
    }

    public String getEnvioStatus() {
        return envioStatus;
    }

    public void setEnvioStatus(String envioStatus) {
        this.envioStatus = envioStatus;
    }

    public String getEnvioErro() {
        return envioErro;
    }

    public void setEnvioErro(String envioErro) {
        this.envioErro = envioErro;
    }
}
