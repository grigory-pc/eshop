package ru.yandex.practicum.eshop.core.config.security;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("spring.security.oauth2")
public record OAuth2Properties(String clientId, String clientSecret, String scope,
                               String authorizationUri, String tokenUri) {
}