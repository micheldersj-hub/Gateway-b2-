package com.gatewayb2.common.util;

/**
 * Algoritmo de Luhn (mod 10), usado para gerar e validar números de cartão (PAN) simulados.
 */
public final class LuhnUtils {

    private LuhnUtils() {
    }

    public static boolean isValid(String number) {
        String digits = DocumentValidator.onlyDigits(number);
        if (digits.isEmpty()) {
            return false;
        }
        int sum = 0;
        boolean alternate = false;
        for (int i = digits.length() - 1; i >= 0; i--) {
            int n = Character.getNumericValue(digits.charAt(i));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n -= 9;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        return sum % 10 == 0;
    }

    /** Calcula o dígito verificador de Luhn para completar um número base. */
    public static int checkDigit(String partialNumberWithoutCheckDigit) {
        String digits = DocumentValidator.onlyDigits(partialNumberWithoutCheckDigit);
        int sum = 0;
        boolean alternate = true;
        for (int i = digits.length() - 1; i >= 0; i--) {
            int n = Character.getNumericValue(digits.charAt(i));
            if (alternate) {
                n *= 2;
                if (n > 9) {
                    n -= 9;
                }
            }
            sum += n;
            alternate = !alternate;
        }
        int mod = sum % 10;
        return mod == 0 ? 0 : 10 - mod;
    }
}
