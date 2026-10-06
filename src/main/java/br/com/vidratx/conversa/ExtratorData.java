package br.com.vidratx.conversa;

import java.text.Normalizer;
import java.time.DateTimeException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.TemporalAdjusters;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class ExtratorData {

    private static final Pattern DATA_BARRA = Pattern.compile("\\b(\\d{1,2})/(\\d{1,2})(?:/(\\d{2,4}))?\\b");
    private static final Pattern DIA_DO_MES = Pattern.compile("\\bdia\\s+(\\d{1,2})\\b(?!/)");
    private static final Pattern HORA_H = Pattern.compile("\\b(\\d{1,2})\\s*(?:h|hs|hr|hrs|horas?)\\s*(\\d{2})?\\b");
    private static final Pattern HORA_DOIS_PONTOS = Pattern.compile("\\b(\\d{1,2}):(\\d{2})\\b");
    private static final Pattern HORA_AS = Pattern.compile("\\b(?:as|a partir das|depois das|apos as)\\s+(\\d{1,2})\\b");

    private static final Map<String, DayOfWeek> DIAS_DA_SEMANA = Map.of(
            "segunda", DayOfWeek.MONDAY,
            "terca", DayOfWeek.TUESDAY,
            "quarta", DayOfWeek.WEDNESDAY,
            "quinta", DayOfWeek.THURSDAY,
            "sexta", DayOfWeek.FRIDAY,
            "sabado", DayOfWeek.SATURDAY,
            "domingo", DayOfWeek.SUNDAY
    );

    private ExtratorData() {
    }

    public static Optional<LocalDateTime> extrair(String texto, LocalDateTime referencia) {

        if (texto == null || texto.isBlank() || referencia == null) {
            return Optional.empty();
        }

        String normalizado = Normalizer.normalize(texto, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT);

        Optional<LocalTime> hora = extrairHora(normalizado);

        if (hora.isEmpty()) {
            return Optional.empty();
        }

        return extrairDia(normalizado, referencia.toLocalDate())
                .map(dia -> dia.atTime(hora.get()))
                .filter(dataHora -> dataHora.isAfter(referencia));
    }

    private static Optional<LocalDate> extrairDia(String texto, LocalDate hoje) {

        Set<LocalDate> dias = new HashSet<>();

        Matcher barra = DATA_BARRA.matcher(texto);

        while (barra.find()) {

            int dia = Integer.parseInt(barra.group(1));
            int mes = Integer.parseInt(barra.group(2));

            try {

                if (barra.group(3) != null) {
                    int ano = Integer.parseInt(barra.group(3));
                    dias.add(LocalDate.of(ano < 100 ? 2000 + ano : ano, mes, dia));
                } else {
                    LocalDate candidata = LocalDate.of(hoje.getYear(), mes, dia);
                    dias.add(candidata.isBefore(hoje) ? candidata.plusYears(1) : candidata);
                }

            } catch (DateTimeException ex) {
                return Optional.empty();
            }
        }

        if (texto.contains("depois de amanha")) {
            dias.add(hoje.plusDays(2));
        } else if (texto.contains("amanha")) {
            dias.add(hoje.plusDays(1));
        }

        if (texto.matches("(?s).*\\bhoje\\b.*")) {
            dias.add(hoje);
        }

        Matcher diaDoMes = DIA_DO_MES.matcher(texto);

        while (diaDoMes.find()) {

            int dia = Integer.parseInt(diaDoMes.group(1));

            try {

                LocalDate candidata = hoje.withDayOfMonth(dia);
                dias.add(candidata.isBefore(hoje) ? hoje.plusMonths(1).withDayOfMonth(dia) : candidata);

            } catch (DateTimeException ex) {
                return Optional.empty();
            }
        }

        for (Map.Entry<String, DayOfWeek> dia : DIAS_DA_SEMANA.entrySet()) {

            if (texto.matches("(?s).*\\b" + dia.getKey() + "\\b.*")) {
                dias.add(hoje.with(TemporalAdjusters.next(dia.getValue())));
            }
        }

        return dias.size() == 1 ? Optional.of(dias.iterator().next()) : Optional.empty();
    }

    private static Optional<LocalTime> extrairHora(String texto) {

        boolean manha = texto.contains("da manha") || texto.contains("de manha");
        Set<LocalTime> horas = new HashSet<>();

        if (texto.contains("meio dia") || texto.contains("meio-dia")) {
            horas.add(LocalTime.NOON);
        }

        Matcher doisPontos = HORA_DOIS_PONTOS.matcher(texto);

        while (doisPontos.find()) {
            horas.add(hora(Integer.parseInt(doisPontos.group(1)), Integer.parseInt(doisPontos.group(2)), manha));
        }

        Matcher horaH = HORA_H.matcher(texto);

        while (horaH.find()) {
            horas.add(hora(Integer.parseInt(horaH.group(1)),
                    horaH.group(2) != null ? Integer.parseInt(horaH.group(2)) : 0, manha));
        }

        Matcher horaAs = HORA_AS.matcher(texto);

        while (horaAs.find()) {

            int hora = Integer.parseInt(horaAs.group(1));
            LocalTime cheia = hora(hora, 0, manha);

            if (cheia != null && horas.stream().noneMatch(h -> h != null && h.getHour() == cheia.getHour())) {
                horas.add(cheia);
            }
        }

        if (horas.contains(null) || horas.size() != 1) {
            return Optional.empty();
        }

        return Optional.of(horas.iterator().next());
    }

    private static LocalTime hora(int hora, int minuto, boolean manha) {

        if (hora >= 1 && hora <= 6 && !manha) {
            hora += 12;
        }

        if (hora > 23 || minuto > 59) {
            return null;
        }

        return LocalTime.of(hora, minuto);
    }
}
