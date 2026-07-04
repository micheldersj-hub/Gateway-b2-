package com.gatewayb2.payment.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record SendPixRequest(
        @NotNull UUID accountId,
        @NotBlank String pixKey,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @NotBlank String counterpartyName,
        String counterpartyDocument
) {
}
