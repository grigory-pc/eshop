package ru.yandex.practicum.eshop.core.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import ru.yandex.practicum.eshop.core.exceptions.NegativeDurationException;

@Configuration
@RequiredArgsConstructor
public class PaymentServiceClientBinding {
  private final PaymentServiceClientProps props;

  @Bean
  public WebClient pinCheckWebClient() throws NegativeDurationException {
    return DefaultWebClientFactory.getClient(props.connectTimeoutMs(), props.responseTimeoutMs(),
                                             props.baseUrl());
  }
}