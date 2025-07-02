package ru.yandex.practicum.eshop.payment.service.service;

import reactor.core.publisher.Mono;

/**
 * Сервис для работы с товарами.
 */
public interface PaymentService {
  /**
   * Формирование заказа для товаров в корзине.
   * @param cartId - id корзины.
   *
   * @return id заказа.
   */
  Mono<Long> buyItems(Long cartId);
}