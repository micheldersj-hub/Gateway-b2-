package com.gatewayb2.payment.web.dto;

import com.gatewayb2.common.domain.PaymentDirection;
import com.gatewayb2.common.domain.PaymentMethod;
import com.gatewayb2.common.domain.PaymentStatus;
import com.gatewayb2.payment.domain.Payment;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        UUID accountId,
        PaymentMethod method,
        PaymentDirection direction,
        BigDecimal amount,
        String currency,
        String counterpartyName,
        String counterpartyDocument,
        PaymentStatus status,
        String failureReason,
        Instant createdAt,
        Instant updatedAt
) {
    public static PaymentResponse from(Payment p) {
        return new PaymentResponse(p.getId(), p.getAccountId(), p.getMethod(), p.getDirection(), p.getAmount(),
                p.getCurrency(), p.getCounterpartyName(), p.getCounterpartyDocument(), p.getStatus(),
                p.getFailureReason(), p.getCreatedAt(), p.getUpdatedAt());
    }
}
