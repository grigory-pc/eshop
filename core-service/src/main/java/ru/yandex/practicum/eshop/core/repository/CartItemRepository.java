package ru.yandex.practicum.eshop.core.repository;

import ru.yandex.practicum.eshop.core.entity.CartItem;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 * Получение данных из таблицы CartItem.
 */
@Repository
public interface CartItemRepository extends R2dbcRepository<CartItem, Long> {

  /**
   * Получение записи соотношения товара к корзине.
   *
   * @param cartId - id корзины.
   * @param itemId - id товара.
   * @return запись соотношения товара к корзине.
   */
  @Query("SELECT * FROM cart_item WHERE cart_id = :cartId AND item_id = :itemId")
  Mono<CartItem> findCartItemByCartIdAndItemId(@Param("cartId") Long cartId, @Param("itemId") Long itemId);

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