package com.gatewayb2.card.service;

import com.gatewayb2.common.util.LuhnUtils;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PanGeneratorTest {

    private final PanGenerator generator = new PanGenerator();

    @Test
    void generatesValidLuhnPan() {
        String pan = generator.generatePan();
        assertThat(pan).hasSize(16);
        assertThat(LuhnUtils.isValid(pan)).isTrue();
    }

    @Test
    void masksAllButLastFourDigits() {
        String pan = "9999999999991234";
        assertThat(generator.mask(pan)).isEqualTo("**** **** **** 1234");
    }
}
