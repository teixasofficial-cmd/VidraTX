package br.com.vidratx.validator;

public final class CnpjValidator {

    private CnpjValidator() {
    }

    public static boolean isValid(
            String cnpj) {

        if (cnpj == null) {
            return false;
        }

        String numeros =
                cnpj.replaceAll("\\D", "");

        if (numeros.length() != 14) {
            return false;
        }

        if (numeros.chars().distinct().count() == 1) {
            return false;
        }

        int primeiroDigito =
                calcularDigito(
                        numeros.substring(0, 12)
                );

        int segundoDigito =
                calcularDigito(
                        numeros.substring(0, 12)
                                + primeiroDigito
                );

        return numeros.equals(
                numeros.substring(0, 12)
                        + primeiroDigito
                        + segundoDigito
        );
    }

    private static int calcularDigito(
            String cnpj) {

        int[] pesos =
                cnpj.length() == 12
                        ? new int[]{
                        5, 4, 3, 2,
                        9, 8, 7, 6,
                        5, 4, 3, 2
                }
                        : new int[]{
                        6, 5, 4, 3, 2,
                        9, 8, 7, 6,
                        5, 4, 3, 2
                };

        int soma = 0;

        for (int i = 0;
             i < cnpj.length();
             i++) {

            soma +=
                    Character.digit(
                            cnpj.charAt(i),
                            10
                    ) * pesos[i];
        }

        int resto = soma % 11;

        return resto < 2
                ? 0
                : 11 - resto;
    }

}
