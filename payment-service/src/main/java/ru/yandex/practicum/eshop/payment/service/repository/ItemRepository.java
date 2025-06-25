package ru.yandex.practicum.eshop.payment.service.repository;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.eshop.payment.service.entity.Item;

/**
 * Получение данных из таблицы Items.
 */
@Repository
public interface ItemRepository extends R2dbcRepository<Item, Long> {
  /**
   * Сброс количества товаров.
   */
  @Transactional
  @Modifying
  @Query("UPDATE item SET count = 0")
  Mono<Void> updateAllCountToZero();
}