package ru.yandex.practicum.eshop.core.controller;

import ch.qos.logback.core.joran.spi.ActionException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.eshop.core.service.ItemService;

/**
 * Контроллер обрабатывает запросы относительно корзины.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class CartController {
  public static final String REDIRECT_CART = "redirect:/cart/items";
  public static final String REDIRECT_ORDERS = "redirect:/orders";
  private final ItemService itemService;

  /**
   * Обрабатывает GET-запросы на получение списка товаров в корзине.
   *
   * @param model - модель данных.
   * @return главная страница.
   */
  @GetMapping("/cart/items")
  public Mono<String> getCartItems(Model model) {
    return Mono.just(model)
               .doOnNext(m -> log.info("Получен запрос на получение списка товаров в корзине"))
               .flatMap(m -> itemService.getCartItems()
                                        .doOnNext(dto -> log.info(
                                            "Получен список товаров в корзине размером: {}",
                                            dto.items().size()))
                                        .flatMap(dto -> {
                                          if (dto.items().isEmpty()) {
                                            m.addAttribute("empty", "true");
                                          } else {
                                            m.addAttribute("items", dto.items());
                                            m.addAttribute("total", dto.total());
                                          }
                                          return Mono.just(m);
                                        })
               )
               .thenReturn("cart");
  }

  /**
   * Изменение количества товаров в корзине.
   *
   * @param itemId - id товара.
   * @param action - действие с товаром в корзине.
   * @return перенаправляет на страницу корзины.
   */
  @PostMapping("/cart/items/{id}")
  public Mono<String> updateCartItems(@PathVariable(name = "id") @NotNull Long itemId,
                                      @RequestParam(defaultValue = "") @NotBlank String action)
      throws ActionException {

    log.info("Получен запрос на изменение корзины: {} для товара id = {}", action, itemId);

    return itemService.editCart(itemId, action)
                      .then(Mono.just(REDIRECT_CART));
  }

  /**
   * Купить товары в корзине.
   *
   * @return перенаправляет на страницу заказов.
   */
  @PostMapping("/buy")
  public Mono<String> buyItems(RedirectAttributes redirectAttributes) {
    log.info("Получен запрос на покупку товаров в корзине");

    return itemService.buyItems()
                      .doOnNext(orderId -> {
                        log.info("Создан новый заказ = {}", orderId);
                        redirectAttributes.addAttribute("orderId", orderId);
                        redirectAttributes.addFlashAttribute("newOrder", true);
                      })
                      .thenReturn(REDIRECT_ORDERS);
  }
}
