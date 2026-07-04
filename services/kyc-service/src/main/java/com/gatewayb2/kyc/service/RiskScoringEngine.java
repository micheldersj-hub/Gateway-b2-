package com.gatewayb2.kyc.service;

import org.springframework.stereotype.Component;

/**
 * Motor de score de risco simplificado (stub de um provedor antifraude real, ex: Serasa,
 * Unico ou SCR do Bacen). Gera um score determinístico de 0 a 100 a partir do documento,
 * suficiente para exercitar o fluxo de decisão automática/manual do onboarding.
 */
@Component
public class RiskScoringEngine {

    public static final int AUTO_APPROVE_THRESHOLD = 30;
    public static final int AUTO_REJECT_THRESHOLD = 70;

    public int score(String document) {
        int hash = Math.abs(document.hashCode());
        return hash % 101;
    }
}
