package com.gatewayb2.card.repository;

import com.gatewayb2.card.domain.CardTransaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CardTransactionRepository extends JpaRepository<CardTransaction, UUID> {
    Optional<CardTransaction> findByIdempotencyKey(String idempotencyKey);
}
