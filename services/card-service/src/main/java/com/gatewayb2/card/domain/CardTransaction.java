package com.gatewayb2.card.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "card_transactions", uniqueConstraints = @UniqueConstraint(name = "uk_card_tx_idempotency", columnNames = "idempotency_key"))
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CardTransaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "card_id", nullable = false)
    private UUID cardId;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "merchant_name", nullable = false, length = 180)
    private String merchantName;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CardTransactionStatus status;

    @Column(name = "decline_reason", length = 255)
    private String declineReason;

    @Column(name = "idempotency_key", nullable = false, length = 180)
    private String idempotencyKey;

    @Column(nullable = false)
    private Instant createdAt;

    public static CardTransaction authorized(UUID cardId, UUID accountId, String merchantName, BigDecimal amount,
                                              String currency, String idempotencyKey) {
        CardTransaction tx = base(cardId, accountId, merchantName, amount, currency, idempotencyKey);
        tx.status = CardTransactionStatus.AUTHORIZED;
        return tx;
    }

    public static CardTransaction declined(UUID cardId, UUID accountId, String merchantName, BigDecimal amount,
                                            String currency, String idempotencyKey, String reason) {
        CardTransaction tx = base(cardId, accountId, merchantName, amount, currency, idempotencyKey);
        tx.status = CardTransactionStatus.DECLINED;
        tx.declineReason = reason;
        return tx;
    }

    private static CardTransaction base(UUID cardId, UUID accountId, String merchantName, BigDecimal amount,
                                         String currency, String idempotencyKey) {
        CardTransaction tx = new CardTransaction();
        tx.cardId = cardId;
        tx.accountId = accountId;
        tx.merchantName = merchantName;
        tx.amount = amount;
        tx.currency = currency;
        tx.idempotencyKey = idempotencyKey;
        tx.createdAt = Instant.now();
        return tx;
    }
}
