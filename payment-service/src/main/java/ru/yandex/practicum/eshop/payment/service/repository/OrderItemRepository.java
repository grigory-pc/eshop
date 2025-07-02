package ru.yandex.practicum.eshop.payment.service.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.eshop.payment.service.entity.OrderItem;

/**
 * Получение данных из таблицы OrderItem.
 */
@Repository
public interface OrderItemRepository extends R2dbcRepository<OrderItem, Long> {
}