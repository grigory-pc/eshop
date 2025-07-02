package ru.yandex.practicum.eshop.core.repository;

import ru.yandex.practicum.eshop.core.entity.OrderItem;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

/**
 * Получение данных из таблицы OrderItem.
 */
@Repository
public interface OrderItemRepository extends R2dbcRepository<OrderItem, Long> {
  /**
   * Получение всех записей по order id.
   *
   * @param orderId - id заказа.
   * @return список всех записей, полученных по order id.
   */
  Flux<OrderItem> findOrderItemsByOrderId(Long orderId);
}