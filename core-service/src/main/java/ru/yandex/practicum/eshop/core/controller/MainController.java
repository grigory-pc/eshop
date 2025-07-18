package ru.yandex.practicum.eshop.core.controller;

import ch.qos.logback.core.joran.spi.ActionException;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.security.Principal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.reactive.result.view.RedirectView;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.eshop.core.dto.PagingDto;
import ru.yandex.practicum.eshop.core.enums.Sorting;
import ru.yandex.practicum.eshop.core.service.CartService;
import ru.yandex.practicum.eshop.core.service.ItemService;

/**
 * Контроллер обрабатывает запросы на главной странице витрины магазина.
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class MainController {
  public static final String REDIRECT_MAIN = "redirect:/main/items";
  private final ItemService itemService;
  private final CartService cartService;

  /**
   * Перенаправление запросов с "/" на "/main/items".
   *
   * @return redirect /main/items.
   */
  @GetMapping("/")
  public RedirectView redirectToMainItems() {
    return new RedirectView("/main/items");
  }

  /**
   * Обрабатывает GET-запросы на получение списка товаров для главной страницы.
   *
   * @param pageNumber - номер текущей страницы (по умолчанию, 1)
   * @param pageSize   - максимальное число товаров на странице (по умолчанию, 10)
   * @param search     - строка с поисков по названию/описанию товара (по умолчанию, пустая строка -
   *                   все товары)
   * @param sort       - сортировка перечисление NO, ALPHA, PRICE (по умолчанию, NO - не
   *                   использовать сортировку).
   * @param model      - модель данных.
   * @return главная страница.
   */
  @GetMapping("/main/items")
  public Mono<String> getItems(@RequestParam(defaultValue = "") String search,
                               @RequestParam(defaultValue = "NO") Sorting sort,
                               @RequestParam(defaultValue = "10") int pageSize,
                               @RequestParam(defaultValue = "0") int pageNumber,
                               Model model) {

    log.info(
        "Получен запрос на получение списка товаров для главной страницы. pageNumber={} pageSize={} sort={}",
        pageNumber, pageSize, sort);

    return itemService.getItems(search, sort, pageNumber, pageSize)
                      .flatMap(page -> {
                        model.addAttribute("items", page.getContent());
                        model.addAttribute("search", search);
                        model.addAttribute("paging", new PagingDto(
                            pageNumber + 1,
                            pageSize,
                            page.hasPrevious(),
                            page.hasNext()
                        ));
                        return Mono.just("main");
                      })
                      .onErrorResume(e -> {
                        log.error("Ошибка при получении списка товаров", e);
                        return Mono.just("error");
                      });
  }

  /**
   * Изменение количества товаров в корзине на главной странице.
   *
   * @param itemId - id товара.
   * @param action - действие с товаром в корзине.
   * @param principal - данные пользователя.
   *
   * @return перенаправляет на главную страницу.
   */
  @PostMapping("/main/items/{id}/{action}")
  public Mono<String> updateMainCartItems(@PathVariable(name = "id") @NotNull Long itemId,
                                          @PathVariable(name = "action") @NotBlank String action,
                                          Principal principal)
      throws ActionException {

    log.info("Получен запрос на изменение корзины: {} для товара id = {}", action, itemId);

    return cartService.editCart(itemId, action, principal.getName())
                      .then(Mono.just(REDIRECT_MAIN));
  }
}