package ru.yandex.practicum.eshop.core.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

/**
 * Класс связи товара с заказом.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@Table(name = "order_item")
public class OrderItem {
  @Id
  @Column("id")
  private Long id;
  @Column("order_id")
  private Long orderId;
  @Column("item_id")
  private Long itemId;
  @Column("count")
  private Integer count;
}