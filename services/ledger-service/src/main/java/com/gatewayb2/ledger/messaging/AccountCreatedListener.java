package com.gatewayb2.ledger.messaging;

import com.gatewayb2.common.event.AccountCreatedEvent;
import com.gatewayb2.ledger.service.LedgerAccountService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AccountCreatedListener {

    private static final Logger log = LoggerFactory.getLogger(AccountCreatedListener.class);

    private final LedgerAccountService ledgerAccountService;

    public AccountCreatedListener(LedgerAccountService ledgerAccountService) {
        this.ledgerAccountService = ledgerAccountService;
    }

    @KafkaListener(topics = AccountCreatedEvent.TOPIC, groupId = "ledger-service")
    public void onAccountCreated(AccountCreatedEvent event) {
        log.info("Evento AccountCreatedEvent recebido: accountId={}", event.accountId());
        ledgerAccountService.getOrCreateCustomerDeposit(event.accountId(), "BRL");
    }
}
