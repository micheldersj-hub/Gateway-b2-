package com.gatewayb2.ledger.repository;

import com.gatewayb2.ledger.domain.Posting;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface PostingRepository extends JpaRepository<Posting, UUID> {

    List<Posting> findByLedgerAccountIdOrderByCreatedAtDesc(UUID ledgerAccountId);

    @Query("""
            select coalesce(sum(case when p.entryType = 'CREDIT' then p.amount else -p.amount end), 0)
            from Posting p
            where p.ledgerAccount.id = :ledgerAccountId
            """)
    BigDecimal computeBalance(@Param("ledgerAccountId") UUID ledgerAccountId);
}
