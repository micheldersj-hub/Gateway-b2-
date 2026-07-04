package com.gatewayb2.ledger.web.dto;

import com.gatewayb2.common.domain.EntryType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/** Lançamento manual, usado por operações internas (ex: ajustes, aporte inicial de capital). */
public record CreateJournalEntryRequest(
        @NotBlank String description,
        @NotBlank String idempotencyKey,
        @NotEmpty @Valid List<Line> lines
) {
    public record Line(
            @NotNull UUID ledgerAccountId,
            @NotNull EntryType entryType,
            @NotNull @DecimalMin(value = "0.01") BigDecimal amount,
            @NotBlank String currency
    ) {
    }
}
