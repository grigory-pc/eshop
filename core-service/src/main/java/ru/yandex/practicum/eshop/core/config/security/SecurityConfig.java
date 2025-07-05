package ru.yandex.practicum.eshop.core.config.security;

import java.util.Collection;
import java.util.Collections;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableReactiveMethodSecurity;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.eshop.core.entity.User;
import ru.yandex.practicum.eshop.core.exceptions.DataBaseRequestException;
import ru.yandex.practicum.eshop.core.repository.UserRepository;

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
  public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
    return http
        .csrf().disable()
        .authorizeExchange(exchanges -> exchanges
            .pathMatchers("/login", "/error").permitAll()
            .pathMatchers("/**").hasRole("USER")
            .anyExchange().authenticated()
        )
        .formLogin(form -> form
            .loginPage("/login"))
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
  public ReactiveUserDetailsService userDetailsService(UserRepository userRepository) {
    return username ->
        userRepository.findByUsername(username)
                      .map(UserDetailsImpl::new)
                      .cast(UserDetails.class)
                      .switchIfEmpty(
                          Mono.error(new UsernameNotFoundException(USER_NOT_FOUND.getMessage())))
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

class UserDetailsImpl implements UserDetails {
  private final User user;

  public UserDetailsImpl(User user) {
    this.user = user;
  }

  @Override
  public String getUsername() {
    return user.getUsername();
  }

  @Override
  public String getPassword() {
    return user.getPassword();
  }

  @Override
  public Collection<? extends GrantedAuthority> getAuthorities() {
    return Collections.singletonList(
        new SimpleGrantedAuthority(user.getRole())
    );
  }

  @Override
  public boolean isAccountNonExpired() {
    return true;
  }

  @Override
  public boolean isAccountNonLocked() {
    return true;
  }

  @Override
  public boolean isCredentialsNonExpired() {
    return true;
  }

  @Override
  public boolean isEnabled() {
    return true;
  }
}
