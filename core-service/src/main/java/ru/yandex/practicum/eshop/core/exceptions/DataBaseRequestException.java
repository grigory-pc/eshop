package ru.yandex.practicum.eshop.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * Исключение в случае исключения при взаимодействии с БД.
 */
@ResponseStatus(HttpStatus.BAD_REQUEST)
public class DataBaseRequestException extends RuntimeException {

  public DataBaseRequestException(String message, Throwable cause) {
    super(message, cause);
  }
}