package com.gatewayb2.common.event;

import java.time.Instant;
import java.util.UUID;

/** Publicado pelo payment-service quando um pagamento não pôde ser concluído. */
public record PaymentFailedEvent(
        UUID paymentId,
        UUID accountId,
        String reason,
        Instant occurredAt
) {
    public static final String TOPIC = "payment.failed";
}
