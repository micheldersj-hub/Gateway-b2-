package com.gatewayb2.card.web.dto;

import com.gatewayb2.card.domain.Card;
import com.gatewayb2.common.domain.CardStatus;
import com.gatewayb2.common.domain.CardType;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record CardResponse(
        UUID id,
        UUID accountId,
        CardType cardType,
        String holderName,
        String panMasked,
        int expiryMonth,
        int expiryYear,
        CardStatus status,
        BigDecimal dailyLimit,
        BigDecimal availableLimitToday,
        Instant createdAt
) {
    public static CardResponse from(Card c) {
        return new CardResponse(c.getId(), c.getAccountId(), c.getCardType(), c.getHolderName(), c.getPanMasked(),
                c.getExpiryMonth(), c.getExpiryYear(), c.getStatus(), c.getDailyLimit(), c.availableLimitToday(), c.getCreatedAt());
    }
}
