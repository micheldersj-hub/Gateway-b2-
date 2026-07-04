package com.gatewayb2.common.event;

import com.gatewayb2.common.domain.PaymentDirection;
import com.gatewayb2.common.domain.PaymentMethod;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Publicado pelo payment-service quando um pagamento (PIX/TED/Boleto) é liquidado com sucesso.
 * O ledger-service consome este evento para gerar os lançamentos contábeis correspondentes.
 */
public record PaymentCompletedEvent(
        UUID paymentId,
        UUID accountId,
        PaymentMethod method,
        PaymentDirection direction,
        BigDecimal amount,
        String currency,
        String counterpartyName,
        String counterpartyDocument,
        String idempotencyKey,
        Instant occurredAt
) {
    public static final String TOPIC = "payment.completed";
}
