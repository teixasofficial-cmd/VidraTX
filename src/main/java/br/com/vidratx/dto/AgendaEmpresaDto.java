package br.com.vidratx.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class AgendaEmpresaDto {

    @NotNull(message = "Informe a duração da visita de medição")
    @Min(value = 15, message = "A visita de medição deve ter pelo menos 15 minutos")
    @Max(value = 600, message = "A visita de medição deve ter no máximo 10 horas")
    private Integer duracaoMedicaoMinutos;

    @NotNull(message = "Informe a duração da instalação")
    @Min(value = 30, message = "A instalação deve ter pelo menos 30 minutos")
    @Max(value = 1440, message = "A instalação deve ter no máximo 24 horas")
    private Integer duracaoInstalacaoMinutos;

    @NotNull(message = "Informe quantas medições podem acontecer ao mesmo tempo")
    @Min(value = 1, message = "Pelo menos uma medição por vez")
    @Max(value = 50, message = "No máximo 50 medições ao mesmo tempo")
    private Integer medicoesSimultaneas;

    @NotNull(message = "Informe o início do horário de mensagens automáticas")
    @Min(value = 0, message = "Hora inválida")
    @Max(value = 23, message = "Hora inválida")
    private Integer horaInicioMensagens;

    @NotNull(message = "Informe o fim do horário de mensagens automáticas")
    @Min(value = 1, message = "Hora inválida")
    @Max(value = 24, message = "Hora inválida")
    private Integer horaFimMensagens;

    @Size(max = 40, message = "Fuso horário inválido")
    private String fusoHorario;

    @Min(value = 0, message = "A antecedência não pode ser negativa")
    @Max(value = 10080, message = "A antecedência deve ser de no máximo 7 dias")
    private Integer antecedenciaMinimaMinutos;

    @Min(value = 5, message = "O prazo de resposta deve ser de pelo menos 5 minutos")
    @Max(value = 1440, message = "O prazo de resposta deve ser de no máximo 24 horas")
    private Integer prazoRespostaAtendenteMinutos;

    @Size(max = 500, message = "A mensagem de fora do horário deve ter no máximo 500 caracteres")
    private String mensagemForaHorario;

    @Size(max = 500, message = "As formas de pagamento devem ter no máximo 500 caracteres")
    private String condicoesPagamento;

    private Boolean lembreteOrcamentoAtivo;

    public Integer getDuracaoMedicaoMinutos() {
        return duracaoMedicaoMinutos;
    }

    public void setDuracaoMedicaoMinutos(Integer duracaoMedicaoMinutos) {
        this.duracaoMedicaoMinutos = duracaoMedicaoMinutos;
    }

    public Integer getDuracaoInstalacaoMinutos() {
        return duracaoInstalacaoMinutos;
    }

    public void setDuracaoInstalacaoMinutos(Integer duracaoInstalacaoMinutos) {
        this.duracaoInstalacaoMinutos = duracaoInstalacaoMinutos;
    }

    public Integer getMedicoesSimultaneas() {
        return medicoesSimultaneas;
    }

    public void setMedicoesSimultaneas(Integer medicoesSimultaneas) {
        this.medicoesSimultaneas = medicoesSimultaneas;
    }

    public Integer getHoraInicioMensagens() {
        return horaInicioMensagens;
    }

    public void setHoraInicioMensagens(Integer horaInicioMensagens) {
        this.horaInicioMensagens = horaInicioMensagens;
    }

    public Integer getHoraFimMensagens() {
        return horaFimMensagens;
    }

    public void setHoraFimMensagens(Integer horaFimMensagens) {
        this.horaFimMensagens = horaFimMensagens;
    }

    public String getFusoHorario() {
        return fusoHorario;
    }

    public void setFusoHorario(String fusoHorario) {
        this.fusoHorario = fusoHorario;
    }

    public Integer getAntecedenciaMinimaMinutos() {
        return antecedenciaMinimaMinutos;
    }

    public void setAntecedenciaMinimaMinutos(Integer antecedenciaMinimaMinutos) {
        this.antecedenciaMinimaMinutos = antecedenciaMinimaMinutos;
    }

    public Integer getPrazoRespostaAtendenteMinutos() {
        return prazoRespostaAtendenteMinutos;
    }

    public void setPrazoRespostaAtendenteMinutos(Integer prazoRespostaAtendenteMinutos) {
        this.prazoRespostaAtendenteMinutos = prazoRespostaAtendenteMinutos;
    }

    public String getMensagemForaHorario() {
        return mensagemForaHorario;
    }

    public void setMensagemForaHorario(String mensagemForaHorario) {
        this.mensagemForaHorario = mensagemForaHorario;
    }

    public String getCondicoesPagamento() {
        return condicoesPagamento;
    }

    public void setCondicoesPagamento(String condicoesPagamento) {
        this.condicoesPagamento = condicoesPagamento;
    }

    public Boolean getLembreteOrcamentoAtivo() {
        return lembreteOrcamentoAtivo;
    }

    public void setLembreteOrcamentoAtivo(Boolean lembreteOrcamentoAtivo) {
        this.lembreteOrcamentoAtivo = lembreteOrcamentoAtivo;
    }
}
