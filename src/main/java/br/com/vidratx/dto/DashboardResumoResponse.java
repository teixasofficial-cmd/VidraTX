package br.com.vidratx.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class DashboardResumoResponse {

    private long orcamentosRascunho;
    private long orcamentosEnviados;
    private long orcamentosAprovados;
    private long orcamentosRecusados;
    private BigDecimal valorAprovado;
    private BigDecimal valorTotalEnviado;
    private long totalOrcamentosEnviados;
    private double taxaConversaoPercentual;
    private long atendimentosPendentesWhatsapp;

    private long atendimentosAtrasadosWhatsapp;
    private long medicoesHoje;
    private long instalacoesHoje;
    private long acoesDaEmpresa;
    private long aguardandoCliente;
    private long mensagensNaoEntregues;

    private String whatsappStatus;
    private LocalDateTime whatsappDesconectadoEm;
    private long medicoesAguardandoCliente;
    private long contrapropostasPendentes;
    private long instalacoesAguardandoAgendamento;
    private long orcamentosAprovadosSemOs;

    public long getOrcamentosRascunho() {
        return orcamentosRascunho;
    }

    public void setOrcamentosRascunho(long orcamentosRascunho) {
        this.orcamentosRascunho = orcamentosRascunho;
    }

    public long getOrcamentosEnviados() {
        return orcamentosEnviados;
    }

    public void setOrcamentosEnviados(long orcamentosEnviados) {
        this.orcamentosEnviados = orcamentosEnviados;
    }

    public long getOrcamentosAprovados() {
        return orcamentosAprovados;
    }

    public void setOrcamentosAprovados(long orcamentosAprovados) {
        this.orcamentosAprovados = orcamentosAprovados;
    }

    public long getOrcamentosRecusados() {
        return orcamentosRecusados;
    }

    public void setOrcamentosRecusados(long orcamentosRecusados) {
        this.orcamentosRecusados = orcamentosRecusados;
    }

    public BigDecimal getValorAprovado() {
        return valorAprovado;
    }

    public void setValorAprovado(BigDecimal valorAprovado) {
        this.valorAprovado = valorAprovado;
    }

    public long getAtendimentosPendentesWhatsapp() {
        return atendimentosPendentesWhatsapp;
    }

    public void setAtendimentosPendentesWhatsapp(long atendimentosPendentesWhatsapp) {
        this.atendimentosPendentesWhatsapp = atendimentosPendentesWhatsapp;
    }

    public long getAtendimentosAtrasadosWhatsapp() {
        return atendimentosAtrasadosWhatsapp;
    }

    public void setAtendimentosAtrasadosWhatsapp(long atendimentosAtrasadosWhatsapp) {
        this.atendimentosAtrasadosWhatsapp = atendimentosAtrasadosWhatsapp;
    }

    public BigDecimal getValorTotalEnviado() {
        return valorTotalEnviado;
    }

    public void setValorTotalEnviado(BigDecimal valorTotalEnviado) {
        this.valorTotalEnviado = valorTotalEnviado;
    }

    public double getTaxaConversaoPercentual() {
        return taxaConversaoPercentual;
    }

    public void setTaxaConversaoPercentual(double taxaConversaoPercentual) {
        this.taxaConversaoPercentual = taxaConversaoPercentual;
    }

    public long getTotalOrcamentosEnviados() {
        return totalOrcamentosEnviados;
    }

    public void setTotalOrcamentosEnviados(long totalOrcamentosEnviados) {
        this.totalOrcamentosEnviados = totalOrcamentosEnviados;
    }

    public long getMedicoesHoje() {
        return medicoesHoje;
    }

    public void setMedicoesHoje(long medicoesHoje) {
        this.medicoesHoje = medicoesHoje;
    }

    public long getInstalacoesHoje() {
        return instalacoesHoje;
    }

    public void setInstalacoesHoje(long instalacoesHoje) {
        this.instalacoesHoje = instalacoesHoje;
    }

    public long getAcoesDaEmpresa() {
        return acoesDaEmpresa;
    }

    public void setAcoesDaEmpresa(long acoesDaEmpresa) {
        this.acoesDaEmpresa = acoesDaEmpresa;
    }

    public long getAguardandoCliente() {
        return aguardandoCliente;
    }

    public void setAguardandoCliente(long aguardandoCliente) {
        this.aguardandoCliente = aguardandoCliente;
    }

    public long getMensagensNaoEntregues() {
        return mensagensNaoEntregues;
    }

    public void setMensagensNaoEntregues(long mensagensNaoEntregues) {
        this.mensagensNaoEntregues = mensagensNaoEntregues;
    }

    public long getMedicoesAguardandoCliente() {
        return medicoesAguardandoCliente;
    }

    public void setMedicoesAguardandoCliente(long medicoesAguardandoCliente) {
        this.medicoesAguardandoCliente = medicoesAguardandoCliente;
    }

    public long getContrapropostasPendentes() {
        return contrapropostasPendentes;
    }

    public void setContrapropostasPendentes(long contrapropostasPendentes) {
        this.contrapropostasPendentes = contrapropostasPendentes;
    }

    public long getInstalacoesAguardandoAgendamento() {
        return instalacoesAguardandoAgendamento;
    }

    public void setInstalacoesAguardandoAgendamento(long instalacoesAguardandoAgendamento) {
        this.instalacoesAguardandoAgendamento = instalacoesAguardandoAgendamento;
    }

    public long getOrcamentosAprovadosSemOs() {
        return orcamentosAprovadosSemOs;
    }

    public void setOrcamentosAprovadosSemOs(long orcamentosAprovadosSemOs) {
        this.orcamentosAprovadosSemOs = orcamentosAprovadosSemOs;
    }

    public String getWhatsappStatus() {
        return whatsappStatus;
    }

    public void setWhatsappStatus(String whatsappStatus) {
        this.whatsappStatus = whatsappStatus;
    }

    public LocalDateTime getWhatsappDesconectadoEm() {
        return whatsappDesconectadoEm;
    }

    public void setWhatsappDesconectadoEm(LocalDateTime whatsappDesconectadoEm) {
        this.whatsappDesconectadoEm = whatsappDesconectadoEm;
    }
}
