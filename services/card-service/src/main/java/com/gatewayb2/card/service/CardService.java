package com.gatewayb2.card.service;

import com.gatewayb2.card.client.AccountClient;
import com.gatewayb2.card.client.AccountDto;
import com.gatewayb2.card.client.LedgerClient;
import com.gatewayb2.card.domain.Card;
import com.gatewayb2.card.domain.CardTransaction;
import com.gatewayb2.card.messaging.CardEventPublisher;
import com.gatewayb2.card.repository.CardRepository;
import com.gatewayb2.card.repository.CardTransactionRepository;
import com.gatewayb2.common.domain.AccountStatus;
import com.gatewayb2.common.domain.CardStatus;
import com.gatewayb2.common.domain.CardType;
import com.gatewayb2.common.exception.BusinessException;
import com.gatewayb2.common.exception.ResourceNotFoundException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class CardService {

    private final CardRepository cardRepository;
    private final CardTransactionRepository cardTransactionRepository;
    private final AccountClient accountClient;
    private final LedgerClient ledgerClient;
    private final PanGenerator panGenerator;
    private final CardEncryptionService encryptionService;
    private final CardEventPublisher eventPublisher;
    private final BigDecimal defaultDailyLimit;

    public CardService(CardRepository cardRepository, CardTransactionRepository cardTransactionRepository,
                        AccountClient accountClient, LedgerClient ledgerClient, PanGenerator panGenerator,
                        CardEncryptionService encryptionService, CardEventPublisher eventPublisher,
                        @Value("${gatewayb2.card.default-daily-limit}") BigDecimal defaultDailyLimit) {
        this.cardRepository = cardRepository;
        this.cardTransactionRepository = cardTransactionRepository;
        this.accountClient = accountClient;
        this.ledgerClient = ledgerClient;
        this.panGenerator = panGenerator;
        this.encryptionService = encryptionService;
        this.eventPublisher = eventPublisher;
        this.defaultDailyLimit = defaultDailyLimit;
    }

    /** Emite um novo cartão. O PAN e o CVV completos só existem nesta resposta - nunca são retornados novamente. */
    @Transactional
    public IssuedCard issue(UUID accountId, CardType cardType, String holderName) {
        AccountDto account = accountClient.getAccount(accountId);
        if (account.status() != AccountStatus.ACTIVE) {
            throw new BusinessException("ACCOUNT_NOT_ACTIVE", "A conta não está ativa para emissão de cartão", HttpStatus.CONFLICT);
        }

        String pan = panGenerator.generatePan();
        String cvv = panGenerator.generateCvv();
        String masked = panGenerator.mask(pan);
        String encrypted = encryptionService.encrypt(pan);

        Card card = Card.issue(accountId, cardType, holderName, masked, encrypted, defaultDailyLimit);
        cardRepository.save(card);
        return new IssuedCard(card, pan, cvv);
    }

    @Transactional
    public Card activate(UUID cardId) {
        Card card = findById(cardId);
        if (card.getStatus() != CardStatus.PENDING_ACTIVATION) {
            throw new BusinessException("INVALID_CARD_STATUS", "Somente cartões pendentes de ativação podem ser ativados", HttpStatus.CONFLICT);
        }
        card.activate();
        return card;
    }

    @Transactional
    public Card block(UUID cardId) {
        Card card = findById(cardId);
        card.block();
        return card;
    }

    @Transactional
    public Card unblock(UUID cardId) {
        Card card = findById(cardId);
        card.unblock();
        return card;
    }

    @Transactional
    public Card cancel(UUID cardId) {
        Card card = findById(cardId);
        card.cancel();
        return card;
    }

    /** Simula uma requisição de autorização recebida da rede da bandeira/adquirente. */
    @Transactional
    public CardTransaction authorize(UUID cardId, BigDecimal amount, String currency, String merchantName,
                                      String idempotencyKey) {
        return cardTransactionRepository.findByIdempotencyKey(idempotencyKey)
                .orElseGet(() -> processAuthorization(cardId, amount, currency, merchantName, idempotencyKey));
    }

    private CardTransaction processAuthorization(UUID cardId, BigDecimal amount, String currency, String merchantName,
                                                  String idempotencyKey) {
        Card card = findById(cardId);

        if (card.getStatus() != CardStatus.ACTIVE) {
            return decline(card, amount, currency, merchantName, idempotencyKey, "Cartão não está ativo (" + card.getStatus() + ")");
        }
        if (card.availableLimitToday().compareTo(amount) < 0) {
            return decline(card, amount, currency, merchantName, idempotencyKey, "Limite diário do cartão excedido");
        }
        BigDecimal availableBalance = ledgerClient.getAvailableBalance(card.getAccountId());
        if (availableBalance.compareTo(amount) < 0) {
            return decline(card, amount, currency, merchantName, idempotencyKey, "Saldo insuficiente na conta");
        }

        card.registerSpend(amount);
        CardTransaction tx = CardTransaction.authorized(card.getId(), card.getAccountId(), merchantName, amount, currency, idempotencyKey);
        cardTransactionRepository.save(tx);
        eventPublisher.publishAuthorized(tx);
        return tx;
    }

    private CardTransaction decline(Card card, BigDecimal amount, String currency, String merchantName,
                                     String idempotencyKey, String reason) {
        CardTransaction tx = CardTransaction.declined(card.getId(), card.getAccountId(), merchantName, amount, currency,
                idempotencyKey, reason);
        return cardTransactionRepository.save(tx);
    }

    @Transactional(readOnly = true)
    public Card findById(UUID cardId) {
        return cardRepository.findById(cardId)
                .orElseThrow(() -> new ResourceNotFoundException("Cartão", cardId));
    }

    public record IssuedCard(Card card, String pan, String cvv) {
    }
}
