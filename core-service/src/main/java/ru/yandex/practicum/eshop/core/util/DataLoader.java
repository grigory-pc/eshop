package ru.yandex.practicum.eshop.core.util;

import io.r2dbc.spi.ConnectionFactory;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.eshop.core.entity.Item;
import ru.yandex.practicum.eshop.core.entity.User;
import ru.yandex.practicum.eshop.core.repository.UserRepository;

/**
 * Класс для загрузки данных в БД при запуске приложения.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class DataLoader implements ApplicationRunner {
  private static final String ITEM_SHORTS_IMG_PATH = "images/shorts.jpg";
  private static final String ITEM_SUNGLASSES_IMG_PATH = "images/sunglasses.jpg";
  private static final String ITEM_TSHIRT_IMG_PATH = "images/tshirt.jpg";
  public static final String PATH_INIT_SQL_SCRIPT = "classpath:db/migration/V1__init.sql";
  public static final String REDIS_KEY_ITEM = "item";
  public static final String REDIS_ITEM_FIELD_TITLE = "title";
  public static final String REDIS_ITEM_FIELD_IMG_PATH = "imgPath";
  public static final String REDIS_ITEM_FIELD_DESCRIPTION = "description";
  public static final String REDIS_ITEM_FIELD_PRICE = "price";
  public static final String REDIS_ITEM_FIELD_COUNT = "count";
  private final ConnectionFactory connectionFactory;
  private final ResourceLoader resourceLoader;
  private final RedisTemplate<String, Object> redisTemplate;
  private final UserRepository userRepository;
  private final PasswordEncoder passwordEncoder;

  @Override
  public void run(ApplicationArguments args) {
    try {
      executeSqlScript(PATH_INIT_SQL_SCRIPT).subscribe();
    } catch (IOException e) {
      log.error("Не удалось запустить стартовый SQL-скрипт");
      throw new RuntimeException(e);
    }
    addItems();
    addUsers();
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

  private void addItems() {
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

    redisTemplate.opsForHash().putAll(REDIS_KEY_ITEM + ":1",
                                      Map.of(
                                          REDIS_ITEM_FIELD_TITLE, shorts.getTitle(),
                                          REDIS_ITEM_FIELD_IMG_PATH, shorts.getImgPath(),
                                          REDIS_ITEM_FIELD_DESCRIPTION, shorts.getDescription(),
                                          REDIS_ITEM_FIELD_PRICE, shorts.getPrice(),
                                          REDIS_ITEM_FIELD_COUNT, shorts.getCount()
                                      )
    );

    redisTemplate.opsForHash().putAll(REDIS_KEY_ITEM + ":2",
                                      Map.of(
                                          REDIS_ITEM_FIELD_TITLE, sunglasses.getTitle(),
                                          REDIS_ITEM_FIELD_IMG_PATH, sunglasses.getImgPath(),
                                          REDIS_ITEM_FIELD_DESCRIPTION, sunglasses.getDescription(),
                                          REDIS_ITEM_FIELD_PRICE, sunglasses.getPrice(),
                                          REDIS_ITEM_FIELD_COUNT, sunglasses.getCount()
                                      )
    );

    redisTemplate.opsForHash().putAll(REDIS_KEY_ITEM + ":3",
                                      Map.of(
                                          REDIS_ITEM_FIELD_TITLE, tShirt.getTitle(),
                                          REDIS_ITEM_FIELD_IMG_PATH, tShirt.getImgPath(),
                                          REDIS_ITEM_FIELD_DESCRIPTION, tShirt.getDescription(),
                                          REDIS_ITEM_FIELD_PRICE, tShirt.getPrice(),
                                          REDIS_ITEM_FIELD_COUNT, tShirt.getCount()
                                      )
    );

    log.info("Товары добавлены в кэш");
  }

  private void addUsers() {
    User user1 = User.builder()
                     .username("user1")
                     .password(passwordEncoder.encode("password"))
                     .role("USER")
                     .build();
    User user2 = User.builder()
                     .username("user2")
                     .password(passwordEncoder.encode("password"))
                     .role("USER")
                     .build();

    List<User> users = List.of(user1, user2);

    userRepository.deleteAll()
                  .thenMany(Flux.fromIterable(users))
                  .flatMap(userRepository::save)
                  .subscribe();

    log.info("Пользователи добавлены в базу");
  }
}