package com.gatewayb2.account.messaging;

import com.gatewayb2.account.domain.Account;
import com.gatewayb2.common.event.AccountCreatedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class AccountEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(AccountEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public AccountEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishAccountCreated(Account account) {
        AccountCreatedEvent event = new AccountCreatedEvent(
                account.getId(), account.getCustomerId(), account.getAccountNumber(), account.getBranch(), Instant.now());
        log.info("Publicando AccountCreatedEvent: accountId={}, customerId={}", account.getId(), account.getCustomerId());
        kafkaTemplate.send(AccountCreatedEvent.TOPIC, account.getId().toString(), event);
    }
}
