package com.gatewayb2.kyc.service;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class RiskScoringEngineTest {

    private final RiskScoringEngine engine = new RiskScoringEngine();

    @Test
    void scoreIsDeterministicAndWithinRange() {
        int score1 = engine.score("52998224725");
        int score2 = engine.score("52998224725");

        assertThat(score1).isEqualTo(score2);
        assertThat(score1).isBetween(0, 100);
    }

    @Test
    void differentDocumentsTendToProduceDifferentScores() {
        int score1 = engine.score("52998224725");
        int score2 = engine.score("11144477735");

        assertThat(score1).isNotEqualTo(score2);
    }
}
