package com.gatewayb2.gateway.web;

import com.gatewayb2.common.exception.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

/** Destino do circuit breaker quando um serviço downstream está indisponível ou muito lento. */
@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @GetMapping
    public Mono<ResponseEntity<ErrorResponse>> fallback() {
        ErrorResponse body = ErrorResponse.of(HttpStatus.SERVICE_UNAVAILABLE.value(), "SERVICE_UNAVAILABLE",
                "O serviço solicitado está temporariamente indisponível. Tente novamente em instantes.", "/fallback");
        return Mono.just(ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(body));
    }
}
