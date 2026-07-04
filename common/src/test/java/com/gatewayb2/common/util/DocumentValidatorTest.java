package com.gatewayb2.common.util;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class DocumentValidatorTest {

    @Test
    void validCpfIsAccepted() {
        assertThat(DocumentValidator.isValidCpf("529.982.247-25")).isTrue();
    }

    @Test
    void cpfWithAllRepeatedDigitsIsRejected() {
        assertThat(DocumentValidator.isValidCpf("111.111.111-11")).isFalse();
    }

    @Test
    void cpfWithWrongCheckDigitIsRejected() {
        assertThat(DocumentValidator.isValidCpf("529.982.247-00")).isFalse();
    }

    @Test
    void validCnpjIsAccepted() {
        assertThat(DocumentValidator.isValidCnpj("11.222.333/0001-81")).isTrue();
    }

    @Test
    void cnpjWithWrongCheckDigitIsRejected() {
        assertThat(DocumentValidator.isValidCnpj("11.222.333/0001-00")).isFalse();
    }
}
