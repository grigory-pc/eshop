package ru.yandex.practicum.eshop.core.pojo;

import lombok.Builder;
import ru.yandex.practicum.eshop.core.enums.Sorting;

/**
 * Набор данных для передачи в сервис, где получаем список товаров.
 *
 * @param search     - строка поиска.
 * @param sort       - критерий сортировка.
 * @param pageNumber - с какой страницы
 * @param pageSize   - количество записей.
 * @param username   - имя пользователя.
 */
@Builder
public record GetItemsData(String search, Sorting sort, int pageSize, int pageNumber,
                           String username) {
}