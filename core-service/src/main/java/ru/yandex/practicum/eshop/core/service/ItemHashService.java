package ru.yandex.practicum.eshop.core.service;

import java.util.List;
import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.eshop.core.entity.Item;

/**
 * Сервис для работы с данными товара из кеша Redis.
 */
public interface ItemHashService {
  /**
   * Поиск товаров на базе строки поиска.
   *
   * @param search          - строка для поиска товаров с буквами, с которых начинается название
   *                        товаров.
   * @param pageableRequest - содержит from - с какой страницы и size - количество записей + тип
   *                        сортировки.
   * @return коллекция товаров с параметрами пагинации.
   */
  List<Item> findByTitleContainingIgnoreCase(String search, Pageable pageableRequest);

  /**
   * Увеличение количества товаров для отображения на главной странице после добавления в корзину.
   *
   * @param id - id товара.
   */
  //    @Query("UPDATE item SET count = count + 1 WHERE id = :id")
  void incrementCount(Long id);

  /**
   * Уменьшение количества товаров для отображения на главной странице после добавления в корзину.
   *
   * @param id - id товара.
   */
  //    @Query("UPDATE item SET count = count - 1 WHERE id = :id")
  void decrementCount(Long id);

  /**
   * Поиск всех товаров.
   */
  List<Item> findAll();

  /**
   * Поиск товара по id.
   *
   * @param id - id товара.
   */
  Item findById(Long id);

  /**
   * Поиск товаров по списку id.
   *
   * @param ids - список id товаров.
   */
  List<Item> findAllByIds(List<Long> ids);
}