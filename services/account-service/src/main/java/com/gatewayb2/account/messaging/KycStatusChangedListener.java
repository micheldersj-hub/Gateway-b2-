package com.gatewayb2.account.messaging;

import com.gatewayb2.account.service.AccountService;
import com.gatewayb2.common.domain.KycStatus;
import com.gatewayb2.common.event.KycStatusChangedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class KycStatusChangedListener {

    private static final Logger log = LoggerFactory.getLogger(KycStatusChangedListener.class);

    private final AccountService accountService;

    public KycStatusChangedListener(AccountService accountService) {
        this.accountService = accountService;
    }

    @KafkaListener(topics = KycStatusChangedEvent.TOPIC, groupId = "account-service")
    public void onKycStatusChanged(KycStatusChangedEvent event) {
        log.info("Evento KycStatusChangedEvent recebido: customerId={}, status={}", event.customerId(), event.newStatus());
        if (event.newStatus() == KycStatus.APPROVED) {
            accountService.openAccountForApprovedCustomer(event);
        }
    }
}
