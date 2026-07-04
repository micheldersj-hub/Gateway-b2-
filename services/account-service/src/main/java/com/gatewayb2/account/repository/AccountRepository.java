package com.gatewayb2.account.repository;

import com.gatewayb2.account.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    Optional<Account> findByCustomerId(UUID customerId);
    boolean existsByCustomerId(UUID customerId);
    boolean existsByAccountNumber(String accountNumber);
}
