package com.gatewayb2.account.repository;

import com.gatewayb2.account.domain.Account;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface AccountRepository extends JpaRepository<Account, UUID> {
    Optional<Account> findByCustomerId(UUID customerId);
    boolean existsByCustomerId(UUID customerId);
    boolean existsByAccountNumber(String accountNumber);

    /**
     * Busca com fetch antecipado de pixKeys: evita LazyInitializationException quando o
     * resultado é serializado para o DTO de resposta fora da transação (open-in-view=false).
     */
    @Query("select a from Account a left join fetch a.pixKeys where a.id = :id")
    Optional<Account> findByIdFetchingPixKeys(@Param("id") UUID id);

    @Query("select a from Account a left join fetch a.pixKeys where a.customerId = :customerId")
    Optional<Account> findByCustomerIdFetchingPixKeys(@Param("customerId") UUID customerId);
}
