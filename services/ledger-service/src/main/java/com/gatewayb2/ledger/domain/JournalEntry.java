package com.gatewayb2.ledger.domain;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Cabeçalho de um lançamento contábil (transação). Imutável após criação: nunca é editado ou
 * removido — estornos são feitos criando um novo JournalEntry inverso.
 */
@Entity
@Table(name = "journal_entries", uniqueConstraints = @UniqueConstraint(name = "uk_journal_entries_idempotency", columnNames = "idempotency_key"))
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class JournalEntry {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String description;

    /** Chave de deduplicação: ex. "payment:<paymentId>" ou "card-tx:<transactionId>". */
    @Column(name = "idempotency_key", nullable = false, length = 180)
    private String idempotencyKey;

    @Column(name = "source_event", length = 60)
    private String sourceEvent;

    @OneToMany(mappedBy = "journalEntry", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Posting> postings = new ArrayList<>();

    @Column(nullable = false)
    private Instant createdAt;

    public static JournalEntry create(String description, String idempotencyKey, String sourceEvent) {
        JournalEntry entry = new JournalEntry();
        entry.description = description;
        entry.idempotencyKey = idempotencyKey;
        entry.sourceEvent = sourceEvent;
        entry.createdAt = Instant.now();
        return entry;
    }

    public void addPosting(Posting posting) {
        posting.setJournalEntry(this);
        this.postings.add(posting);
    }
}
