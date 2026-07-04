package com.gatewayb2.ledger.service;

import com.gatewayb2.common.domain.EntryType;
import com.gatewayb2.common.exception.BusinessException;
import com.gatewayb2.ledger.domain.JournalEntry;
import com.gatewayb2.ledger.domain.LedgerAccount;
import com.gatewayb2.ledger.repository.JournalEntryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PostingServiceTest {

    private JournalEntryRepository repository;
    private PostingService postingService;

    @BeforeEach
    void setUp() {
        repository = mock(JournalEntryRepository.class);
        postingService = new PostingService(repository);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Test
    void postsBalancedEntrySuccessfully() {
        when(repository.findByIdempotencyKey("key-1")).thenReturn(Optional.empty());
        LedgerAccount debitAccount = LedgerAccount.customerDeposit(UUID.randomUUID(), "BRL");
        LedgerAccount creditAccount = LedgerAccount.settlement("PIX_SETTLEMENT", "PIX", "BRL");

        JournalEntry entry = postingService.post("teste", "key-1", "TEST", List.of(
                new PostingLineCommand(debitAccount, EntryType.DEBIT, new BigDecimal("100.00"), "BRL"),
                new PostingLineCommand(creditAccount, EntryType.CREDIT, new BigDecimal("100.00"), "BRL")));

        assertThat(entry.getPostings()).hasSize(2);
        ArgumentCaptor<JournalEntry> captor = ArgumentCaptor.forClass(JournalEntry.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getIdempotencyKey()).isEqualTo("key-1");
    }

    @Test
    void rejectsUnbalancedEntry() {
        when(repository.findByIdempotencyKey("key-2")).thenReturn(Optional.empty());
        LedgerAccount debitAccount = LedgerAccount.customerDeposit(UUID.randomUUID(), "BRL");
        LedgerAccount creditAccount = LedgerAccount.settlement("PIX_SETTLEMENT", "PIX", "BRL");

        assertThatThrownBy(() -> postingService.post("teste", "key-2", "TEST", List.of(
                new PostingLineCommand(debitAccount, EntryType.DEBIT, new BigDecimal("100.00"), "BRL"),
                new PostingLineCommand(creditAccount, EntryType.CREDIT, new BigDecimal("99.00"), "BRL"))))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("não balanceado");

        verify(repository, never()).save(any());
    }

    @Test
    void isIdempotentForSameKey() {
        JournalEntry existing = JournalEntry.create("já existente", "key-3", "TEST");
        when(repository.findByIdempotencyKey("key-3")).thenReturn(Optional.of(existing));
        LedgerAccount debitAccount = LedgerAccount.customerDeposit(UUID.randomUUID(), "BRL");
        LedgerAccount creditAccount = LedgerAccount.settlement("PIX_SETTLEMENT", "PIX", "BRL");

        JournalEntry result = postingService.post("teste", "key-3", "TEST", List.of(
                new PostingLineCommand(debitAccount, EntryType.DEBIT, new BigDecimal("50.00"), "BRL"),
                new PostingLineCommand(creditAccount, EntryType.CREDIT, new BigDecimal("50.00"), "BRL")));

        assertThat(result).isSameAs(existing);
        verify(repository, never()).save(any());
    }
}
