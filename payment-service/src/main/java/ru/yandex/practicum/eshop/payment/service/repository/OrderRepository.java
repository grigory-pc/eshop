package ru.yandex.practicum.eshop.payment.service.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.eshop.payment.service.entity.Orders;

/**
 * Получение данных из таблицы Orders.
 */
@Repository
public interface OrderRepository extends R2dbcRepository<Orders, Long> {
}