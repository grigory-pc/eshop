package ru.yandex.practicum.eshop.payment.service.service;

import reactor.core.publisher.Mono;
import ru.yandex.practicum.eshop.payment.service.entity.Item;

/**
 * Сервис для работы с данными товара из кеша Redis.
 */
public interface ItemHashService {

  /**
   * Поиск товара по id.
   *
   * @param id - id товара.
   */
  Mono<Item> findById(Long id);

  /**
   * Сброс количества товаров.
   */
  Mono<Void> updateAllCountToZero();
}