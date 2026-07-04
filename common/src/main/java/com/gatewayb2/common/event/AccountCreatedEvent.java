package com.gatewayb2.common.event;

import java.time.Instant;
import java.util.UUID;

/** Publicado pelo account-service quando uma nova conta digital é aberta e ativada. */
public record AccountCreatedEvent(
        UUID accountId,
        UUID customerId,
        String accountNumber,
        String branch,
        Instant occurredAt
) {
    public static final String TOPIC = "account.created";
}
