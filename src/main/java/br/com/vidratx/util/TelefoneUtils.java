package br.com.vidratx.util;

import java.util.ArrayList;
import java.util.List;

public final class TelefoneUtils {

    private static final String DDI_BRASIL = "55";

    private TelefoneUtils() {
    }

    public static String normalizar(String bruto) {

        if (bruto == null) {
            return null;
        }

        String digitos = bruto.replaceAll("\\D", "");

        if (digitos.isEmpty()) {
            return null;
        }

        if (digitos.startsWith("00")) {
            digitos = digitos.substring(2);
        }

        if (digitos.startsWith("0") && (digitos.length() == 11 || digitos.length() == 12)) {
            digitos = digitos.substring(1);
        }

        if (digitos.length() == 10 || digitos.length() == 11) {
            digitos = DDI_BRASIL + digitos;
        }

        return canonico(digitos);
    }

    public static String canonico(String digitos) {

        if (digitos == null || digitos.isEmpty()) {
            return digitos;
        }

        if (ehCelularBrasilSemNonoDigito(digitos)) {
            return digitos.substring(0, 4) + "9" + digitos.substring(4);
        }

        return digitos;
    }

    public static List<String> variantes(String numero) {

        List<String> variantes = new ArrayList<>();

        String canonico = canonico(numero);

        if (canonico == null || canonico.isEmpty()) {
            return variantes;
        }

        variantes.add(canonico);

        if (ehCelularBrasilComNonoDigito(canonico)) {
            variantes.add(canonico.substring(0, 4) + canonico.substring(5));
        }

        if (!variantes.contains(numero)) {
            variantes.add(numero);
        }

        return variantes;
    }

    public static boolean pareceValido(String normalizado) {

        if (normalizado == null) {
            return false;
        }

        if (normalizado.startsWith(DDI_BRASIL)) {
            return normalizado.length() == 12 || normalizado.length() == 13;
        }

        return normalizado.length() >= 8 && normalizado.length() <= 15;
    }

    private static boolean ehCelularBrasilSemNonoDigito(String digitos) {

        return digitos.length() == 12
                && digitos.startsWith(DDI_BRASIL)
                && digitos.charAt(4) >= '6';
    }

    private static boolean ehCelularBrasilComNonoDigito(String digitos) {

        return digitos.length() == 13
                && digitos.startsWith(DDI_BRASIL)
                && digitos.charAt(4) == '9';
    }
}
