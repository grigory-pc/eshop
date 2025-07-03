package ru.yandex.practicum.eshop.core.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Конфигурация Spring Security.
 */
@Slf4j
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

  @Bean
  public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
    return http
        .formLogin(withDefaults())
        .authorizeExchange(exchanges -> exchanges.anyExchange().authenticated())
        .logout(logout -> logout
            .logoutUrl("/logout")
            .logoutSuccessHandler((webFilterExchange, authentication) ->
                                      invalidateSession(webFilterExchange.getExchange())
            )
        )
        .build();
  }

  @Bean
  public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
    return http
        .authorizeExchange(exchanges -> exchanges
            .pathMatchers("/**").hasRole("USER")
            .anyExchange().authenticated()
        )
        .build();
  }

  private Mono<Void> invalidateSession(ServerWebExchange exchange) {
    return exchange.getSession()
                   .flatMap(session -> session.invalidate()
                                              .then(Mono.fromRunnable(() -> {
                                                exchange.getResponse().setStatusCode(HttpStatus.OK);
                                                exchange.getResponse().getCookies()
                                                        .remove("SESSION");
                                              }))
                                              .onErrorMap(e -> {
                                                log.error("Ошибка при удалении сессии", e);
                                                return e;
                                              }))
                   .onErrorResume(error -> {
                     log.error("Ошибка при обработке сессии", error);
                     return Mono.empty();
                   }).then();
  }
}