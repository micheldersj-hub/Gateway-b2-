package com.gatewayb2.card.client;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "gatewayb2.clients")
public record CardClientProperties(String accountServiceBaseUrl, String ledgerServiceBaseUrl) {
}
