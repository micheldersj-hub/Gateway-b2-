package com.gatewayb2.ledger.web.dto;

import com.gatewayb2.common.domain.EntryType;
import com.gatewayb2.ledger.domain.JournalEntry;
import com.gatewayb2.ledger.domain.Posting;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record JournalEntryResponse(
        UUID id,
        String description,
        String idempotencyKey,
        String sourceEvent,
        Instant createdAt,
        List<PostingResponse> postings
) {
    public static JournalEntryResponse from(JournalEntry entry) {
        return new JournalEntryResponse(entry.getId(), entry.getDescription(), entry.getIdempotencyKey(),
                entry.getSourceEvent(), entry.getCreatedAt(), entry.getPostings().stream().map(PostingResponse::from).toList());
    }

    public record PostingResponse(UUID id, UUID ledgerAccountId, String ledgerAccountCode, EntryType entryType,
                                   BigDecimal amount, String currency) {
        public static PostingResponse from(Posting p) {
            return new PostingResponse(p.getId(), p.getLedgerAccount().getId(), p.getLedgerAccount().getCode(),
                    p.getEntryType(), p.getAmount(), p.getCurrency());
        }
    }
}
