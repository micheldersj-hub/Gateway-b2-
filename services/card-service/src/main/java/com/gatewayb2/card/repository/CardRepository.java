package com.gatewayb2.card.repository;

import com.gatewayb2.card.domain.Card;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CardRepository extends JpaRepository<Card, UUID> {
    List<Card> findByAccountId(UUID accountId);
}
