package ru.yandex.practicum.eshop.payment.service.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.eshop.payment.service.entity.CartItem;

/**
 * Получение данных из таблицы CartItem.
 */
@Repository
public interface CartItemRepository extends R2dbcRepository<CartItem, Long> {
  /**
   * Получение всех записей по cart id.
   *
   * @param cartId - id корзины.
   * @return список всех записей, полученных по cart id.
   */
  Flux<CartItem> findCartItemsByCartId(Long cartId);

  /**
   * Удаление всех связей по id корзины.
   *
   * @param cartId - id корзины.
   */
  Mono<Void> deleteAllByCartId(Long cartId);
}