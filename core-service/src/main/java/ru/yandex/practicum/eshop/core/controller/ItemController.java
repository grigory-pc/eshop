package ru.yandex.practicum.eshop.core.controller;

import ch.qos.logback.core.joran.spi.ActionException;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.eshop.core.service.ItemService;

/**
 * Контроллер обрабатывает запросы на относительно товаров.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
@RequestMapping("/items")
public class ItemController {
  public static final String REDIRECT_ITEMS = "redirect:/items/";
  private final ItemService itemService;

  /**
   * Обрабатывает GET-запросы на получение карточки товара по id.
   *
   * @param id    - id товара.
   * @param model - модель данных.
   * @return страница карточки товара.
   */
  @GetMapping("/{id}")
  public Mono<String> getItemById(@PathVariable @NotNull Long id, Model model) {
    log.info("Получен запрос на получение карточки товара для id = {}", id);

    return itemService.getItem(id)
                      .doOnNext(item -> {
                        log.info("Из базы данных получен объект товара с id: {}", id);
                        model.addAttribute("item", item);
                      })
                      .thenReturn("item");
  }

  /**
   * Изменение количества товаров в корзине из карточки товара.
   *
   * @param id     - id товара.
   * @param action - действие с товаром в корзине.
   * @return перенаправляет на страницу карточки товара.
   */
  @PostMapping("/{id}")
  public Mono<String> updateItems(@PathVariable(name = "id") Long id,
                                  @RequestParam(defaultValue = "") String action)
      throws ActionException {
    log.info("Получен запрос из карточки товара на изменение корзины: {} для товара id = {}",
             action, id);


    return itemService.editCart(id, action)
                      .then(Mono.just(REDIRECT_ITEMS + id));
  }
}