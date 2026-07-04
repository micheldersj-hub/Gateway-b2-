package com.gatewayb2.account.service;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/** Gera número de conta de 8 dígitos + dígito verificador, no padrão comum de bancos brasileiros. */
@Component
public class AccountNumberGenerator {

    private static final SecureRandom RANDOM = new SecureRandom();
    public static final String DEFAULT_BRANCH = "0001";

    public String generate() {
        int base = 10_000_000 + RANDOM.nextInt(90_000_000);
        int checkDigit = base % 9;
        return base + "-" + checkDigit;
    }
}
