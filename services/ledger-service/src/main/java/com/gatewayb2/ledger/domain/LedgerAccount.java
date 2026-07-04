package com.gatewayb2.ledger.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.UUID;

/**
 * Conta do plano de contas interno do ledger. Cada conta digital do account-service possui
 * uma LedgerAccount do tipo CUSTOMER_DEPOSIT correspondente; cada trilho de pagamento (PIX,
 * TED, Boleto, Cartão) possui uma conta de liquidação (SETTLEMENT) única e compartilhada.
 */
@Entity
@Table(name = "ledger_accounts", uniqueConstraints = {
        @UniqueConstraint(name = "uk_ledger_accounts_code", columnNames = "code"),
        @UniqueConstraint(name = "uk_ledger_accounts_external", columnNames = "external_account_id")
})
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class LedgerAccount {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private LedgerAccountKind kind;

    /** Identificador único e estável, ex: "PIX_SETTLEMENT" ou "CUSTOMER:<externalAccountId>". */
    @Column(nullable = false, length = 100)
    private String code;

    @Column(nullable = false, length = 180)
    private String name;

    /** Para contas CUSTOMER_DEPOSIT: id da conta no account-service. Nulo para contas internas. */
    @Column(name = "external_account_id")
    private UUID externalAccountId;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false)
    private Instant createdAt;

    public static LedgerAccount customerDeposit(UUID externalAccountId, String currency) {
        LedgerAccount account = new LedgerAccount();
        account.kind = LedgerAccountKind.CUSTOMER_DEPOSIT;
        account.code = "CUSTOMER:" + externalAccountId;
        account.name = "Depósito - conta " + externalAccountId;
        account.externalAccountId = externalAccountId;
        account.currency = currency;
        account.createdAt = Instant.now();
        return account;
    }

    public static LedgerAccount settlement(String code, String name, String currency) {
        LedgerAccount account = new LedgerAccount();
        account.kind = LedgerAccountKind.SETTLEMENT;
        account.code = code;
        account.name = name;
        account.currency = currency;
        account.createdAt = Instant.now();
        return account;
    }
}
