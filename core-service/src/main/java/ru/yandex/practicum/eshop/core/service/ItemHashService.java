package ru.yandex.practicum.eshop.core.service;

import java.util.Set;
import org.springframework.data.domain.Pageable;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.eshop.core.entity.Item;

/**
 * Сервис для работы с данными товара из кеша Redis.
 */
public interface ItemHashService {
  /**
   * Поиск всех товаров.
   *
   * @param pageableRequest - содержит from - с какой страницы и size - количество записей + тип
   *                        сортировки.
   * @return коллекция товаров с параметрами пагинации.
   */
  Flux<Item> findAll(Pageable pageableRequest);

  /**
   * Поиск товаров на базе строки поиска.
   *
   * @param search          - строка для поиска товаров с буквами, с которых начинается название
   *                        товаров.
   * @param pageableRequest - содержит from - с какой страницы и size - количество записей + тип
   *                        сортировки.
   * @return коллекция товаров с параметрами пагинации.
   */
  Flux<Item> findByTitleContainingIgnoreCase(String search, Pageable pageableRequest);

  /**
   * Увеличение количества товаров для отображения на главной странице после добавления в корзину.
   *
   * @param id - id товара.
   */
  //    @Query("UPDATE item SET count = count + 1 WHERE id = :id")
  Mono<Void> incrementCount(Long id);

  /**
   * Уменьшение количества товаров для отображения на главной странице после добавления в корзину.
   *
   * @param id - id товара.
   */
  //    @Query("UPDATE item SET count = count - 1 WHERE id = :id")
  Mono<Void> decrementCount(Long id);

  /**
   * Поиск товара по id.
   *
   * @param id - id товара.
   */
  Mono<Item> findById(Long id);

  /**
   * Поиск товаров по списку id.
   *
   * @param ids - множество id товаров.
   */
  Flux<Item> findAllByIds(Set<Long> ids);
}