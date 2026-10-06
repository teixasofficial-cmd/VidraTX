package br.com.vidratx.service;

import br.com.vidratx.config.FusoEmpresa;
import br.com.vidratx.entity.Empresa;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Optional;

@Component
public class HorarioAtendimento {

    static final String MENSAGEM_PADRAO =
            "Estamos fora do horário de atendimento (segunda a sábado, das {inicio}h às {fim}h). "
                    + "Um atendente te responde a partir de {abertura}.";

    private static final Locale PT_BR = Locale.forLanguageTag("pt-BR");
    private static final DateTimeFormatter DIA_MES = DateTimeFormatter.ofPattern("dd/MM", PT_BR);

    private final FusoEmpresa fuso;

    public HorarioAtendimento(Clock clock) {
        this.fuso = new FusoEmpresa(clock);
    }

    public boolean aberto(Empresa empresa) {
        return WhatsappSaidaDespachante.dentroDoHorario(empresa, fuso.agora(empresa));
    }

    public Optional<String> avisoForaDoHorario(Empresa empresa) {

        if (empresa == null || aberto(empresa)) {
            return Optional.empty();
        }

        LocalDateTime agora = fuso.agora(empresa);
        LocalDateTime abertura = WhatsappSaidaDespachante.proximoHorarioPermitido(empresa, agora);

        String modelo = empresa.getMensagemForaHorario() != null && !empresa.getMensagemForaHorario().isBlank()
                ? empresa.getMensagemForaHorario() : MENSAGEM_PADRAO;

        return Optional.of(modelo
                .replace("{inicio}", String.valueOf(hora(empresa.getHoraInicioMensagens(), 8)))
                .replace("{fim}", String.valueOf(hora(empresa.getHoraFimMensagens(), 20)))
                .replace("{abertura}", descrever(abertura, agora)));
    }

    static String descrever(LocalDateTime abertura, LocalDateTime agora) {

        String hora = abertura.getHour() + "h" + (abertura.getMinute() > 0 ? String.format("%02d", abertura.getMinute()) : "");

        if (abertura.toLocalDate().equals(agora.toLocalDate())) {
            return "hoje às " + hora;
        }

        if (abertura.toLocalDate().equals(agora.toLocalDate().plusDays(1))) {
            return "amanhã às " + hora;
        }

        return abertura.getDayOfWeek().getDisplayName(TextStyle.FULL, PT_BR)
                + " (" + abertura.format(DIA_MES) + ") às " + hora;
    }

    private static int hora(Integer configurada, int padrao) {
        return configurada != null ? configurada : padrao;
    }
}
