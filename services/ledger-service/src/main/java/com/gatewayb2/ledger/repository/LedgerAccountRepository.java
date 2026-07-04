package com.gatewayb2.ledger.repository;

import com.gatewayb2.ledger.domain.LedgerAccount;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LedgerAccountRepository extends JpaRepository<LedgerAccount, UUID> {
    Optional<LedgerAccount> findByCode(String code);
    Optional<LedgerAccount> findByExternalAccountId(UUID externalAccountId);
}
