package ru.yandex.practicum.eshop.core.utils;

import lombok.experimental.UtilityClass;

import ru.yandex.practicum.eshop.core.entity.CartItem;
import ru.yandex.practicum.eshop.core.entity.Item;
import ru.yandex.practicum.eshop.core.entity.Orders;

@UtilityClass
public class Data {
    private static final Long ITEM_ID = 1L;
    private static final Long CART_ID = 1L;
    private static final Long ORDER_ID = 1L;
    private static final String ITEM_SHORTS_IMG_PATH = "/images/shorts.jpg";

    public Item getItemPlusCount() {
        return Item.builder()
                .id(ITEM_ID)
                .title("Шорты")
                .imgPath(ITEM_SHORTS_IMG_PATH)
                .description("Летние шорты карго")
                .price(1399.99)
                .count(1)
                .build();
    }

    public Orders getOrder() {
        return Orders.builder()
                     .id(ORDER_ID)
                     .totalSum(getItemPlusCount().getPrice())
                     .build();
    }

    public CartItem getCartItem() {
        return CartItem.builder()
                       .cartId(CART_ID)
                       .itemId(ITEM_ID)
                       .count(1)
                       .build();
    }
}