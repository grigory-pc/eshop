package ru.yandex.practicum.eshop.core.config.security;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.eshop.core.exceptions.DataBaseRequestException;
import ru.yandex.practicum.eshop.core.repository.UserRepository;

import static org.springframework.security.config.Customizer.withDefaults;
import static ru.yandex.practicum.eshop.core.enums.MessagesLog.MESSAGE_LOG_DB_RESPONSE_ERROR;
import static ru.yandex.practicum.eshop.core.enums.MessagesLog.MESSAGE_LOG_FIND_USER;
import static ru.yandex.practicum.eshop.core.enums.MessagesLog.USER_NOT_FOUND;

/**
 * Конфигурация Spring Security.
 */
@Slf4j
@Configuration
@EnableWebFluxSecurity
@EnableReactiveMethodSecurity
public class SecurityConfig {
  @Bean
  public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
                                                       ReactiveAuthenticationManager authenticationManager) {
    return http
        .authenticationManager(authenticationManager)
        .authorizeExchange(exchanges -> exchanges
            .pathMatchers("/login", "/error").permitAll()
            .pathMatchers("/**").hasRole("USER")
            .anyExchange().authenticated()
        )
        .formLogin(withDefaults()
        )
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
  public ReactiveAuthenticationManager authenticationManager(
      ReactiveUserDetailsService reactiveUserDetailsService) {
    return authentication -> reactiveUserDetailsService.findByUsername(authentication.getName())
                                                       .map(userDetails -> {
                                                         String presentedPassword
                                                             = (String) authentication.getCredentials();
                                                         if (!passwordEncoder().matches(
                                                             presentedPassword,
                                                             userDetails.getPassword())) {
                                                           throw new BadCredentialsException(
                                                               "Неверный пароль");
                                                         }
                                                         return new UsernamePasswordAuthenticationToken(
                                                             userDetails,
                                                             authentication.getCredentials(),
                                                             userDetails.getAuthorities());
                                                       })
                                                       .cast(Authentication.class)
                                                       .switchIfEmpty(Mono.error(
                                                           new BadCredentialsException(
                                                               "Неверное имя пользователя")))
                                                       .onErrorMap(UsernameNotFoundException.class,
                                                                   ex -> new BadCredentialsException(
                                                                       "Неверное имя пользователя"));
  }

  @Bean
  public ReactiveUserDetailsService reactiveUserDetailsService(UserRepository userRepository) {
    return username -> userRepository.findByUsername(username)
                                     .doOnSuccess(
                                         user -> log.debug("Пользователь найден: {}", user))
                                     .doOnError(e -> log.error("Ошибка при поиске пользователя", e))
                                     .map(UserDetailsImpl::new)
                                     .cast(UserDetails.class)
                                     .switchIfEmpty(
                                         Mono.error(new UsernameNotFoundException(
                                             USER_NOT_FOUND.getMessage()))
                                     )
                                     .onErrorResume(e -> {
                                       log.error(MESSAGE_LOG_FIND_USER.getMessage(), e);
                                       return Mono.error(new DataBaseRequestException(
                                           MESSAGE_LOG_DB_RESPONSE_ERROR.getMessage(), e));
                                     });
  }

  @Bean
  PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
  }
}