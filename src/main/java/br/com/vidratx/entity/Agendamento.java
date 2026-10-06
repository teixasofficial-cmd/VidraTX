package br.com.vidratx.entity;

import br.com.vidratx.enums.StatusAgendamento;
import br.com.vidratx.enums.TipoAgendamento;

import java.time.LocalDateTime;

public interface Agendamento {

    Long getId();

    TipoAgendamento getTipoAgendamento();

    StatusAgendamento getStatus();

    void setStatus(StatusAgendamento status);

    LocalDateTime getDataAgendada();

    void setDataAgendada(LocalDateTime dataAgendada);

    Integer getVersaoProposta();

    void setVersaoProposta(Integer versaoProposta);

    String getContrapropostaTexto();

    void setContrapropostaTexto(String contrapropostaTexto);

    LocalDateTime getCancelamentoSolicitadoEm();

    void setCancelamentoSolicitadoEm(LocalDateTime cancelamentoSolicitadoEm);

    String getCancelamentoSolicitadoTexto();

    void setCancelamentoSolicitadoTexto(String cancelamentoSolicitadoTexto);

    String getEndereco();

    String getEquipeResponsavel();

    Orcamento getOrcamentoReferencia();
}
