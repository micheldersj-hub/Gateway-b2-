package com.gatewayb2.common.util;

/**
 * Validação de CPF e CNPJ pelo algoritmo oficial de dígitos verificadores (módulo 11).
 * Usado no onboarding (KYC) para garantir que documentos informados sejam consistentes
 * antes de seguir para verificação em bases externas.
 */
public final class DocumentValidator {

    private DocumentValidator() {
    }

    public static boolean isValidCpf(String rawCpf) {
        String cpf = onlyDigits(rawCpf);
        if (cpf.length() != 11 || allDigitsEqual(cpf)) {
            return false;
        }
        int firstDigit = calculateCpfCheckDigit(cpf.substring(0, 9), new int[]{10, 9, 8, 7, 6, 5, 4, 3, 2});
        int secondDigit = calculateCpfCheckDigit(cpf.substring(0, 9) + firstDigit, new int[]{11, 10, 9, 8, 7, 6, 5, 4, 3, 2});
        return cpf.equals(cpf.substring(0, 9) + firstDigit + secondDigit);
    }

    public static boolean isValidCnpj(String rawCnpj) {
        String cnpj = onlyDigits(rawCnpj);
        if (cnpj.length() != 14 || allDigitsEqual(cnpj)) {
            return false;
        }
        int[] firstWeights = {5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int[] secondWeights = {6, 5, 4, 3, 2, 9, 8, 7, 6, 5, 4, 3, 2};
        int firstDigit = calculateCheckDigit(cnpj.substring(0, 12), firstWeights);
        int secondDigit = calculateCheckDigit(cnpj.substring(0, 12) + firstDigit, secondWeights);
        return cnpj.equals(cnpj.substring(0, 12) + firstDigit + secondDigit);
    }

    public static String onlyDigits(String value) {
        return value == null ? "" : value.replaceAll("\\D", "");
    }

    private static int calculateCpfCheckDigit(String base, int[] weights) {
        int sum = 0;
        for (int i = 0; i < weights.length; i++) {
            sum += Character.getNumericValue(base.charAt(i)) * weights[i];
        }
        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }

    private static int calculateCheckDigit(String base, int[] weights) {
        int sum = 0;
        for (int i = 0; i < weights.length; i++) {
            sum += Character.getNumericValue(base.charAt(i)) * weights[i];
        }
        int remainder = sum % 11;
        return remainder < 2 ? 0 : 11 - remainder;
    }

    private static boolean allDigitsEqual(String value) {
        return value.chars().distinct().count() == 1;
    }
}
