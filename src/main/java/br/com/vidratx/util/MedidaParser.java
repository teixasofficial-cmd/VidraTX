package br.com.vidratx.util;

import java.util.Locale;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class MedidaParser {

    public static final int LIMITE_MINIMO_MM = 50;
    public static final int LIMITE_MAXIMO_MM = 6000;

    private static final String MEDIDA =
            "(?:(\\d+)\\s*m\\s*(\\d{1,2})(?![\\d.,])"
                    + "|(\\d+(?:[.,]\\d+)?)\\s*(mm|cm|m|metros?|centimetros?|centímetros?|milimetros?|milímetros?)?"
                    + "(?![a-wyzà-ú]))";

    private static final Pattern PAR = Pattern.compile(
            MEDIDA + "\\s*(?:x|×|\\*|por)\\s*" + MEDIDA
    );

    private MedidaParser() {
    }

    public record MedidaParseada(int larguraMm, int alturaMm) {

        public String descricaoEmMetros() {
            return metros(larguraMm) + " × " + metros(alturaMm);
        }

        private static String metros(int mm) {
            return String.format(Locale.forLanguageTag("pt-BR"), "%.2f m", mm / 1000.0);
        }
    }

    public static Optional<MedidaParseada> tentarInterpretar(String texto) {

        if (texto == null || texto.isBlank()) {
            return Optional.empty();
        }

        Matcher matcher = PAR.matcher(texto.toLowerCase(Locale.ROOT));

        if (!matcher.find()) {
            return Optional.empty();
        }

        Integer larguraMm = paraMm(matcher.group(1), matcher.group(2), matcher.group(3), matcher.group(4));
        Integer alturaMm = paraMm(matcher.group(5), matcher.group(6), matcher.group(7), matcher.group(8));

        if (larguraMm == null || alturaMm == null || !plausivel(larguraMm) || !plausivel(alturaMm)) {
            return Optional.empty();
        }

        return Optional.of(new MedidaParseada(larguraMm, alturaMm));
    }

    private static Integer paraMm(String metrosInteiros, String cm, String numero, String unidade) {

        if (metrosInteiros != null) {
            return Integer.parseInt(metrosInteiros) * 1000 + centimetros(cm) * 10;
        }

        return numero == null ? null : interpretarNumeroEmMm(numero, unidade);
    }

    public static boolean plausivel(int mm) {
        return mm >= LIMITE_MINIMO_MM && mm <= LIMITE_MAXIMO_MM;
    }

    private static int centimetros(String digitos) {
        return digitos.length() == 1 ? Integer.parseInt(digitos) * 10 : Integer.parseInt(digitos);
    }

    private static Integer interpretarNumeroEmMm(String token, String unidade) {

        boolean temSeparadorDecimal = token.indexOf(',') >= 0 || token.indexOf('.') >= 0;

        double valor;

        try {
            valor = Double.parseDouble(token.replace(',', '.'));
        } catch (NumberFormatException excecao) {
            return null;
        }

        double fator;

        if (unidade != null) {
            fator = unidade.startsWith("mm") || unidade.startsWith("mil") ? 1 : unidade.startsWith("c") ? 10 : 1000;
        } else if (temSeparadorDecimal || token.length() == 1) {
            fator = 1000;
        } else {
            fator = token.length() <= 3 ? 10 : 1;
        }

        return (int) Math.round(valor * fator);
    }
}
