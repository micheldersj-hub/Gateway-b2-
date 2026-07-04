package com.gatewayb2.ledger.service;

import com.gatewayb2.common.domain.EntryType;
import com.gatewayb2.common.exception.BusinessException;
import com.gatewayb2.common.exception.ResourceNotFoundException;
import com.gatewayb2.ledger.domain.JournalEntry;
import com.gatewayb2.ledger.domain.Posting;
import com.gatewayb2.ledger.repository.JournalEntryRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Serviço central do ledger: garante que todo lançamento seja balanceado (soma dos débitos =
 * soma dos créditos, por moeda) e idempotente (mesma idempotencyKey nunca gera dois lançamentos).
 */
@Service
public class PostingService {

    private static final Logger log = LoggerFactory.getLogger(PostingService.class);

    private final JournalEntryRepository journalEntryRepository;

    public PostingService(JournalEntryRepository journalEntryRepository) {
        this.journalEntryRepository = journalEntryRepository;
    }

    @Transactional
    public JournalEntry post(String description, String idempotencyKey, String sourceEvent, List<PostingLineCommand> lines) {
        return journalEntryRepository.findByIdempotencyKeyFetchingPostings(idempotencyKey)
                .map(existing -> {
                    log.info("Lançamento idempotente já existente para chave '{}', ignorando reprocessamento", idempotencyKey);
                    return existing;
                })
                .orElseGet(() -> createAndPersist(description, idempotencyKey, sourceEvent, lines));
    }

    private JournalEntry createAndPersist(String description, String idempotencyKey, String sourceEvent,
                                           List<PostingLineCommand> lines) {
        if (lines.size() < 2) {
            throw new BusinessException("INVALID_JOURNAL_ENTRY", "Um lançamento precisa de ao menos duas linhas", HttpStatus.BAD_REQUEST);
        }
        validateBalanced(lines);

        JournalEntry journalEntry = JournalEntry.create(description, idempotencyKey, sourceEvent);
        for (PostingLineCommand line : lines) {
            journalEntry.addPosting(Posting.of(line.ledgerAccount(), line.entryType(), line.amount(), line.currency()));
        }
        return journalEntryRepository.save(journalEntry);
    }

    private void validateBalanced(List<PostingLineCommand> lines) {
        Map<String, List<PostingLineCommand>> byCurrency = lines.stream()
                .collect(Collectors.groupingBy(PostingLineCommand::currency));

        for (Map.Entry<String, List<PostingLineCommand>> entry : byCurrency.entrySet()) {
            BigDecimal debits = sumBy(entry.getValue(), EntryType.DEBIT);
            BigDecimal credits = sumBy(entry.getValue(), EntryType.CREDIT);
            if (debits.compareTo(credits) != 0) {
                throw new BusinessException("UNBALANCED_JOURNAL_ENTRY",
                        "Lançamento não balanceado em " + entry.getKey() + ": débitos=" + debits + ", créditos=" + credits,
                        HttpStatus.BAD_REQUEST);
            }
        }
    }

    private BigDecimal sumBy(List<PostingLineCommand> lines, EntryType type) {
        return lines.stream()
                .filter(l -> l.entryType() == type)
                .map(PostingLineCommand::amount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Transactional(readOnly = true)
    public JournalEntry findById(UUID journalEntryId) {
        return journalEntryRepository.findByIdFetchingPostings(journalEntryId)
                .orElseThrow(() -> new ResourceNotFoundException("Lançamento contábil", journalEntryId));
    }
}
