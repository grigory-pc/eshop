package ru.yandex.practicum.eshop.core.service;

import ru.yandex.practicum.eshop.core.enums.Sorting;
import org.springframework.data.domain.PageImpl;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.eshop.core.dto.ItemDto;

/**
 * Сервис для работы с товарами.
 */
public interface ItemService {
  /**
   * Получение всех товаров.
   *
   * @param search     - строка поиска.
   * @param sort       - критерий сортировка.
   * @param pageNumber - с какой страницы
   * @param pageSize   - количество записей.
   * @return список товаров.
   */
  Mono<PageImpl<ItemDto>> getItems(String search, Sorting sort, int pageNumber, int pageSize);

  /**
   * Получение объекта товара по id.
   *
   * @param id - id товара.
   * @return объекта товара.
   */
  Mono<ItemDto> getItem(Long id);
}