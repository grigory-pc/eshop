package ru.yandex.practicum.eshop.core.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Класс связи товара с корзиной.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@Table(name = "cart_item")
public class CartItem {
  @Id
  @Column("id")
  private Long id;
  @Column("cart_id")
  private Long cartId;
  @Column("item_id")
  private Long itemId;
  @Column("count")
  private Integer count;
}