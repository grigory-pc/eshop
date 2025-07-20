package ru.yandex.practicum.eshop.core.service.impl;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.eshop.core.dto.OrderDto;
import ru.yandex.practicum.eshop.core.entity.Item;
import ru.yandex.practicum.eshop.core.entity.OrderItem;
import ru.yandex.practicum.eshop.core.mappers.ItemMapper;
import ru.yandex.practicum.eshop.core.repository.OrderItemRepository;
import ru.yandex.practicum.eshop.core.repository.OrderRepository;
import ru.yandex.practicum.eshop.core.repository.ItemHashService;
import ru.yandex.practicum.eshop.core.service.OrderService;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {
  private final ItemMapper itemMapper;
  private final ItemHashService itemHashService;
  private final OrderRepository orderRepository;
  private final OrderItemRepository orderItemRepository;

  @Override
  public Flux<OrderDto> getOrders(String username) {
    return orderRepository.findByUsername(username)
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
}