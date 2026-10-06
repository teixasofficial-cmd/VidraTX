package br.com.vidratx.validator;

public final class CpfCnpjValidator {

    private CpfCnpjValidator() {
    }

    public static boolean isValid(
            String documento) {

        if (documento == null) {
            return false;
        }

        String numeros =
                documento.replaceAll("\\D", "");

        if (numeros.length() == 11) {
            return isCpfValido(numeros);
        }

        if (numeros.length() == 14) {
            return CnpjValidator.isValid(numeros);
        }

        return false;
    }

    private static boolean isCpfValido(String cpf) {

        if (cpf.chars().distinct().count() == 1) {
            return false;
        }

        int primeiroDigito =
                calcularDigito(
                        cpf.substring(0, 9)
                );

        int segundoDigito =
                calcularDigito(
                        cpf.substring(0, 9)
                                + primeiroDigito
                );

        return cpf.equals(
                cpf.substring(0, 9)
                        + primeiroDigito
                        + segundoDigito
        );
    }

    private static int calcularDigito(String base) {

        int pesoInicial = base.length() + 1;
        int soma = 0;

        for (int i = 0; i < base.length(); i++) {

            soma +=
                    Character.digit(base.charAt(i), 10)
                            * (pesoInicial - i);
        }

        int resto = soma % 11;

        return resto < 2 ? 0 : 11 - resto;
    }
}
