package com.gatewayb2.payment.web.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/** Simula o webhook recebido do SPI/Bacen quando um PIX é recebido por uma chave da nossa plataforma. */
public record ReceivePixRequest(
        @NotBlank String destinationPixKey,
        @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
        @NotBlank String payerName,
        String payerDocument
) {
}
