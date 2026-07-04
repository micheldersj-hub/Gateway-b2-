package com.gatewayb2.kyc.messaging;

import com.gatewayb2.common.domain.KycStatus;
import com.gatewayb2.common.event.KycStatusChangedEvent;
import com.gatewayb2.kyc.domain.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class KycEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(KycEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public KycEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishStatusChanged(Customer customer, KycStatus previousStatus) {
        KycStatusChangedEvent event = new KycStatusChangedEvent(
                customer.getId(),
                customer.getPersonType(),
                customer.getDocument(),
                previousStatus,
                customer.getStatus(),
                Instant.now()
        );
        log.info("Publicando KycStatusChangedEvent: customerId={}, {} -> {}", customer.getId(), previousStatus, customer.getStatus());
        kafkaTemplate.send(KycStatusChangedEvent.TOPIC, customer.getId().toString(), event);
    }
}
