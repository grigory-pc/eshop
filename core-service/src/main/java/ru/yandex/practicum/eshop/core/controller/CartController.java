package ru.yandex.practicum.eshop.core.controller;

import ch.qos.logback.core.joran.spi.ActionException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.net.URI;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.eshop.core.service.CartService;

/**
 * Контроллер обрабатывает запросы относительно корзины.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class CartController {
  public static final String REDIRECT_CART = "redirect:/cart/items";
  public static final String REDIRECT_ORDERS = "redirect:/orders";
  private final CartService cartService;

  /**
   * Обрабатывает GET-запросы на получение списка товаров в корзине.
   *
   * @param model - модель данных.
   * @param principal - данные пользователя.
   * @return главная страница.
   */
  @GetMapping("/cart/items")
  public Mono<String> getCartItems(Model model, Principal principal) {
    return Mono.just(model)
               .doOnNext(m -> log.info("Получен запрос на получение списка товаров в корзине"))
               .flatMap(m -> cartService.getCartItems(principal.getName())
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
   * @param principal - данные пользователя.
   *
   * @return перенаправляет на страницу корзины.
   */
  @PostMapping("/cart/items/{id}/{action}")
  public Mono<String> updateCartItems(@PathVariable(name = "id") @NotNull Long itemId,
                                      @PathVariable(name = "action") @NotBlank String action,
                                      Principal principal)
      throws ActionException {

    log.info("Получен запрос на изменение корзины: {} для товара id = {}", action, itemId);

    return cartService.editCart(itemId, action, principal.getName())
                      .then(Mono.just(REDIRECT_CART));
  }

  /**
   * Покупка товаров в корзине.
   * @param principal - данные пользователя.
   *
   * @return перенаправляет на страницу заказов.
   */
  @PostMapping("/buy")
  public Mono<ResponseEntity<String>> buyItems(Principal principal) {
    log.info("Получен запрос на покупку товаров в корзине");

    return cartService.buyItems(principal.getName())
                      .flatMap(orderId -> {
                        URI redirectUri = URI.create(
                            REDIRECT_ORDERS + "?orderId=" + orderId + "&newOrder=true");
                        log.info("Создан новый заказ = {}", orderId);

                        return Mono.just(
                            ResponseEntity
                                .status(HttpStatus.FOUND)
                                .location(redirectUri)
                                .build()
                        );
                      });
  }
}
