package ru.yandex.practicum.eshop.core.service;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.eshop.core.dto.OrderDto;

/**
 * Сервис для работы с заказами.
 */
public interface OrderService {
  /**
   * Получение всех заказов.
   *
   * @return список заказов.
   */
  Flux<OrderDto> getOrders();

  /**
   * Получение объекта заказа по id.
   *
   * @param id - id заказа.
   * @return объекта заказа.
   */
  Mono<OrderDto> getOrderItems(Long id);
}