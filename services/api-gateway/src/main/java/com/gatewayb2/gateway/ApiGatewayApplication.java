package com.gatewayb2.gateway;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Aplicação 100% reativa (WebFlux/Netty). Propositalmente NÃO usa scanBasePackages="com.gatewayb2"
 * como os demais serviços: o common traz um GlobalExceptionHandler baseado em Servlet
 * (HttpServletRequest) que não se aplica a este gateway reativo.
 */
@SpringBootApplication
public class ApiGatewayApplication {
    public static void main(String[] args) {
        SpringApplication.run(ApiGatewayApplication.class, args);
    }
}
