package com.gatewayb2.payment.messaging;

import com.gatewayb2.common.event.PaymentCompletedEvent;
import com.gatewayb2.common.event.PaymentFailedEvent;
import com.gatewayb2.payment.domain.Payment;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Component
public class PaymentEventPublisher {

    private static final Logger log = LoggerFactory.getLogger(PaymentEventPublisher.class);

    private final KafkaTemplate<String, Object> kafkaTemplate;

    public PaymentEventPublisher(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    public void publishCompleted(Payment payment) {
        PaymentCompletedEvent event = new PaymentCompletedEvent(
                payment.getId(), payment.getAccountId(), payment.getMethod(), payment.getDirection(),
                payment.getAmount(), payment.getCurrency(), payment.getCounterpartyName(),
                payment.getCounterpartyDocument(), payment.getIdempotencyKey(), Instant.now());
        log.info("Publicando PaymentCompletedEvent: paymentId={}", payment.getId());
        kafkaTemplate.send(PaymentCompletedEvent.TOPIC, payment.getId().toString(), event);
    }

    public void publishFailed(Payment payment) {
        PaymentFailedEvent event = new PaymentFailedEvent(payment.getId(), payment.getAccountId(),
                payment.getFailureReason(), Instant.now());
        log.info("Publicando PaymentFailedEvent: paymentId={}, motivo={}", payment.getId(), payment.getFailureReason());
        kafkaTemplate.send(PaymentFailedEvent.TOPIC, payment.getId().toString(), event);
    }
}
