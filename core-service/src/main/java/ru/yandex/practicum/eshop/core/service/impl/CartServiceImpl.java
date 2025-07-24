package ru.yandex.practicum.eshop.core.service.impl;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.eshop.core.dto.CartDto;
import ru.yandex.practicum.eshop.core.dto.CreatePaymentResponse;
import ru.yandex.practicum.eshop.core.entity.Cart;
import ru.yandex.practicum.eshop.core.entity.CartItem;
import ru.yandex.practicum.eshop.core.entity.Item;
import ru.yandex.practicum.eshop.core.enums.Action;
import ru.yandex.practicum.eshop.core.exceptions.ActionException;
import ru.yandex.practicum.eshop.core.exceptions.DataBaseRequestException;
import ru.yandex.practicum.eshop.core.mappers.ItemMapper;
import ru.yandex.practicum.eshop.core.repository.CartItemRepository;
import ru.yandex.practicum.eshop.core.repository.CartRepository;
import ru.yandex.practicum.eshop.core.repository.ItemHashService;
import ru.yandex.practicum.eshop.core.service.CartService;

import static ru.yandex.practicum.eshop.core.enums.MessagesLog.MESSAGE_LOG_ADD_ITEM_TO_CART;
import static ru.yandex.practicum.eshop.core.enums.MessagesLog.MESSAGE_LOG_DB_GET_REQUEST;
import static ru.yandex.practicum.eshop.core.enums.MessagesLog.MESSAGE_LOG_DB_RESPONSE_ERROR;
import static ru.yandex.practicum.eshop.core.enums.MessagesLog.MESSAGE_LOG_FIND_CARTITEM;
import static ru.yandex.practicum.eshop.core.enums.MessagesLog.MESSAGE_LOG_FIND_ITEM_OR_CARTITEM;
import static ru.yandex.practicum.eshop.core.enums.MessagesLog.MESSAGE_LOG_ITEMS_SIZE;
import static ru.yandex.practicum.eshop.core.enums.MessagesLog.MESSAGE_LOG_SAVE_CART;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {
  public static final String BUY_REQUEST_PARAMETER = "cartId";
  public static final String ITEM_NOT_FOUND = "Товар не найден в корзине";
  public static final String ITEM_NOT_FOUND_IN_CART = "Товар не найден в корзине";
  private static final Double TOTAL_INIT = 0.00;

  public static final String PRINCIPAL_NAME = "system";
  public static final String CLIENT_NAME = "eshop";
  public static final String BEARER_PREFIX = "Bearer ";
  public static final String CART_NOT_FOUND = "Корзина не найдена для пользователя user: %s";
  private final ItemMapper itemMapper;
  private final ItemHashService itemHashService;
  private final CartRepository cartRepository;
  private final CartItemRepository cartItemRepository;
  private final WebClient paymentServiceClient;
  private final ReactiveOAuth2AuthorizedClientManager manager;

  @Override
  public Mono<Void> editCart(Long itemId, String actionRequest, String username) {
    var action = Action.getValueOf(actionRequest);

    return cartRepository.findByUsername(username)
                         .switchIfEmpty(createNewCart(username))
                         .flatMap(existingCart -> handleCartItem(existingCart, itemId, action))
                         .onErrorResume(this::handleDatabaseError);
  }

  @Override
  public Mono<CartDto> getCartItems(String username) {
    return Mono.defer(() -> {
      log.info(MESSAGE_LOG_DB_GET_REQUEST.getMessage());
      return cartRepository.findByUsername(username)
                           .switchIfEmpty(createNewCart(username))
                           .map(Cart::getId)
                           .flatMap(cartId ->
                                        getItemsFromCart(cartId)
                                            .map(items -> {
                                              log.info(MESSAGE_LOG_ITEMS_SIZE.getMessage(),
                                                       items.size());
                                              return items;
                                            })
                                            .map(items -> {
                                              double total = items.stream()
                                                                  .mapToDouble(
                                                                      item -> item.getPrice()
                                                                              * item.getCount())
                                                                  .sum();

                                              return CartDto.builder()
                                                            .id(cartId)
                                                            .items(itemMapper.toListDto(items))
                                                            .total(total)
                                                            .build();
                                            })
                                            .onErrorResume(e -> {
                                              log.error(MESSAGE_LOG_FIND_CARTITEM.getMessage(), e);
                                              return Mono.error(
                                                  new DataBaseRequestException(
                                                      MESSAGE_LOG_DB_RESPONSE_ERROR.getMessage(),
                                                      e));
                                            }));
    });
  }

  @Override
  public Mono<Long> buyItems(String username) {
    return manager.authorize(OAuth2AuthorizeRequest.withClientRegistrationId(CLIENT_NAME)
                                                   .principal(PRINCIPAL_NAME)
                                                   .build())
                  .flatMap(client -> {
                    String accessToken = client.getAccessToken().getTokenValue();

                    return cartRepository.findByUsername(username)
                                         .switchIfEmpty(Mono.error(new NoSuchElementException(
                                             String.format(CART_NOT_FOUND, username))))
                                         .map(Cart::getId)
                                         .flatMap(cartId -> paymentServiceClient
                                             .post()
                                             .uri(uriBuilder -> uriBuilder
                                                 .queryParam(BUY_REQUEST_PARAMETER, cartId)
                                                 .build())
                                             .header(HttpHeaders.AUTHORIZATION,
                                                     BEARER_PREFIX + accessToken)
                                             .retrieve()
                                             .bodyToMono(CreatePaymentResponse.class)
                                             .map(CreatePaymentResponse::getOrderId));
                  });
  }

  private Mono<Cart> createNewCart(String username) {
    var newCart = Cart.builder()
                      .username(username)
                      .total(TOTAL_INIT)
                      .build();

    return cartRepository.save(newCart);
  }

  private Mono<Void> handleCartItem(Cart existingCart, Long itemId, Action action) {
    return switch (action) {
      case PLUS -> incrementItem(itemId, existingCart);
      case MINUS -> decrementItem(itemId, existingCart);
      case DELETE -> cartItemRepository.findCartItemByCartIdAndItemId(existingCart.getId(), itemId)
                                       .map(Optional::ofNullable)
                                       .switchIfEmpty(Mono.error(
                                           new ActionException(ITEM_NOT_FOUND)))
                                       .flatMap(optionalCartItem -> optionalCartItem.map(
                                                                                        cartItemRepository::delete)
                                                                                    .orElse(
                                                                                        Mono.error(
                                                                                            new ActionException(
                                                                                                ITEM_NOT_FOUND))));
    };
  }

  private Mono<Void> handleDatabaseError(Throwable e) {
    return Mono.error(new DataBaseRequestException(
        MESSAGE_LOG_DB_RESPONSE_ERROR.getMessage(),
        e));
  }

  private Mono<Void> incrementItem(Long itemId, Cart existingCart) {
    return cartItemRepository.findCartItemByCartIdAndItemId(existingCart.getId(), itemId)
                             .map(Optional::ofNullable)
                             .switchIfEmpty(
                                 cartItemRepository.save(
                                     CartItem.builder()
                                             .cartId(existingCart.getId())
                                             .itemId(itemId)
                                             .count(1)
                                             .build()
                                 ).singleOptional()
                             )
                             .flatMap(optionalCartItem -> optionalCartItem.map(cartItem -> {
                               cartItem.setCount(cartItem.getCount() + 1);
                               return cartItemRepository.save(cartItem);
                             }).orElseGet(Mono::empty))
                             .then(itemHashService.incrementCount(itemId))
                             .then(updateCartTotal(existingCart))
                             .onErrorResume(e -> {
                               log.error(MESSAGE_LOG_ADD_ITEM_TO_CART.getMessage(), e);
                               if (e instanceof Exception) {
                                 return Mono.error(
                                     new DataBaseRequestException(
                                         MESSAGE_LOG_ADD_ITEM_TO_CART.getMessage(), e));
                               } else {
                                 return Mono.error(new DataBaseRequestException(
                                     "Произошла ошибка при обновлении корзины", e));
                               }
                             });
  }

  private Mono<Void> decrementItem(Long itemId, Cart existingCart) {
    return cartItemRepository.findCartItemByCartIdAndItemId(existingCart.getId(), itemId)
                             .map(Optional::ofNullable)
                             .switchIfEmpty(
                                 Mono.error(new ActionException(ITEM_NOT_FOUND_IN_CART)))
                             .flatMap(optionalCartItem -> optionalCartItem.map(cartItem -> {
                               if (cartItem.getCount() > 1) {
                                 cartItem.setCount(cartItem.getCount() - 1);
                                 return cartItemRepository.save(cartItem)
                                                          .then(itemHashService.decrementCount(
                                                              itemId));
                               } else {
                                 return cartItemRepository.delete(cartItem);
                               }
                             }).orElse(
                                 Mono.error(new ActionException(ITEM_NOT_FOUND_IN_CART))))
                             .then(updateCartTotal(existingCart));
  }

  private Mono<Void> updateCartTotal(Cart existingCart) {
    return calculateTotal(existingCart.getId())
        .doOnNext(existingCart::setTotal)
        .then(cartRepository.save(existingCart))
        .onErrorResume(e -> {
          log.error(MESSAGE_LOG_SAVE_CART.getMessage(), e);
          return Mono.error(
              new DataBaseRequestException(MESSAGE_LOG_DB_RESPONSE_ERROR.getMessage(), e));
        })
        .then();
  }

  private Mono<Double> calculateTotal(Long cartId) {
    return cartItemRepository.findCartItemsByCartId(cartId)
                             .flatMap(cartItem -> itemHashService.findById(cartItem.getItemId())
                                                                 .map(item -> item.getPrice()
                                                                              * cartItem.getCount()))
                             .reduce(0.0, Double::sum)
                             .onErrorResume(e -> {
                               log.error(MESSAGE_LOG_FIND_CARTITEM.getMessage(), e);
                               return Mono.error(new DataBaseRequestException(
                                   MESSAGE_LOG_DB_RESPONSE_ERROR.getMessage(), e));
                             });
  }

  private Mono<List<Item>> getItemsFromCart(Long cartId) {
    return cartItemRepository.findCartItemsByCartId(cartId)
                             .map(CartItem::getItemId)
                             .collect(Collectors.toSet())
                             .flatMap(ids -> itemHashService.findAllByIds(ids)
                                                            .collectList())
                             .onErrorResume(e -> {
                               log.error(MESSAGE_LOG_FIND_ITEM_OR_CARTITEM.getMessage(), e);
                               return Mono.error(new DataBaseRequestException(
                                   MESSAGE_LOG_DB_RESPONSE_ERROR.getMessage(), e));
                             });
  }
}