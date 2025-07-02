package ru.yandex.practicum.eshop.core.exceptions;

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
  private static final String ACTION_EXCEPTION = "Action Exception";
  private static final String SORT_EXCEPTION = "Sorting Exception";
  private static final String INTERNAL_ERROR = "Internal_Error";

  @ExceptionHandler(ActionException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Map<String, String> handleBadActionFormatException(ActionException exception) {
    log.error(exception.getMessage());

    return Map.of(ERROR, ACTION_EXCEPTION);
  }

  @ExceptionHandler(SortingException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Map<String, String> handleBadSortFormatException(SortingException exception) {
    log.error(exception.getMessage());

    return Map.of(ERROR, SORT_EXCEPTION);
  }

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