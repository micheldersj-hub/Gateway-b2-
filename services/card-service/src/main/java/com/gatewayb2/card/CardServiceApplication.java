package com.gatewayb2.card;

import com.gatewayb2.card.client.CardClientProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication(scanBasePackages = "com.gatewayb2")
@EnableConfigurationProperties(CardClientProperties.class)
public class CardServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(CardServiceApplication.class, args);
    }
}
