package com.gatewayb2.gateway.config;

import com.gatewayb2.gateway.filter.ApiKeyAuthenticationFilter;
import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import reactor.core.publisher.Mono;

@Configuration
public class RateLimiterConfig {

    /** Limita por cliente parceiro (X-Client-Id, atribuído pelo ApiKeyAuthenticationFilter). */
    @Bean
    public KeyResolver clientKeyResolver() {
        return exchange -> {
            String clientId = exchange.getRequest().getHeaders().getFirst(ApiKeyAuthenticationFilter.CLIENT_ID_HEADER);
            if (clientId != null) {
                return Mono.just(clientId);
            }
            String remoteAddress = exchange.getRequest().getRemoteAddress() != null
                    ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                    : "unknown";
            return Mono.just(remoteAddress);
        };
    }
}
