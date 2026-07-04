package com.gatewayb2.gateway.filter;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApiKeyHasherTest {

    @Test
    void hashesKnownSandboxKeyToExpectedDigest() {
        String key = "gwb2_sandbox_2ae1b72882db4b87c712424149ef8524";
        assertThat(ApiKeyHasher.sha256Hex(key))
                .isEqualTo("613802a4e057cd16fa87036066b406ade4e00738cb8d386e54223ebc98973c39");
    }

    @Test
    void isDeterministic() {
        assertThat(ApiKeyHasher.sha256Hex("abc")).isEqualTo(ApiKeyHasher.sha256Hex("abc"));
    }

    @Test
    void differentInputsProduceDifferentHashes() {
        assertThat(ApiKeyHasher.sha256Hex("abc")).isNotEqualTo(ApiKeyHasher.sha256Hex("abd"));
    }
}
