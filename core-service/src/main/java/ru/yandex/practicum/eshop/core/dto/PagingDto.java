package ru.yandex.practicum.eshop.core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;

/**
 * DTO пагинации
 *
 * @param pageNumber  - с какой страницы.
 * @param pageSize    - количество на странице.
 * @param hasPrevious - есть ли предыдущая страница.
 * @param hasNext     - есть ли следующая страница.
 */
@Builder
public record PagingDto(@JsonProperty(value = "pageNumber", required = true) Integer pageNumber,
                        @JsonProperty(value = "pageSize",
                                      required = true) Integer pageSize,
                        @JsonProperty(value = "hasPrevious",
                                      required = true) boolean hasPrevious,
                        @JsonProperty(value = "hasNext",
                                      required = true) boolean hasNext) {
}