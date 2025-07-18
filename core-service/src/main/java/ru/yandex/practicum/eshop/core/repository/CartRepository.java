package ru.yandex.practicum.eshop.core.repository;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.eshop.core.entity.Cart;

/**
 * Получение данных из таблицы Carts.
 */
@Repository
public interface CartRepository extends R2dbcRepository<Cart, Long> {
  @Query("SELECT * FROM cart WHERE username = :username")
  Mono<Cart> findByUsername(String username);
}
