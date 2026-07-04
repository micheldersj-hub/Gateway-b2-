package com.gatewayb2.payment.domain;

import com.gatewayb2.common.domain.PaymentDirection;
import com.gatewayb2.common.domain.PaymentMethod;
import com.gatewayb2.common.domain.PaymentStatus;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payments", uniqueConstraints = @UniqueConstraint(name = "uk_payments_idempotency", columnNames = "idempotency_key"))
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentMethod method;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentDirection direction;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(name = "counterparty_name", nullable = false, length = 180)
    private String counterpartyName;

    @Column(name = "counterparty_document", length = 14)
    private String counterpartyDocument;

    /** Chave PIX, conta/banco de destino (TED) ou linha digitável (Boleto), conforme o método. */
    @Column(name = "counterparty_identifier", length = 180)
    private String counterpartyIdentifier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentStatus status;

    @Column(name = "idempotency_key", nullable = false, length = 180)
    private String idempotencyKey;

    @Column(name = "failure_reason", length = 255)
    private String failureReason;

    @Column(nullable = false)
    private Instant createdAt;

    @Column(nullable = false)
    private Instant updatedAt;

    @Version
    private Long version;

    public static Payment create(UUID accountId, PaymentMethod method, PaymentDirection direction, BigDecimal amount,
                                  String currency, String counterpartyName, String counterpartyDocument,
                                  String counterpartyIdentifier, String idempotencyKey) {
        Payment payment = new Payment();
        payment.accountId = accountId;
        payment.method = method;
        payment.direction = direction;
        payment.amount = amount;
        payment.currency = currency;
        payment.counterpartyName = counterpartyName;
        payment.counterpartyDocument = counterpartyDocument;
        payment.counterpartyIdentifier = counterpartyIdentifier;
        payment.idempotencyKey = idempotencyKey;
        payment.status = PaymentStatus.PENDING;
        Instant now = Instant.now();
        payment.createdAt = now;
        payment.updatedAt = now;
        return payment;
    }

    public void markProcessing() {
        this.status = PaymentStatus.PROCESSING;
        this.updatedAt = Instant.now();
    }

    public void markCompleted() {
        this.status = PaymentStatus.COMPLETED;
        this.updatedAt = Instant.now();
    }

    public void markFailed(String reason) {
        this.status = PaymentStatus.FAILED;
        this.failureReason = reason;
        this.updatedAt = Instant.now();
    }
}
