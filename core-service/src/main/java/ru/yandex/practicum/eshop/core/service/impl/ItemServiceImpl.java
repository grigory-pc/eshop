package ru.yandex.practicum.eshop.core.service.impl;

import java.util.stream.Collectors;
import org.springframework.web.reactive.function.client.WebClient;
import ru.yandex.practicum.eshop.core.dto.CreatePaymentResponse;
import ru.yandex.practicum.eshop.core.enums.Action;
import ru.yandex.practicum.eshop.core.exceptions.ActionException;
import ru.yandex.practicum.eshop.core.exceptions.DataBaseRequestException;
import ru.yandex.practicum.eshop.core.exceptions.SortingException;
import ru.yandex.practicum.eshop.core.mappers.ItemMapper;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.eshop.core.dto.CartDto;
import ru.yandex.practicum.eshop.core.dto.ItemDto;
import ru.yandex.practicum.eshop.core.dto.OrderDto;
import ru.yandex.practicum.eshop.core.entity.Cart;
import ru.yandex.practicum.eshop.core.entity.CartItem;
import ru.yandex.practicum.eshop.core.entity.Item;
import ru.yandex.practicum.eshop.core.entity.OrderItem;
import ru.yandex.practicum.eshop.core.enums.Sorting;
import ru.yandex.practicum.eshop.core.exceptions.ItemNotFoundException;
import ru.yandex.practicum.eshop.core.repository.CartItemRepository;
import ru.yandex.practicum.eshop.core.repository.CartRepository;
import ru.yandex.practicum.eshop.core.repository.OrderItemRepository;
import ru.yandex.practicum.eshop.core.repository.OrderRepository;
import ru.yandex.practicum.eshop.core.service.ItemHashService;
import ru.yandex.practicum.eshop.core.service.ItemService;

import static ru.yandex.practicum.eshop.core.enums.MessagesLog.MESSAGE_LOG_ADD_ITEM_TO_CART;
import static ru.yandex.practicum.eshop.core.enums.MessagesLog.MESSAGE_LOG_DB_GET_REQUEST;
import static ru.yandex.practicum.eshop.core.enums.MessagesLog.MESSAGE_LOG_DB_RESPONSE_ERROR;
import static ru.yandex.practicum.eshop.core.enums.MessagesLog.MESSAGE_LOG_FIND_CARTITEM;
import static ru.yandex.practicum.eshop.core.enums.MessagesLog.MESSAGE_LOG_FIND_ITEM;
import static ru.yandex.practicum.eshop.core.enums.MessagesLog.MESSAGE_LOG_FIND_ITEM_OR_CARTITEM;
import static ru.yandex.practicum.eshop.core.enums.MessagesLog.MESSAGE_LOG_ITEMS_SIZE;
import static ru.yandex.practicum.eshop.core.enums.MessagesLog.MESSAGE_LOG_FIND_ALL_ITEMS;
import static ru.yandex.practicum.eshop.core.enums.MessagesLog.MESSAGE_LOG_SAVE_CART;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
  private static final Long CART_ID = 1L;
  public static final String BUY_REQUEST_PARAMETER = "cartId";
  public static final String ITEM_NOT_FOUND = "Товар не найден в корзине";
  public static final String ITEM_NOT_FOUND_IN_CART = "Товар не найден в корзине";
  private final ItemMapper itemMapper;
  private final ItemHashService itemHashService;
  private final CartRepository cartRepository;
  private final OrderRepository orderRepository;
  private final CartItemRepository cartItemRepository;
  private final OrderItemRepository orderItemRepository;
  private final WebClient paymentServiceClient;


  @Override
  public Mono<PageImpl<ItemDto>> getItems(String search, Sorting sort, int pageNumber,
                                          int pageSize) {
    Pageable pageableItems = getPageableItemsRequest(sort, pageNumber, pageSize);
    log.info(MESSAGE_LOG_DB_GET_REQUEST.getMessage());

    if (search.isEmpty()) {
      return itemHashService.findAll(pageableItems)
                            .collectList()
                            .flatMap(items -> {
                             long total = items.size();
                             return Mono.just(
                                 new PageImpl<>(itemMapper.toListDto(items),
                                                pageableItems,
                                                total));
                           })
                            .onErrorResume(e -> {
                             log.error(MESSAGE_LOG_FIND_ALL_ITEMS.getMessage(), e);
                             return Mono.error(new DataBaseRequestException(
                                 MESSAGE_LOG_DB_RESPONSE_ERROR.getMessage(), e));
                           });

    } else {
      return itemHashService.findByTitleContainingIgnoreCase(search, pageableItems)
                            .skip((long) pageNumber * pageSize)
                            .limitRate(pageSize)
                            .collectList()
                            .flatMap(items -> {
                             long total = items.size();
                             return Mono.just(
                                 new PageImpl<>(itemMapper.toListDto(items),
                                                pageableItems,
                                                total));
                           })
                            .onErrorResume(e -> {
                             log.error(MESSAGE_LOG_FIND_ALL_ITEMS.getMessage(), e);
                             return Mono.error(new DataBaseRequestException(
                                 MESSAGE_LOG_DB_RESPONSE_ERROR.getMessage(), e));
                           });
    }
  }

  @Override
  public Mono<Void> editCart(Long itemId, String actionRequest) {
    Action action = Action.getValueOf(actionRequest);

    return cartRepository.findById(CART_ID)
                         .flatMap(existingCart -> handleCartItem(existingCart, itemId, action))
                         .onErrorResume(this::handleDatabaseError);
  }

  @Override
  public Mono<CartDto> getCartItems() {
    return Mono.defer(() -> {
      log.info(MESSAGE_LOG_DB_GET_REQUEST.getMessage());

      return getItemsFromCart()
          .map(items -> {
            log.info(MESSAGE_LOG_ITEMS_SIZE.getMessage(), items.size());
            return items;
          })
          .map(items -> {
            double total = items.stream()
                                .mapToDouble(item -> item.getPrice() * item.getCount())
                                .sum();

            return CartDto.builder()
                          .id(CART_ID)
                          .items(itemMapper.toListDto(items))
                          .total(total)
                          .build();
          })
          .onErrorResume(e -> {
            log.error(MESSAGE_LOG_DB_RESPONSE_ERROR.getMessage(), e);
            return Mono.error(
                new DataBaseRequestException(MESSAGE_LOG_DB_RESPONSE_ERROR.getMessage(), e));
          });
    });
  }

  @Override
  public Mono<ItemDto> getItem(Long id) {
    return Mono.defer(() -> {
      log.info(MESSAGE_LOG_DB_GET_REQUEST.getMessage());

      return itemHashService.findById(id)
                            .map(itemMapper::toDto)
                            .switchIfEmpty(Mono.error(new ItemNotFoundException("Товар не найден")))
                            .onErrorResume(e -> {
                             log.error(MESSAGE_LOG_FIND_ITEM.getMessage(), e);
                             return Mono.error(new DataBaseRequestException(
                                 MESSAGE_LOG_DB_RESPONSE_ERROR.getMessage(), e));
                           });
    });
  }

  @Override
  public Mono<Long> buyItems() {
    return paymentServiceClient
        .post()
        .uri(uriBuilder -> uriBuilder
            .queryParam(BUY_REQUEST_PARAMETER, CART_ID)
            .build())
        .retrieve()
        .bodyToMono(CreatePaymentResponse.class)
        .map(CreatePaymentResponse::getOrderId);
  }

  @Override
  public Flux<OrderDto> getOrders() {
    return orderRepository.findAll()
                          .flatMap(orders -> getOrderItems(orders.getId()));
  }

  @Override
  public Mono<OrderDto> getOrderItems(Long id) {
    return getItemsCountFromOrderItems(id)
        .flatMapMany(this::getItemsFromOrder)
        .collectList()
        .map(items -> {
          double total = items.stream()
                              .mapToDouble(item -> item.getPrice() * item.getCount())
                              .sum();

          return OrderDto.builder()
                         .id(id)
                         .items(itemMapper.toListDto(items))
                         .totalSum(total)
                         .build();
        });
  }

  private Mono<Void> handleCartItem(Cart existingCart, Long itemId, Action action) {
    return switch (action) {
      case PLUS -> incrementItem(itemId, existingCart);
      case MINUS -> decrementItem(itemId, existingCart);
      case DELETE -> cartItemRepository.findCartItemByCartIdAndItemId(CART_ID, itemId)
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

  private Flux<Item> getItemsFromOrder(Map<Long, Integer> orderItemCounts) {
    return itemHashService.findAllByIds(orderItemCounts.keySet())
                          .map(item -> {
                           Integer countFromOrder = orderItemCounts.get(item.getId());
                           if (countFromOrder != null) {
                             item.setCount(countFromOrder);
                           }
                           return item;
                         });
  }

  private Mono<Map<Long, Integer>> getItemsCountFromOrderItems(Long id) {
    return orderItemRepository.findOrderItemsByOrderId(id)
                              .collectMap(OrderItem::getItemId, OrderItem::getCount);
  }

  private static Pageable getPageableItemsRequest(Sorting sort, int pageNumber, int pageSize) {
    return switch (sort) {
      case ALPHA -> PageRequest.of(pageNumber, pageSize, Sort.by("title").ascending());
      case PRICE -> PageRequest.of(pageNumber, pageSize, Sort.by("price").ascending());
      case NO -> PageRequest.of(pageNumber, pageSize);
      default -> throw new SortingException("Некорректный тип сортировки: " + sort);
    };
  }

  private Mono<Void> incrementItem(Long itemId, Cart existingCart) {
    return cartItemRepository.findCartItemByCartIdAndItemId(CART_ID, itemId)
                             .map(Optional::ofNullable)
                             .switchIfEmpty(
                                 cartItemRepository.save(
                                     CartItem.builder()
                                             .cartId(CART_ID)
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
    return cartItemRepository.findCartItemByCartIdAndItemId(CART_ID, itemId)
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
    return calculateTotal()
        .doOnNext(existingCart::setTotal)
        .then(cartRepository.save(existingCart))
        .onErrorResume(e -> {
          log.error(MESSAGE_LOG_SAVE_CART.getMessage(), e);
          return Mono.error(
              new DataBaseRequestException(MESSAGE_LOG_DB_RESPONSE_ERROR.getMessage(), e));
        })
        .then();
  }

  private Mono<Double> calculateTotal() {
    return cartItemRepository.findCartItemsByCartId(CART_ID)
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

  private Mono<List<Item>> getItemsFromCart() {
    return cartItemRepository.findCartItemsByCartId(CART_ID)
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