package com.gatewayb2.kyc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication(scanBasePackages = "com.gatewayb2")
public class KycServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(KycServiceApplication.class, args);
    }
}
