package ru.yandex.practicum.eshop.core.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("client.payment-service")
public record PaymentServiceClientProps(String baseUrl, int connectTimeoutMs,
                                        int responseTimeoutMs) {
}
