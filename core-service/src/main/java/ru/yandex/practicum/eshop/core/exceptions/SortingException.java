package ru.yandex.practicum.eshop.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение в случае, если пришел некорректный тип сортировки.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class SortingException extends RuntimeException {
  public SortingException(String message) {
    super(message);
  }
}