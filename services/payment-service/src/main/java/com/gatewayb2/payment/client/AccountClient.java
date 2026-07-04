package com.gatewayb2.payment.client;

import com.gatewayb2.common.exception.BusinessException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import java.util.UUID;

@Component
public class AccountClient {

    private final RestClient restClient;

    public AccountClient(RestClient.Builder restClientBuilder, PaymentClientProperties properties) {
        this.restClient = restClientBuilder.baseUrl(properties.accountServiceBaseUrl()).build();
    }

    @CircuitBreaker(name = "account-service")
    public AccountDto getAccount(UUID accountId) {
        try {
            return restClient.get()
                    .uri("/api/v1/accounts/{id}", accountId)
                    .retrieve()
                    .body(AccountDto.class);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
                throw new BusinessException("ACCOUNT_NOT_FOUND", "Conta não encontrada: " + accountId, HttpStatus.NOT_FOUND);
            }
            throw new BusinessException("ACCOUNT_SERVICE_UNAVAILABLE", "Falha ao consultar account-service", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }

    @CircuitBreaker(name = "account-service")
    public AccountDto resolvePixKey(String keyValue) {
        try {
            return restClient.get()
                    .uri("/api/v1/accounts/pix-keys/{key}", keyValue)
                    .retrieve()
                    .body(AccountDto.class);
        } catch (RestClientResponseException e) {
            if (e.getStatusCode().value() == HttpStatus.NOT_FOUND.value()) {
                throw new BusinessException("PIX_KEY_NOT_FOUND", "Chave PIX não encontrada: " + keyValue, HttpStatus.NOT_FOUND);
            }
            throw new BusinessException("ACCOUNT_SERVICE_UNAVAILABLE", "Falha ao consultar account-service", HttpStatus.SERVICE_UNAVAILABLE);
        }
    }
}
