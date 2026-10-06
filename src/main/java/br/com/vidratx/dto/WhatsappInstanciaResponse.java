package br.com.vidratx.dto;

import br.com.vidratx.enums.StatusInstanciaWhatsapp;

import java.time.LocalDateTime;

public class WhatsappInstanciaResponse {

    private Long id;
    private String numero;
    private StatusInstanciaWhatsapp status;
    private String qrCode;
    private long mensagensNaFila;
    private long mensagensComFalha;
    private LocalDateTime conectadoEm;
    private LocalDateTime criadoEm;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNumero() {
        return numero;
    }

    public void setNumero(String numero) {
        this.numero = numero;
    }

    public StatusInstanciaWhatsapp getStatus() {
        return status;
    }

    public void setStatus(StatusInstanciaWhatsapp status) {
        this.status = status;
    }

    public String getQrCode() {
        return qrCode;
    }

    public void setQrCode(String qrCode) {
        this.qrCode = qrCode;
    }

    public long getMensagensNaFila() {
        return mensagensNaFila;
    }

    public void setMensagensNaFila(long mensagensNaFila) {
        this.mensagensNaFila = mensagensNaFila;
    }

    public long getMensagensComFalha() {
        return mensagensComFalha;
    }

    public void setMensagensComFalha(long mensagensComFalha) {
        this.mensagensComFalha = mensagensComFalha;
    }

    public LocalDateTime getConectadoEm() {
        return conectadoEm;
    }

    public void setConectadoEm(LocalDateTime conectadoEm) {
        this.conectadoEm = conectadoEm;
    }

    public LocalDateTime getCriadoEm() {
        return criadoEm;
    }

    public void setCriadoEm(LocalDateTime criadoEm) {
        this.criadoEm = criadoEm;
    }
}
