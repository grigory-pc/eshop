package ru.yandex.practicum.eshop.payment.service.exceptions;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Обработка исключений.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {
  private static final String ERROR = "error";
  private static final String INTERNAL_ERROR = "Internal_Error";

  @ExceptionHandler(DataBaseRequestException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public Map<String, String> handleDataBaseRequestException(DataBaseRequestException exception) {
    log.error(exception.getMessage());

    return Map.of(ERROR, INTERNAL_ERROR);
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  public Map<String, String> handleException(Exception exception) {
    log.error(exception.getMessage());

    return Map.of(ERROR, INTERNAL_ERROR);
  }
}