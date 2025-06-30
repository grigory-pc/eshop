package ru.yandex.practicum.eshop.payment.service.service.impl;

import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;
import ru.yandex.practicum.eshop.payment.service.entity.Cart;
import ru.yandex.practicum.eshop.payment.service.entity.CartItem;
import ru.yandex.practicum.eshop.payment.service.entity.Item;
import ru.yandex.practicum.eshop.payment.service.entity.OrderItem;
import ru.yandex.practicum.eshop.payment.service.entity.Orders;
import ru.yandex.practicum.eshop.payment.service.exceptions.DataBaseRequestException;
import ru.yandex.practicum.eshop.payment.service.repository.CartItemRepository;
import ru.yandex.practicum.eshop.payment.service.repository.CartRepository;
import ru.yandex.practicum.eshop.payment.service.repository.OrderItemRepository;
import ru.yandex.practicum.eshop.payment.service.repository.OrderRepository;
import ru.yandex.practicum.eshop.payment.service.service.ItemHashService;
import ru.yandex.practicum.eshop.payment.service.service.PaymentService;

import static ru.yandex.practicum.eshop.payment.service.enums.MessagesLog.MESSAGE_LOG_DB_RESPONSE_ERROR;
import static ru.yandex.practicum.eshop.payment.service.enums.MessagesLog.MESSAGE_LOG_DB_SAVE_REQUEST;
import static ru.yandex.practicum.eshop.payment.service.enums.MessagesLog.MESSAGE_LOG_FLUSH_CART;
import static ru.yandex.practicum.eshop.payment.service.enums.MessagesLog.MESSAGE_LOG_FLUSH_CART_SUCCESS;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {
  private static final Double TOTAL_INIT = 0.00;
  private final ItemHashService itemHashService;
  private final CartRepository cartRepository;
  private final OrderRepository orderRepository;
  private final CartItemRepository cartItemRepository;
  private final OrderItemRepository orderItemRepository;

  @Override
  public Mono<Long> buyItems(Long cartId) {
    return Mono.defer(() -> {
      log.info(MESSAGE_LOG_DB_SAVE_REQUEST.getMessage());

      return cartRepository.findById(cartId)
                           .flatMap(this::fetchAndProcessCartItems)
                           .flatMap(this::createAndSaveOrder)
                           .flatMap(this::saveOrderItems)
                           .flatMap(savedOrder -> flushItemAndCart(savedOrder, cartId))
                           .map(Orders::getId)
                           .onErrorResume(e -> {
                             log.error(MESSAGE_LOG_DB_RESPONSE_ERROR.getMessage(), e);
                             return Mono.error(new DataBaseRequestException(
                                 MESSAGE_LOG_DB_RESPONSE_ERROR.getMessage(), e));
                           });
    });
  }

  private Mono<Orders> flushItemAndCart(Orders orders, Long cartId) {
    return Mono.defer(() -> {
      log.info(MESSAGE_LOG_FLUSH_CART.getMessage());

      Cart cart = new Cart(cartId, TOTAL_INIT);
      return cartRepository.save(cart)
                           .then(cartItemRepository.deleteAllByCartId(cartId))
                           .then(itemHashService.updateAllCountToZero())
                           .doOnSuccess(v -> log.info(MESSAGE_LOG_FLUSH_CART_SUCCESS.getMessage()))
                           .thenReturn(orders)
                           .onErrorResume(e -> {
                             log.error(MESSAGE_LOG_DB_RESPONSE_ERROR.getMessage(), e);
                             return Mono.error(new DataBaseRequestException(
                                 MESSAGE_LOG_DB_RESPONSE_ERROR.getMessage(), e));
                           });
    });
  }

  private Mono<Tuple2<Cart, List<CartItem>>> fetchAndProcessCartItems(Cart cart) {
    return cartItemRepository.findCartItemsByCartId(cart.getId())
                             .collectList()
                             .map(items -> Tuples.of(cart, items))
                             .onErrorResume(e -> {
                               log.error(MESSAGE_LOG_DB_RESPONSE_ERROR.getMessage(), e);
                               return Mono.error(new DataBaseRequestException(
                                   MESSAGE_LOG_DB_RESPONSE_ERROR.getMessage(), e));
                             });
  }

  private Mono<Tuple2<Orders, List<CartItem>>> createAndSaveOrder(
      Tuple2<Cart, List<CartItem>> cartAndItems) {
    Cart cart = cartAndItems.getT1();
    List<CartItem> cartItems = cartAndItems.getT2();

    Orders orders = Orders.builder()
                          .totalSum(cart.getTotal())
                          .build();

    return orderRepository.save(orders)
                          .map(ordersSaved -> Tuples.of(ordersSaved, cartItems));
  }

  private Mono<Orders> saveOrderItems(Tuple2<Orders, List<CartItem>> orderAndCartItems) {
    Orders orders = orderAndCartItems.getT1();
    List<CartItem> cartItems = orderAndCartItems.getT2();

    return Flux.fromIterable(cartItems)
               .flatMap(cartItem -> itemHashService.findById(cartItem.getItemId())
                                                   .map(item -> createOrderItem(orders, item,
                                                                               cartItem.getCount())))
               .collectList()
               .flatMap(orderItems -> orderItemRepository.saveAll(orderItems)
                                                         .collectList()
                                                         .thenReturn(orders))
               .onErrorResume(e -> {
                 log.error(MESSAGE_LOG_DB_RESPONSE_ERROR.getMessage(), e);
                 return Mono.error(new DataBaseRequestException(
                     MESSAGE_LOG_DB_RESPONSE_ERROR.getMessage(), e));
               });
  }

  private OrderItem createOrderItem(Orders orders, Item item, int count) {
    return OrderItem.builder()
                    .orderId(orders.getId())
                    .itemId(item.getId())
                    .count(count)
                    .build();
  }
}