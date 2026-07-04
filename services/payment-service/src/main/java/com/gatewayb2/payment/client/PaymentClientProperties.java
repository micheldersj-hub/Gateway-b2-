package com.gatewayb2.payment.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gatewayb2.clients")
public record PaymentClientProperties(String accountServiceBaseUrl, String ledgerServiceBaseUrl) {
}
