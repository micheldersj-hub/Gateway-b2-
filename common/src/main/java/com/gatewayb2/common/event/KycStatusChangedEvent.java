package com.gatewayb2.common.event;

import com.gatewayb2.common.domain.KycStatus;
import com.gatewayb2.common.domain.PersonType;

import java.time.Instant;
import java.util.UUID;

/**
 * Publicado pelo kyc-service sempre que o status de verificação de um cliente muda.
 * O account-service consome este evento para habilitar (ou bloquear) a abertura de contas.
 */
public record KycStatusChangedEvent(
        UUID customerId,
        PersonType personType,
        String document,
        KycStatus previousStatus,
        KycStatus newStatus,
        Instant occurredAt
) {
    public static final String TOPIC = "kyc.status-changed";
}
