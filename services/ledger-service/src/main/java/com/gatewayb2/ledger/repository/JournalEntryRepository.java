package com.gatewayb2.ledger.repository;

import com.gatewayb2.ledger.domain.JournalEntry;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface JournalEntryRepository extends JpaRepository<JournalEntry, UUID> {
    Optional<JournalEntry> findByIdempotencyKey(String idempotencyKey);
    boolean existsByIdempotencyKey(String idempotencyKey);

    /**
     * Busca com fetch antecipado de postings: evita LazyInitializationException quando o
     * resultado é serializado para o DTO de resposta fora da transação (open-in-view=false).
     */
    @Query("select je from JournalEntry je left join fetch je.postings p left join fetch p.ledgerAccount where je.id = :id")
    Optional<JournalEntry> findByIdFetchingPostings(@Param("id") UUID id);

    @Query("select je from JournalEntry je left join fetch je.postings p left join fetch p.ledgerAccount where je.idempotencyKey = :idempotencyKey")
    Optional<JournalEntry> findByIdempotencyKeyFetchingPostings(@Param("idempotencyKey") String idempotencyKey);
}
