package ru.yandex.practicum.eshop.core.config.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.eshop.core.repository.UserRepository;

import static org.springframework.security.config.Customizer.withDefaults;

/**
 * Конфигурация Spring Security.
 */
@Slf4j
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {

  @Bean
  public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
    return http
        .formLogin(withDefaults())
        .authorizeExchange(exchanges -> exchanges.anyExchange().authenticated())
        .logout(logout -> logout
            .logoutUrl("/logout")
            .logoutSuccessHandler((exchange, authentication) ->
                                      exchange.getExchange().getSession()
                                              .flatMap(WebSession::invalidate)
                                              .then(Mono.fromRunnable(() ->
                                                                          exchange.getExchange()
                                                                                  .getResponse()
                                                                                  .setStatusCode(
                                                                                      HttpStatus.OK)))
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

  @Bean
  public ReactiveUserDetailsService userDetailsService(UserRepository userRepository) {
    return username ->
        userRepository.findByUsername(username)
                      .map(UserDetailsImpl::new)
                      .cast(UserDetails.class)
                      .switchIfEmpty(
                          Mono.error(new UsernameNotFoundException("Пользователь не найден")));
  }

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}
