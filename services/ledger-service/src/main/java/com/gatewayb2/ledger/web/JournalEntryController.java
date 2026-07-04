package com.gatewayb2.ledger.web;

import com.gatewayb2.ledger.domain.JournalEntry;
import com.gatewayb2.ledger.service.LedgerAccountService;
import com.gatewayb2.ledger.service.PostingLineCommand;
import com.gatewayb2.ledger.service.PostingService;
import com.gatewayb2.ledger.web.dto.CreateJournalEntryRequest;
import com.gatewayb2.ledger.web.dto.JournalEntryResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/journal-entries")
@Tag(name = "Ledger", description = "Lançamentos contábeis de partidas dobradas")
public class JournalEntryController {

    private final PostingService postingService;
    private final LedgerAccountService ledgerAccountService;

    public JournalEntryController(PostingService postingService, LedgerAccountService ledgerAccountService) {
        this.postingService = postingService;
        this.ledgerAccountService = ledgerAccountService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Cria um lançamento contábil manual (uso interno/administrativo)")
    public JournalEntryResponse create(@Valid @RequestBody CreateJournalEntryRequest request) {
        List<PostingLineCommand> lines = request.lines().stream()
                .map(line -> new PostingLineCommand(
                        ledgerAccountService.findById(line.ledgerAccountId()), line.entryType(), line.amount(), line.currency()))
                .toList();
        JournalEntry entry = postingService.post(request.description(), request.idempotencyKey(), "MANUAL", lines);
        return JournalEntryResponse.from(entry);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Consulta um lançamento contábil pelo identificador")
    public JournalEntryResponse getById(@PathVariable UUID id) {
        return JournalEntryResponse.from(postingService.findById(id));
    }
}
