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
 * Класса товара.
 */
@Getter
@Setter
@Builder
@AllArgsConstructor
@RequiredArgsConstructor
@Table(name = "item")
public class Item {
  @Id
  @Column("id")
  private Long id;
  @Column("title")
  private String title;
  @Column("image_path")
  private String imgPath;
  @Column("description")
  private String description;
  @Column("price")
  private Double price;
  @Column("count")
  private Integer count;
}