package com.gatewayb2.kyc.web.dto;

import jakarta.validation.constraints.NotNull;

public record ReviewDecisionRequest(
        @NotNull Decision decision,
        String reason
) {
    public enum Decision {
        APPROVE, REJECT
    }
}
