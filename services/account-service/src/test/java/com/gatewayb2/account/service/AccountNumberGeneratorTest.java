package com.gatewayb2.account.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AccountNumberGeneratorTest {

    private final AccountNumberGenerator generator = new AccountNumberGenerator();

    @Test
    void generatesNumberWithCheckDigitFormat() {
        String number = generator.generate();
        assertThat(number).matches("\\d{8}-\\d");
    }
}
