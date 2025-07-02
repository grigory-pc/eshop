package ru.yandex.practicum.eshop.core.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.yandex.practicum.eshop.core.exceptions.ActionException;

/**
 * Enum для действий с товарами в корзине.
 */
@Getter
@RequiredArgsConstructor public enum Action {
  PLUS,
  MINUS,
  DELETE;

  public static Action getValueOf(String action) throws ActionException {
      for (Action type : Action.values()) {
        if (type.name().equalsIgnoreCase(action)) {
          return type;
        }
      } throw new ActionException("Некорректный тип действия с товаром");
  }
}