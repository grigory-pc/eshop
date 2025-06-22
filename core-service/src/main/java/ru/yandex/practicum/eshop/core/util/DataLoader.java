package ru.yandex.practicum.eshop.core.util;

import io.r2dbc.spi.ConnectionFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ResourceLoader;
import org.springframework.r2dbc.core.DatabaseClient;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.eshop.core.entity.Cart;
import ru.yandex.practicum.eshop.core.entity.Item;
import ru.yandex.practicum.eshop.core.repository.CartRepository;
import ru.yandex.practicum.eshop.core.repository.ItemRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Класс для загрузки данных в БД при запуске приложения.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements ApplicationRunner {
  private static final String ITEM_SHORTS_IMG_PATH = "/images/shorts.jpg";
  private static final String ITEM_SUNGLASSES_IMG_PATH = "/images/sunglasses.jpg";
  private static final String ITEM_TSHIRT_IMG_PATH = "/images/tshirt.jpg";
  private static final Double TOTAL_INIT = 0.00;
  public static final String PATH_INIT_SQL_SCRIPT = "classpath:db/migration/V1__init.sql";
  private final ConnectionFactory connectionFactory;
  private final ResourceLoader resourceLoader;
  private final ItemRepository itemRepository;
  private final CartRepository cartRepository;

  @Override
  public void run(ApplicationArguments args) {
    try {
      executeSqlScript(PATH_INIT_SQL_SCRIPT).subscribe();
    } catch (IOException e) {
      log.error("Не удалось запустить стартовый SQL-скрипт");
      throw new RuntimeException(e);
    }

    Item shorts = Item.builder()
                      .title("Шорты")
                      .imgPath(ITEM_SHORTS_IMG_PATH)
                      .description("Летние шорты карго")
                      .price(1399.99)
                      .count(0)
                      .build();

    Item sunglasses = Item.builder()
                          .title("Солцезащитные очки")
                          .imgPath(ITEM_SUNGLASSES_IMG_PATH)
                          .description("Солцезащитные очки с UV и поляризацией")
                          .price(3410.00)
                          .count(0)
                          .build();

    Item tShirt = Item.builder()
                      .title("Футболка")
                      .imgPath(ITEM_TSHIRT_IMG_PATH)
                      .description("Футболка с рукавом")
                      .price(567.99)
                      .count(0)
                      .build();

    Cart newCart = Cart.builder()
                       .total(TOTAL_INIT)
                       .build();
    cartRepository.save(newCart)
                  .doOnNext(cart -> System.out.println(
                      "Сохранена корзина с ID: " + cart.getId()))
                  .subscribe(System.out::println);

    itemRepository.saveAll(List.of(shorts, sunglasses, tShirt))
                  .doOnNext(item -> System.out.println(
                      "Сохранены товары "))
                  .subscribe(System.out::println);
  }

  private Mono<Void> executeSqlScript(String scriptPath) throws IOException {
    Path path = resourceLoader.getResource(scriptPath).getFile().toPath();
    String sql = Files.readString(path);

    return DatabaseClient.create(connectionFactory)
                         .sql(sql)
                         .fetch()
                         .rowsUpdated()
                         .then();
  }
}