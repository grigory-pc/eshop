package ru.yandex.practicum.eshop.core.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;
import lombok.Builder;

/**
 * DTO заказа с товарами.
 */
@Builder
public record OrderDto(@JsonProperty(value = "id", required = true) Long id,
                       @JsonProperty(value = "items", required = true) List<ItemDto> items,
                       @JsonProperty(value = "totalSum", required = true) Double totalSum) {
}