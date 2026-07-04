package com.gatewayb2.gateway.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gatewayb2.common.exception.ErrorResponse;
import com.gatewayb2.gateway.repository.ApiClientRepository;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

/**
 * Autenticação de parceiros via API Key (modelo semelhante a Stripe/outras plataformas BaaS).
 * A chave é enviada no header X-API-Key, seu hash SHA-256 é comparado ao armazenado na base -
 * a chave em texto claro nunca é persistida.
 */
@Component
public class ApiKeyAuthenticationFilter implements GlobalFilter, Ordered {

    public static final String API_KEY_HEADER = "X-API-Key";
    public static final String CLIENT_ID_HEADER = "X-Client-Id";
    public static final String CLIENT_NAME_HEADER = "X-Client-Name";

    private static final List<String> PUBLIC_PATHS = List.of(
            "/actuator", "/swagger-ui", "/v3/api-docs", "/webjars");

    private final ApiClientRepository apiClientRepository;
    private final ObjectMapper objectMapper;

    public ApiKeyAuthenticationFilter(ApiClientRepository apiClientRepository, ObjectMapper objectMapper) {
        this.apiClientRepository = apiClientRepository;
        this.objectMapper = objectMapper;
    }

    @Override
    public int getOrder() {
        return -100;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getURI().getPath();
        if (isPublic(path)) {
            return chain.filter(exchange);
        }

        ServerHttpRequest request = exchange.getRequest();
        String apiKey = request.getHeaders().getFirst(API_KEY_HEADER);
        if (apiKey == null || apiKey.isBlank()) {
            return unauthorized(exchange, "API_KEY_MISSING", "Header X-API-Key é obrigatório");
        }

        String hash = ApiKeyHasher.sha256Hex(apiKey);
        return apiClientRepository.findByApiKeyHashAndActiveTrue(hash)
                .flatMap(client -> {
                    ServerHttpRequest mutatedRequest = request.mutate()
                            .header(CLIENT_ID_HEADER, client.id().toString())
                            .header(CLIENT_NAME_HEADER, client.clientName())
                            .build();
                    return chain.filter(exchange.mutate().request(mutatedRequest).build());
                })
                .switchIfEmpty(Mono.defer(() -> unauthorized(exchange, "API_KEY_INVALID", "API Key inválida ou inativa")));
    }

    private boolean isPublic(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::startsWith);
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange, String errorCode, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        ErrorResponse body = ErrorResponse.of(HttpStatus.UNAUTHORIZED.value(), errorCode, message,
                exchange.getRequest().getURI().getPath());
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(body);
            DataBuffer buffer = response.bufferFactory().wrap(bytes);
            return response.writeWith(Mono.just(buffer));
        } catch (Exception e) {
            return response.setComplete();
        }
    }

}
