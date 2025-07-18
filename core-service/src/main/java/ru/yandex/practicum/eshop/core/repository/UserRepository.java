package ru.yandex.practicum.eshop.core.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.eshop.core.entity.User;

/**
 * Получение данных из таблицы Users.
 */
@Repository
public interface UserRepository extends R2dbcRepository<User, Long> {
  @Query("SELECT * FROM users WHERE username = :username")
  Mono<User> findByUsername(String username);

  @Query("SELECT id FROM users WHERE username = :username")
  Mono<Long> findIdByUsername(String username);
}