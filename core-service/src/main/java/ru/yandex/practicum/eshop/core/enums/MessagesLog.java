package ru.yandex.practicum.eshop.core.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MessagesLog {
  MESSAGE_LOG_DB_GET_REQUEST("Отправляем запрос в БД для получения данных"),
  MESSAGE_LOG_DB_SAVE_REQUEST("Отправляем запрос в БД для сохранения(обновления) данных"),
  MESSAGE_LOG_DB_RESPONSE_ERROR(
      "Возникла непредвиденная ситуация во время получения(отправки) данных из БД"),
  MESSAGE_LOG_ITEMS_SIZE("Получен ответ от БД со списком товаров размером = {}"),
  MESSAGE_LOG_FLUSH_CART("Очистка корзины и количества товаров после размещения заказа"),
  MESSAGE_LOG_FLUSH_CART_SUCCESS("Очистка корзины и количества товаров успешно выполнена"),
  MESSAGE_LOG_ADD_ITEM_TO_CART("Ошибка доступа к базе данных при попытке добавления товара в корзину"),
  MESSAGE_LOG_SAVE_CART("Ошибка БД при попытке сохранения корзины"),
  MESSAGE_LOG_FIND_CARTITEM("Ошибка БД при попытке поиска соотношения товара и корзины"),
  MESSAGE_LOG_FIND_ITEM_OR_CARTITEM("Ошибка БД при попытке поиска товара или соотношения товара и корзины"),
  MESSAGE_LOG_FIND_ALL_ITEMS("Ошибка БД при попытке поиска всех товаров"),
  MESSAGE_LOG_FIND_ITEM("Ошибка БД при попытке поиска товара");

  private final String message;
}
