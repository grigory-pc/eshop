package ru.yandex.practicum.eshop.core.service;

import ch.qos.logback.core.joran.spi.ActionException;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.eshop.core.dto.CartDto;

/**
 * Сервис для работы с корзиной.
 */
public interface CartService {
  /**
   * Изменение состава корзины товаров.
   *
   * @param itemId - id товара.
   * @param action - действие с товаром в корзине.
   * @param username - имя пользователя.
   *
   * @throws ch.qos.logback.core.joran.spi.ActionException - исключение в случае некорректного значения в запросе для action.
   */
  Mono<Void> editCart(Long itemId, String action, String username) throws ActionException;

  /**
   * Получение всех товаров корзины.
   * @param username - имя пользователя.
   *
   * @return список товаров в корзине.
   */
  Mono<CartDto> getCartItems(String username);

  /**
   * Формирование заказа для товаров в корзине.
   * @param username - имя пользователя.
   *
   * @return id заказа.
   */
  Mono<Long> buyItems(String username);
}