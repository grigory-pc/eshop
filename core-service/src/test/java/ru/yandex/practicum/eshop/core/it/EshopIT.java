//package ru.yandex.practicum.eshop.core.it;
//
//import ru.yandex.practicum.eshop.core.entity.CartItem;
//import ru.yandex.practicum.eshop.core.entity.Orders;
//import ru.yandex.practicum.eshop.core.utils.Data;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Paths;
//
//
//import io.r2dbc.spi.ConnectionFactory;
//import org.junit.jupiter.api.AfterEach;
//import org.junit.jupiter.api.BeforeAll;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.core.io.Resource;
//import org.springframework.core.io.ResourceLoader;
//import org.springframework.r2dbc.core.DatabaseClient;
//import org.springframework.test.context.ActiveProfiles;
//import org.springframework.test.web.reactive.server.WebTestClient;
//import reactor.core.publisher.Flux;
//import reactor.core.publisher.Mono;
//
//@ActiveProfiles("test")
//@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
//@AutoConfigureWebTestClient
//class EshopIT {
//  private static final Long ITEM_T_SHIRT_ID = 1L;
//  private static final Long CART_ID = 1L;
//  private static final Double TOTAL_INIT = 0.00;
//
//  @Autowired
//  private WebTestClient webTestClient;
//  @Autowired
//  private DatabaseClient databaseClient;
//
//  @BeforeAll
//  public static void initTables(@Autowired ConnectionFactory connectionFactory,
//                                @Autowired ResourceLoader resourceLoader) throws IOException {
//    Resource resource = resourceLoader.getResource("classpath:V1__initial_schema.sql");
//    String script = Files.readString(Paths.get(resource.getFile().getPath()));
//
//    Flux.from(DatabaseClient.create(connectionFactory)
//                            .sql(script)
//                            .then())
//        .subscribe(result -> {
//        }, error -> {
//          throw new RuntimeException("Ошибка инициализации базы данных", error);
//        });
//  }
//
//
//  @AfterEach
//  void flushData() {
//    String updateCartSql = "UPDATE carts SET total = $1 WHERE id = $2";
//    String updateItemsSql = "UPDATE items SET count = $1 WHERE id = $2";
//
//    databaseClient.sql(updateCartSql)
//                  .bind("$1", TOTAL_INIT)
//                  .bind("$2", CART_ID)
//                  .fetch()
//                  .rowsUpdated()
//                  .subscribe();
//
//    databaseClient.sql(updateItemsSql)
//                  .bind("$1", 0)
//                  .bind("$2", ITEM_T_SHIRT_ID)
//                  .fetch()
//                  .rowsUpdated()
//                  .subscribe();
//
//    databaseClient.sql("DELETE FROM cart_item").fetch().rowsUpdated().subscribe();
//    databaseClient.sql("DELETE FROM orders").fetch().rowsUpdated().subscribe();
//    databaseClient.sql("DELETE FROM order_item").fetch().rowsUpdated().subscribe();
//  }
//
//  @Test
//  @DisplayName("Позитивный тест - проверяем перенаправление на url = /main/items")
//  void redirectToMainItems() {
//    String url = "/";
//    String expectedRedirectUrl = "/main/items";
//
//    webTestClient.get()
//                 .uri(url)
//                 .exchange()
//                 .expectStatus().is3xxRedirection()
//                 .expectHeader().valueMatches("Location", ".*" + expectedRedirectUrl + ".*");
//  }
//
//  @Test
//  @DisplayName(
//      "Позитивный тест - проверяем получение списка товаров с пагинацией и сортировкой из базы данных")
//  void positiveTest_shouldGetItems() {
//    String url = "/main/items";
//    String pageNumber = "0";
//    String pageSize = "10";
//    String search = "";
//    String sort = "ALPHA";
//
//    webTestClient.get()
//                 .uri(uriBuilder -> uriBuilder
//                     .path(url)
//                     .queryParam("search", search)
//                     .queryParam("sort", sort)
//                     .queryParam("pageSize", pageSize)
//                     .queryParam("pageNumber", pageNumber)
//                     .build())
//                 .exchange()
//                 .expectStatus().isOk()
//                 .expectHeader().contentType("text/html;charset=UTF-8");
//  }
//
//  @Test
//  @DisplayName("Позитивный тест - проверяем получение страницы корзины с товарами")
//  void positiveTest_shouldGetCartItems() {
//    String url = "/cart/items";
//
//    insertCartItemWithOneItem();
//
//    webTestClient.get()
//                 .uri(url)
//                 .exchange()
//                 .expectStatus().isOk()
//                 .expectHeader().contentType("text/html;charset=UTF-8");
//  }
//
//  @Test
//  @DisplayName("Позитивный тест - проверяем получение страницы карточки товара")
//  void positiveTest_shouldGetItemById() {
//    String url = "/items";
//
//    webTestClient.get()
//                 .uri(url + "/" + ITEM_T_SHIRT_ID)
//                 .exchange()
//                 .expectStatus().isOk()
//                 .expectHeader().contentType("text/html;charset=UTF-8");
//  }
//
//  @Test
//  @DisplayName("Позитивный тест - получение заказа по ID")
//  void positiveTest_shouldGetOrderById() {
//    String url = "/orders";
//
//    insertOrder();
//
//    webTestClient.get()
//                 .uri(url)
//                 .exchange()
//                 .expectStatus().isOk()
//                 .expectHeader().contentType("text/html;charset=UTF-8");
//  }
//
//  private Mono<Void> insertCartItemWithOneItem() {
//    String sqlRequest = "INSERT INTO cart_item (cart_id, item_id, count) VALUES ($1, $2, $3)";
//    CartItem cartItem = Data.getCartItem();
//
//    return databaseClient
//        .sql(sqlRequest)
//        .bind("$1", cartItem.getCartId())
//        .bind("$2", cartItem.getItemId())
//        .bind("$3", cartItem.getCount())
//        .fetch()
//        .rowsUpdated()
//        .flatMap(rows -> {
//          if (rows > 0) {
//            return Mono.empty();
//          } else {
//            return Mono.error(new RuntimeException("Ошибка при вставке записи"));
//          }
//        });
//  }
//
//  private Mono<Void> insertOrder() {
//    Orders order = Data.getOrder();
//    String sqlOrder = "INSERT INTO orders (id, total_sum)VALUES ($1, $2)";
//
//
//    return databaseClient
//        .sql(sqlOrder)
//        .bind("$1", order.getId())
//        .bind("$2", order.getTotalSum())
//        .fetch()
//        .rowsUpdated()
//        .flatMap(rows -> {
//          if (rows > 0) {
//            return Mono.empty();
//          } else {
//            return Mono.error(new RuntimeException("Ошибка при вставке записи"));
//          }
//        });
//  }
//}