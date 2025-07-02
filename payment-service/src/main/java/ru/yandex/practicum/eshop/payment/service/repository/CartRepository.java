package ru.yandex.practicum.eshop.payment.service.repository;

import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.eshop.payment.service.entity.Cart;

/**
 * Получение данных из таблицы Carts.
 */
@Repository
public interface CartRepository extends R2dbcRepository<Cart, Long> {
}