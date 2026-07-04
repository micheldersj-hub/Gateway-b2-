package com.gatewayb2.ledger.web.dto;

import java.math.BigDecimal;
import java.util.UUID;

public record BalanceResponse(UUID ledgerAccountId, UUID externalAccountId, String currency, BigDecimal balance) {
}
