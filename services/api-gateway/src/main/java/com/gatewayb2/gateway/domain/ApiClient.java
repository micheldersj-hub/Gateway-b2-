package com.gatewayb2.gateway.domain;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.Instant;
import java.util.UUID;

/** Cliente parceiro autorizado a consumir a API (modelo de API Key, como Stripe/outras BaaS). */
@Table("api_clients")
public record ApiClient(
        @Id UUID id,
        @Column("client_name") String clientName,
        @Column("api_key_hash") String apiKeyHash,
        boolean active,
        @Column("requests_per_minute") int requestsPerMinute,
        @Column("created_at") Instant createdAt
) {
}
