package com.gatewayb2.payment.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record SendTedRequest(
        @NotNull UUID accountId,
        @NotBlank String destinationBankCode,
        @NotBlank String destinationBranch,
        @NotBlank String destinationAccountNumber,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @NotBlank String counterpartyName,
        @NotBlank String counterpartyDocument
) {
    public String destinationDescription() {
        return destinationBankCode + "/" + destinationBranch + "/" + destinationAccountNumber;
    }
}
