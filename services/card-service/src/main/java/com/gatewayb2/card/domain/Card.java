package com.gatewayb2.card.domain;

import com.gatewayb2.common.domain.CardStatus;
import com.gatewayb2.common.domain.CardType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "cards")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Card {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CardType cardType;

    @Column(name = "holder_name", nullable = false, length = 180)
    private String holderName;

    @Column(name = "pan_masked", nullable = false, length = 20)
    private String panMasked;

    /** PAN completo criptografado (AES-256-GCM). Nunca exposto após a criação do cartão. */
    @Column(name = "pan_encrypted", nullable = false, length = 255)
    private String panEncrypted;

    @Column(name = "expiry_month", nullable = false)
    private int expiryMonth;

    @Column(name = "expiry_year", nullable = false)
    private int expiryYear;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private CardStatus status;

    @Column(name = "daily_limit", nullable = false, precision = 19, scale = 2)
    private BigDecimal dailyLimit;

    @Column(name = "spent_today", nullable = false, precision = 19, scale = 2)
    private BigDecimal spentToday;

    @Column(name = "last_spend_date")
    private LocalDate lastSpendDate;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    public static Card issue(UUID accountId, CardType cardType, String holderName, String panMasked,
                              String panEncrypted, BigDecimal dailyLimit) {
        Card card = new Card();
        card.accountId = accountId;
        card.cardType = cardType;
        card.holderName = holderName;
        card.panMasked = panMasked;
        card.panEncrypted = panEncrypted;
        LocalDate expiry = LocalDate.now().plusYears(5);
        card.expiryMonth = expiry.getMonthValue();
        card.expiryYear = expiry.getYear();
        card.dailyLimit = dailyLimit;
        card.spentToday = BigDecimal.ZERO;
        // Cartões virtuais ficam prontos para uso imediato; físicos aguardam ativação pelo portador.
        card.status = cardType == CardType.VIRTUAL ? CardStatus.ACTIVE : CardStatus.PENDING_ACTIVATION;
        Instant now = Instant.now();
        card.createdAt = now;
        card.updatedAt = now;
        return card;
    }

    public void activate() {
        this.status = CardStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    public void block() {
        this.status = CardStatus.BLOCKED;
        this.updatedAt = Instant.now();
    }

    public void unblock() {
        this.status = CardStatus.ACTIVE;
        this.updatedAt = Instant.now();
    }

    public void cancel() {
        this.status = CardStatus.CANCELED;
        this.updatedAt = Instant.now();
    }

    public BigDecimal availableLimitToday() {
        resetIfNewDay();
        return dailyLimit.subtract(spentToday);
    }

    public void registerSpend(BigDecimal amount) {
        resetIfNewDay();
        this.spentToday = this.spentToday.add(amount);
        this.updatedAt = Instant.now();
    }

    private void resetIfNewDay() {
        LocalDate today = LocalDate.now();
        if (!today.equals(lastSpendDate)) {
            this.spentToday = BigDecimal.ZERO;
            this.lastSpendDate = today;
        }
    }
}
