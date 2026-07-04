package com.gatewayb2.card.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class CardEncryptionServiceTest {

    private final CardEncryptionService service = new CardEncryptionService("Zm6G4x6M19QuCKI080PtMDy3RVUYQIhKaId83Eu7lqQ=");

    @Test
    void encryptsAndDecryptsRoundTrip() {
        String pan = "9999999999991234";
        String encrypted = service.encrypt(pan);

        assertThat(encrypted).isNotEqualTo(pan);
        assertThat(service.decrypt(encrypted)).isEqualTo(pan);
    }

    @Test
    void encryptingTwiceProducesDifferentCipherTextDueToRandomIv() {
        String pan = "9999999999991234";
        assertThat(service.encrypt(pan)).isNotEqualTo(service.encrypt(pan));
    }
}
