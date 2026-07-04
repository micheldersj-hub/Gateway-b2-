package com.gatewayb2.payment.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

public record PayBoletoRequest(
        @NotNull UUID accountId,
        @NotBlank String barcode,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @NotBlank String recipientName
) {
}
