package com.gatewayb2.gateway.repository;

import com.gatewayb2.gateway.domain.ApiClient;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface ApiClientRepository extends ReactiveCrudRepository<ApiClient, UUID> {
    Mono<ApiClient> findByApiKeyHashAndActiveTrue(String apiKeyHash);
}
