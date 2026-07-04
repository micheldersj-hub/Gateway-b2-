package com.gatewayb2.card.web.dto;

import com.gatewayb2.card.domain.CardTransaction;
import com.gatewayb2.card.domain.CardTransactionStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CardTransactionResponse(
        UUID id,
        UUID cardId,
        String merchantName,
        BigDecimal amount,
        String currency,
        CardTransactionStatus status,
        String declineReason,
        Instant createdAt
) {
    public static CardTransactionResponse from(CardTransaction tx) {
        return new CardTransactionResponse(tx.getId(), tx.getCardId(), tx.getMerchantName(), tx.getAmount(),
                tx.getCurrency(), tx.getStatus(), tx.getDeclineReason(), tx.getCreatedAt());
    }
}
