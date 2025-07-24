package ru.yandex.practicum.eshop.core.config.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.client.AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientProviderBuilder;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientService;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServerOAuth2AuthorizedClientExchangeFilterFunction;

@Configuration
public class OAuth2ClientConfiguration {

  @Bean
  public ReactiveOAuth2AuthorizedClientManager authorizedClientManager(
      ReactiveClientRegistrationRepository clientRegistrationRepository,
      ReactiveOAuth2AuthorizedClientService authorizedClientService) {

    AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager manager =
        new AuthorizedClientServiceReactiveOAuth2AuthorizedClientManager(
            clientRegistrationRepository, authorizedClientService);

    manager.setAuthorizedClientProvider(ReactiveOAuth2AuthorizedClientProviderBuilder.builder()
                                                                                     .clientCredentials()
                                                                                     .build());

    return manager;
  }

  @Bean
  public ServerOAuth2AuthorizedClientExchangeFilterFunction oauth2Client(
      ReactiveOAuth2AuthorizedClientManager authorizedClientManager) {

    ServerOAuth2AuthorizedClientExchangeFilterFunction oauth2Client =
        new ServerOAuth2AuthorizedClientExchangeFilterFunction(
            authorizedClientManager);

    oauth2Client.setDefaultOAuth2AuthorizedClient(true);

    return oauth2Client;
  }
}