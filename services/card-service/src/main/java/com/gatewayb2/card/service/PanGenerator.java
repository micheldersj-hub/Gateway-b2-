package com.gatewayb2.card.service;

import com.gatewayb2.common.util.LuhnUtils;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;

/** Gera números de cartão (PAN) simulados, válidos pelo algoritmo de Luhn, com BIN fictício. */
@Component
public class PanGenerator {

    /** BIN fictício (não pertence a nenhuma bandeira real) reservado para ambientes de teste/demo. */
    private static final String TEST_BIN = "999999";
    private static final SecureRandom RANDOM = new SecureRandom();

    public String generatePan() {
        StringBuilder base = new StringBuilder(TEST_BIN);
        for (int i = 0; i < 9; i++) {
            base.append(RANDOM.nextInt(10));
        }
        int checkDigit = LuhnUtils.checkDigit(base.toString());
        return base + String.valueOf(checkDigit);
    }

    public String generateCvv() {
        return String.format("%03d", RANDOM.nextInt(1000));
    }

    public String mask(String pan) {
        String last4 = pan.substring(pan.length() - 4);
        return "**** **** **** " + last4;
    }
}
