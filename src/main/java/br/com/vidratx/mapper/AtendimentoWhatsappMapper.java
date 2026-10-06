package br.com.vidratx.mapper;

import br.com.vidratx.dto.AtendimentoWhatsappDetalheResponse;
import br.com.vidratx.dto.AtendimentoWhatsappResponse;
import br.com.vidratx.dto.HistoricoResponse;
import br.com.vidratx.dto.MensagemAtendimentoResponse;
import br.com.vidratx.entity.AtendimentoWhatsapp;
import br.com.vidratx.entity.MensagemAtendimento;
import br.com.vidratx.enums.StatusAtendimento;
import br.com.vidratx.enums.TipoMensagem;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class AtendimentoWhatsappMapper {

    public AtendimentoWhatsappResponse toResponse(AtendimentoWhatsapp atendimento) {

        AtendimentoWhatsappResponse response = new AtendimentoWhatsappResponse();

        response.setId(atendimento.getId());

        if (atendimento.getCliente() != null) {
            response.setClienteId(atendimento.getCliente().getId());
            response.setClienteNome(atendimento.getCliente().getNome());
        }

        response.setTelefone(atendimento.getTelefone());
        response.setStatus(atendimento.getStatus());
        response.setEtapaFluxo(atendimento.getEtapaFluxo());
        response.setTentativasErro(atendimento.getTentativasErro());

        if (atendimento.getSolicitacaoOrcamento() != null) {
            response.setSolicitacaoOrcamentoId(
                    atendimento.getSolicitacaoOrcamento().getId()
            );
        }

        if (atendimento.getAtendente() != null) {
            response.setAtendenteId(atendimento.getAtendente().getId());
            response.setAtendenteNome(atendimento.getAtendente().getNome());
        }

        response.setCriadoEm(atendimento.getCriadoEm());
        response.setAtualizadoEm(atendimento.getAtualizadoEm());
        response.setEncerradoEm(atendimento.getEncerradoEm());
        response.setMotivoEncerramento(atendimento.getMotivoEncerramento());
        response.setUltimaMensagemClienteEm(atendimento.getUltimaMensagemClienteEm());
        response.setUltimaMensagemEmpresaEm(atendimento.getUltimaMensagemEmpresaEm());
        response.setClienteAguardandoResposta(
                atendimento.getStatus() != StatusAtendimento.ENCERRADO && atendimento.clienteAguardandoResposta()
        );

        preencherProximaAcao(atendimento, response);

        return response;
    }

    public AtendimentoWhatsappResponse toResponse(AtendimentoWhatsapp atendimento, LocalDateTime agora) {

        AtendimentoWhatsappResponse response = toResponse(atendimento);

        preencherEspera(response, atendimento, agora);

        return response;
    }

    public void preencherEspera(AtendimentoWhatsappResponse response, AtendimentoWhatsapp atendimento, LocalDateTime agora) {

        long minutos = atendimento.minutosEsperandoResposta(agora);
        boolean atrasada = atendimento.respostaAtrasada(agora);

        response.setMinutosEsperando(minutos);
        response.setRespostaAtrasada(atrasada);

        if (atrasada && response.getProximaAcao() != null) {
            response.setProximaAcao(response.getProximaAcao() + " — cliente esperando há " + descreverEspera(minutos)
                    + " (prazo de " + atendimento.prazoRespostaMinutos() + " min)");
        }
    }

    public static String descreverEspera(long minutos) {

        if (minutos < 60) {
            return minutos + " min";
        }

        long horas = minutos / 60;

        return horas < 48 ? horas + " h" + (minutos % 60 > 0 ? " " + (minutos % 60) + " min" : "") : (horas / 24) + " dias";
    }

    private void preencherProximaAcao(AtendimentoWhatsapp atendimento, AtendimentoWhatsappResponse response) {

        switch (atendimento.getStatus()) {

            case AGUARDANDO_ATENDENTE -> {
                response.setResponsavelProximaAcao("EMPRESA");
                response.setProximaAcao("Assumir a conversa e responder o cliente");
            }

            case EM_ATENDIMENTO_HUMANO -> {
                if (atendimento.clienteAguardandoResposta()) {
                    response.setResponsavelProximaAcao("EMPRESA");
                    response.setProximaAcao("Responder o cliente"
                            + (atendimento.getAtendente() != null ? " (" + atendimento.getAtendente().getNome() + ")" : ""));
                } else {
                    response.setResponsavelProximaAcao("CLIENTE");
                    response.setProximaAcao("Aguardando o cliente");
                }
            }

            case EM_FLUXO_BOT -> {
                response.setResponsavelProximaAcao("CLIENTE");
                response.setProximaAcao("Cliente conversando com o bot");
            }

            case ENCERRADO -> {
                response.setResponsavelProximaAcao("NINGUEM");
                response.setProximaAcao("Conversa encerrada");
            }
        }
    }

    public MensagemAtendimentoResponse toMensagemResponse(MensagemAtendimento mensagem) {

        MensagemAtendimentoResponse response = new MensagemAtendimentoResponse();

        response.setId(mensagem.getId());
        response.setRemetente(mensagem.getRemetente());
        response.setTipo(mensagem.getTipo());
        response.setConteudo(mensagem.getConteudo());
        response.setEnviadoEm(mensagem.getEnviadoEm());

        if (mensagem.getMensagemSaida() != null) {
            response.setEnvioStatus(mensagem.getMensagemSaida().getStatus().name());
            response.setEnvioErro(mensagem.getMensagemSaida().getUltimoErro());
        }

        if (mensagem.getTipo() == TipoMensagem.IMAGEM) {
            response.setMidiaUrl("/api/atendimentos-whatsapp/midia/" + mensagem.getId());
        }

        return response;
    }

    public AtendimentoWhatsappDetalheResponse toDetalheResponse(
            AtendimentoWhatsapp atendimento,
            List<MensagemAtendimento> mensagens,
            List<HistoricoResponse> historico) {

        AtendimentoWhatsappDetalheResponse response =
                new AtendimentoWhatsappDetalheResponse();

        response.setAtendimento(toResponse(atendimento));

        response.setMensagens(
                mensagens.stream()
                        .map(this::toMensagemResponse)
                        .toList()
        );

        response.setHistorico(historico);

        return response;
    }
}
