package com.gatewayb2.card.messaging;

import com.gatewayb2.card.domain.CardTransaction;
import com.gatewayb2.common.event.CardTransactionAuthorizedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class CardEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(CardEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public CardEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishAuthorized(CardTransaction tx) {
        CardTransactionAuthorizedEvent event = new CardTransactionAuthorizedEvent(
                tx.getId(), tx.getCardId(), tx.getAccountId(), tx.getAmount(), tx.getCurrency(),
                tx.getMerchantName(), tx.getIdempotencyKey(), Instant.now());
        log.info("Publicando CardTransactionAuthorizedEvent: transactionId={}", tx.getId());
        kafkaTemplate.send(CardTransactionAuthorizedEvent.TOPIC, tx.getId().toString(), event);
    }
}
