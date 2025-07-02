package ru.yandex.practicum.eshop.core.repository;

import ru.yandex.practicum.eshop.core.entity.Orders;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;

/**
 * Получение данных из таблицы Orders.
 */
@Repository
public interface OrderRepository extends R2dbcRepository<Orders, Long> {
}