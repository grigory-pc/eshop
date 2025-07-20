package ru.yandex.practicum.eshop.core.service;

import org.springframework.data.domain.PageImpl;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.eshop.core.dto.ItemDto;
import ru.yandex.practicum.eshop.core.pojo.GetItemsData;

/**
 * Сервис для работы с товарами.
 */
public interface ItemService {
  /**
   * Получение всех товаров.
   *
   * @param getItemsData - набор данных из запроса.
   *
   * @return список товаров.
   */
  Mono<PageImpl<ItemDto>> getItems(GetItemsData getItemsData);

  /**
   * Получение объекта товара по id.
   *
   * @param id - id товара.
   * @return объекта товара.
   */
  Mono<ItemDto> getItem(Long id);
}