package com.gatewayb2.payment.client;

import com.gatewayb2.common.exception.BusinessException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class LedgerClient {

    private final RestClient restClient;

    public LedgerClient(RestClient.Builder restClientBuilder, PaymentClientProperties properties) {
        this.restClient = restClientBuilder.baseUrl(properties.ledgerServiceBaseUrl()).build();
    }

    /** Retorna o saldo disponível da conta; zero se a conta ainda não possui movimentações/lançamentos. */
    @CircuitBreaker(name = "ledger-service")
    public BigDecimal getAvailableBalance(UUID accountId) {
        try {
            BalanceResponse response = restClient.get()
                    .uri("/api/v1/ledger-accounts/by-external-account/{id}/balance", accountId)
                    .retrieve()
                    .body(BalanceResponse.class);
            return response == null ? BigDecimal.ZERO : response.balance();
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
                return BigDecimal.ZERO;
            }
            throw new BusinessException("LEDGER_SERVICE_UNAVAILABLE", "Falha ao consultar saldo no ledger-service", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    private record BalanceResponse(UUID ledgerAccountId, UUID externalAccountId, String currency, BigDecimal balance) {
    }
}
