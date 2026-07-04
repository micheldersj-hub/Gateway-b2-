package com.gatewayb2.card.service;

import com.gatewayb2.card.client.AccountClient;
import com.gatewayb2.card.client.AccountDto;
import com.gatewayb2.card.client.LedgerClient;
import com.gatewayb2.card.domain.Card;
import com.gatewayb2.card.domain.CardTransaction;
import com.gatewayb2.card.domain.CardTransactionStatus;
import com.gatewayb2.card.messaging.CardEventPublisher;
import com.gatewayb2.card.repository.CardRepository;
import com.gatewayb2.card.repository.CardTransactionRepository;
import com.gatewayb2.common.domain.AccountStatus;
import com.gatewayb2.common.domain.CardType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class CardServiceTest {

    private CardRepository cardRepository;
    private CardTransactionRepository cardTransactionRepository;
    private AccountClient accountClient;
    private LedgerClient ledgerClient;
    private CardEventPublisher eventPublisher;
    private CardService cardService;

    private final UUID accountId = UUID.randomUUID();
    private Card activeCard;

    @BeforeEach
    void setUp() {
        cardRepository = mock(CardRepository.class);
        cardTransactionRepository = mock(CardTransactionRepository.class);
        accountClient = mock(AccountClient.class);
        ledgerClient = mock(LedgerClient.class);
        eventPublisher = mock(CardEventPublisher.class);
        cardService = new CardService(cardRepository, cardTransactionRepository, accountClient, ledgerClient,
                new PanGenerator(), new CardEncryptionService("Zm6G4x6M19QuCKI080PtMDy3RVUYQIhKaId83Eu7lqQ="),
                eventPublisher, new BigDecimal("5000.00"));

        activeCard = Card.issue(accountId, CardType.VIRTUAL, "Fulano", "**** **** **** 1234", "encrypted", new BigDecimal("5000.00"));
        when(cardRepository.findById(any())).thenReturn(Optional.of(activeCard));
        when(cardTransactionRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));
    }

    @Test
    void authorizesTransactionWithinLimitAndSufficientBalance() {
        when(cardTransactionRepository.findByIdempotencyKey("tx-1")).thenReturn(Optional.empty());
        when(ledgerClient.getAvailableBalance(accountId)).thenReturn(new BigDecimal("1000.00"));

        CardTransaction tx = cardService.authorize(activeCard.getId(), new BigDecimal("200.00"), "BRL", "Loja X", "tx-1");

        assertThat(tx.getStatus()).isEqualTo(CardTransactionStatus.AUTHORIZED);
        verify(eventPublisher).publishAuthorized(tx);
    }

    @Test
    void declinesTransactionExceedingDailyLimit() {
        when(cardTransactionRepository.findByIdempotencyKey("tx-2")).thenReturn(Optional.empty());

        CardTransaction tx = cardService.authorize(activeCard.getId(), new BigDecimal("6000.00"), "BRL", "Loja Y", "tx-2");

        assertThat(tx.getStatus()).isEqualTo(CardTransactionStatus.DECLINED);
        assertThat(tx.getDeclineReason()).contains("Limite diário");
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void declinesTransactionWhenAccountBalanceIsInsufficient() {
        when(cardTransactionRepository.findByIdempotencyKey("tx-3")).thenReturn(Optional.empty());
        when(ledgerClient.getAvailableBalance(accountId)).thenReturn(new BigDecimal("10.00"));

        CardTransaction tx = cardService.authorize(activeCard.getId(), new BigDecimal("200.00"), "BRL", "Loja Z", "tx-3");

        assertThat(tx.getStatus()).isEqualTo(CardTransactionStatus.DECLINED);
        assertThat(tx.getDeclineReason()).contains("Saldo insuficiente");
        verifyNoInteractions(eventPublisher);
    }

    @Test
    void issuesVirtualCardActiveImmediately() {
        when(accountClient.getAccount(accountId)).thenReturn(new AccountDto(accountId, UUID.randomUUID(), "52998224725", "Fulano", AccountStatus.ACTIVE));
        when(cardRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        CardService.IssuedCard issued = cardService.issue(accountId, CardType.VIRTUAL, "Fulano");

        assertThat(issued.card().getStatus().name()).isEqualTo("ACTIVE");
        assertThat(issued.pan()).hasSize(16);
        assertThat(issued.cvv()).hasSize(3);
    }
}
