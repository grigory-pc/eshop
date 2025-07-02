package ru.yandex.practicum.eshop.payment.service.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum MessagesLog {
  MESSAGE_LOG_DB_SAVE_REQUEST("Отправляем запрос в БД для сохранения(обновления) данных"),
  MESSAGE_LOG_DB_RESPONSE_ERROR(
      "Возникла непредвиденная ситуация во время получения(отправки) данных из БД"),
  MESSAGE_LOG_FLUSH_CART("Очистка корзины и количества товаров после размещения заказа"),
  MESSAGE_LOG_FLUSH_CART_SUCCESS("Очистка корзины и количества товаров успешно выполнена");

  private final String message;
}
