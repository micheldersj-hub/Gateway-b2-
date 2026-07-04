package com.gatewayb2.ledger.messaging;

import com.gatewayb2.common.domain.EntryType;
import com.gatewayb2.common.event.CardTransactionAuthorizedEvent;
import com.gatewayb2.ledger.domain.LedgerAccount;
import com.gatewayb2.ledger.service.LedgerAccountService;
import com.gatewayb2.ledger.service.PostingLineCommand;
import com.gatewayb2.ledger.service.PostingService;
import com.gatewayb2.ledger.service.SettlementAccounts;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

/** Toda transação de cartão autorizada debita a conta do cliente e credita a liquidação da adquirente/bandeira. */
@Component
public class CardTransactionAuthorizedListener {

    private static final Logger log = LoggerFactory.getLogger(CardTransactionAuthorizedListener.class);

    private final LedgerAccountService ledgerAccountService;
    private final PostingService postingService;

    public CardTransactionAuthorizedListener(LedgerAccountService ledgerAccountService, PostingService postingService) {
        this.ledgerAccountService = ledgerAccountService;
        this.postingService = postingService;
    }

    @KafkaListener(topics = CardTransactionAuthorizedEvent.TOPIC, groupId = "ledger-service")
    public void onCardTransactionAuthorized(CardTransactionAuthorizedEvent event) {
        log.info("Evento CardTransactionAuthorizedEvent recebido: transactionId={}, cardId={}", event.transactionId(), event.cardId());

        LedgerAccount customerAccount = ledgerAccountService.getOrCreateCustomerDeposit(event.accountId(), event.currency());
        LedgerAccount settlementAccount = ledgerAccountService.getOrCreateSettlement(
                SettlementAccounts.CARD, "Liquidação de cartões (adquirente/bandeira)", event.currency());

        List<PostingLineCommand> lines = List.of(
                new PostingLineCommand(customerAccount, EntryType.DEBIT, event.amount(), event.currency()),
                new PostingLineCommand(settlementAccount, EntryType.CREDIT, event.amount(), event.currency()));

        String description = "Compra no cartão - " + event.merchantName();
        postingService.post(description, "card-tx:" + event.transactionId(), "CARD", lines);
    }
}
