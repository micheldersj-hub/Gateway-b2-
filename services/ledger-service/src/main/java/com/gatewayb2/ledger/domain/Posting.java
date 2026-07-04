package com.gatewayb2.ledger.domain;

import com.gatewayb2.common.domain.EntryType;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

/** Linha (débito ou crédito) de um lançamento contábil. Imutável. */
@Entity
@Table(name = "postings")
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Posting {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "journal_entry_id", nullable = false)
    private JournalEntry journalEntry;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ledger_account_id", nullable = false)
    private LedgerAccount ledgerAccount;

    @Enumerated(EnumType.STRING)
    @Column(name = "entry_type", nullable = false, length = 10)
    private EntryType entryType;

    @Column(nullable = false, precision = 19, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false, length = 3)
    private String currency;

    @Column(nullable = false)
    private Instant createdAt;

    public static Posting of(LedgerAccount account, EntryType type, BigDecimal amount, String currency) {
        if (amount.signum() <= 0) {
            throw new IllegalArgumentException("O valor de um lançamento deve ser positivo");
        }
        Posting posting = new Posting();
        posting.ledgerAccount = account;
        posting.entryType = type;
        posting.amount = amount;
        posting.currency = currency;
        posting.createdAt = Instant.now();
        return posting;
    }
}
