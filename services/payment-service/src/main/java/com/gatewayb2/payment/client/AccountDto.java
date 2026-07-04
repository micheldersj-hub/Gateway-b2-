package com.gatewayb2.payment.client;

import com.gatewayb2.common.domain.AccountStatus;

import java.util.UUID;

/** Projeção mínima da resposta do account-service, suficiente para o payment-service operar. */
public record AccountDto(UUID id, UUID customerId, String document, String holderName, AccountStatus status) {
}
