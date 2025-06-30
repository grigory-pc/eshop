package ru.yandex.practicum.eshop.core.controller;

import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.eshop.core.service.OrderService;

/**
 * Контроллер обрабатывает запросы относительно заказов.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/orders")

public class OrderController {
  private final OrderService orderService;

  /**
   * Обрабатывает GET-запросы на получение списка заказов.
   *
   * @param model - модель данных.
   * @return страница заказов.
   */
  @GetMapping
  public Mono<String> getOrders(Model model) {
    return Mono.just(model)
               .doOnNext(m -> log.info("Получен запрос на получение списка заказов"))
               .flatMap(m -> orderService.getOrders()
                                         .doOnNext(order -> log.info("Получен список заказов"))
                                         .doOnComplete(
                                            () -> log.info("Завершено получение списка заказов"))
                                         .collectList()
                                         .doOnNext(
                                            list -> log.info("Получен список заказов размером: {}",
                                                             list.size()))
                                         .flatMap(orders -> {
                                          m.addAttribute("orders", orders);
                                          return Mono.just(m);
                                        })
               )
               .thenReturn("orders");
  }

  /**
   * Обрабатывает GET-запросы на получение карточки заказа.
   *
   * @param id       - id заказа.
   * @param newOrder - флаг нового заказа.
   * @param model    - модель данных.
   * @return страница заказа.
   */
  @GetMapping("/{id}")
  public Mono<String> getOrderById(@PathVariable @NotNull Long id,
                                   @RequestParam(defaultValue = "false") Boolean newOrder,
                                   Model model) {
    return Mono.just(model)
               .doOnNext(
                   m -> log.info("Получен запрос на получение карточки заказа для id = {}", id))
               .flatMap(m -> orderService.getOrderItems(id)
                                         .doOnNext(order -> log.info(
                                            "Из базы данных получен объект товара с id: {}", id))
                                         .flatMap(order -> {
                                          m.addAttribute("order", order);
                                          m.addAttribute("newOrder", newOrder);
                                          return Mono.just(m);
                                        })
               )
               .onErrorResume(e -> {
                 log.error("Ошибка при получении заказа для id = {}", id, e);
                 return Mono.error(new ResponseStatusException(
                     HttpStatus.INTERNAL_SERVER_ERROR,
                     "Ошибка получения заказа"
                 ));
               })
               .thenReturn("order");
  }
}
