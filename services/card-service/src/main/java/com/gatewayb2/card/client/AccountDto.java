package com.gatewayb2.card.client;

import com.gatewayb2.common.domain.AccountStatus;

import java.util.UUID;

public record AccountDto(UUID id, UUID customerId, String document, String holderName, AccountStatus status) {
}
