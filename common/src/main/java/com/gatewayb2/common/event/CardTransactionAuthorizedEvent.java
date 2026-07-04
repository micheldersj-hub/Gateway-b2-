package com.gatewayb2.common.event;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/**
 * Publicado pelo card-service quando uma transação de cartão é autorizada.
 * O ledger-service consome este evento para gerar o lançamento contábil de débito na conta
 * e crédito na conta de liquidação da bandeira/adquirente.
 */
public record CardTransactionAuthorizedEvent(
        UUID transactionId,
        UUID cardId,
        UUID accountId,
        BigDecimal amount,
        String currency,
        String merchantName,
        String idempotencyKey,
        Instant occurredAt
) {
    public static final String TOPIC = "card.transaction-authorized";
}
