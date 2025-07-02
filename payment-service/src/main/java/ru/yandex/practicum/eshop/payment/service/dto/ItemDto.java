package ru.yandex.practicum.eshop.payment.service.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;

/**
 * DTO товаров.
 */
@Data
@Builder
@RequiredArgsConstructor
@AllArgsConstructor
public class ItemDto {
  private Long id;
  private String title;
  private String imgPath;
  private String description;
  private double price;
  private int count;
}