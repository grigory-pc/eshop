package ru.yandex.practicum.eshop.core.service.impl;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;
import ru.yandex.practicum.eshop.core.entity.Item;
import ru.yandex.practicum.eshop.core.exceptions.DataBaseRequestException;
import ru.yandex.practicum.eshop.core.exceptions.ItemNotFoundException;
import ru.yandex.practicum.eshop.core.service.ItemHashService;

import static ru.yandex.practicum.eshop.core.enums.MessagesLog.MESSAGE_LOG_DB_RESPONSE_ERROR;
import static ru.yandex.practicum.eshop.core.enums.MessagesLog.MESSAGE_LOG_FIND_ALL_ITEMS;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemHashServiceImpl implements ItemHashService {
  private static final String ITEM_KEY_PREFIX = "item:";
  public static final String REDIS_ITEM_FIELD_TITLE = "title";
  public static final String REDIS_ITEM_FIELD_IMG_PATH = "imgPath";
  public static final String REDIS_ITEM_FIELD_DESCRIPTION = "description";
  public static final String REDIS_ITEM_FIELD_PRICE = "price";
  public static final String REDIS_ITEM_FIELD_COUNT = "count";
  private final RedisTemplate<String, Object> redisTemplate;

  @Override
  public Flux<Item> findAll(Pageable pageableRequest) {
    return Flux.fromIterable(redisTemplate.keys(ITEM_KEY_PREFIX + "*"))
               .flatMap(key -> {
                 Map<Object, Object> rawEntries = redisTemplate.opsForHash().entries(key);
                 Map<String, Object> entries = new HashMap<>();

                 for (Map.Entry<Object, Object> entry : rawEntries.entrySet()) {
                   if (entry.getKey() instanceof String) {
                     entries.put((String) entry.getKey(), entry.getValue());
                   }
                 }

                 return Flux.just(convertToItem(key, entries));
               })
               .skip(pageableRequest.getOffset())
               .take(pageableRequest.getPageSize())
               .onErrorResume(e -> {
                 log.error(MESSAGE_LOG_FIND_ALL_ITEMS.getMessage(), e);
                 return Mono.error(new DataBaseRequestException(
                     MESSAGE_LOG_DB_RESPONSE_ERROR.getMessage(), e));
               });
  }

  @Override
  public Flux<Item> findByTitleContainingIgnoreCase(String search, Pageable pageableRequest) {
    return Flux.fromIterable(redisTemplate.keys(ITEM_KEY_PREFIX + "*"))
               .flatMap(key -> {
                 Map<Object, Object> rawEntries = redisTemplate.opsForHash().entries(key);
                 Map<String, Object> entries = rawEntries.entrySet()
                                                         .stream()
                                                         .filter(e -> e.getKey() instanceof String)
                                                         .collect(Collectors.toMap(
                                                             e -> (String) e.getKey(),
                                                             Map.Entry::getValue
                                                         ));

                 return Flux.just(convertToItem(key, entries));
               })
               .filter(item -> {
                 if (search == null || search.isEmpty()) {
                   return true;
                 }
                 String searchLower = search.toLowerCase();
                 return item.getTitle() != null
                        && item.getTitle().toLowerCase().contains(searchLower);
               })
               .skip(pageableRequest.getOffset())
               .take(pageableRequest.getPageSize())
               .onErrorResume(e -> {
                 log.error(MESSAGE_LOG_FIND_ALL_ITEMS.getMessage(), e);
                 return Mono.error(new DataBaseRequestException(
                     MESSAGE_LOG_DB_RESPONSE_ERROR.getMessage(), e));
               });
  }

  @Override
  public Mono<Void> incrementCount(Long id) {
    return findById(id)
        .flatMap(item -> {
          String key = ITEM_KEY_PREFIX + id;
          redisTemplate.opsForHash().put(key, REDIS_ITEM_FIELD_COUNT, item.getCount() + 1);
          return Mono.empty();
        });
  }

  @Override
  public Mono<Void> decrementCount(Long id) {
    return findById(id)
        .flatMap(item -> {
          Integer currentCount = item.getCount();

          if (currentCount <= 0) {
            return Mono.empty();
          }

          String key = ITEM_KEY_PREFIX + id;
          redisTemplate.opsForHash().put(key, REDIS_ITEM_FIELD_COUNT, currentCount - 1);
          return Mono.empty();
        });
  }

  @Override
  public Mono<Item> findById(Long id) {
    return Mono.fromCallable(() -> {
      String key = ITEM_KEY_PREFIX + id;
      Map<Object, Object> rawEntries = redisTemplate.opsForHash().entries(key);

      Map<String, Object> entries = rawEntries.entrySet()
                                              .stream()
                                              .filter(e -> e.getKey() instanceof String)
                                              .collect(Collectors.toMap(
                                                  e -> (String) e.getKey(),
                                                  Map.Entry::getValue
                                              ));

      if (entries.isEmpty()) {
        throw new ItemNotFoundException(String.format("Товар с id %s не найден", id));
      }

      return convertToItem(key, entries);
    });
  }

  @Override
  public Flux<Item> findAllByIds(Set<Long> ids) {
    return Flux.fromIterable(ids)
               .parallel()
               .runOn(Schedulers.parallel())
               .flatMap(id -> {
                 String key = ITEM_KEY_PREFIX + ":" + id;

                 return Mono.fromCallable(() -> {
                              Map<Object, Object> rawEntries = redisTemplate.opsForHash().entries(key);

                              if (rawEntries.isEmpty()) {
                                throw new ItemNotFoundException("Item with id " + id + " not found");
                              }

                              Map<String, Object> entries = rawEntries.entrySet()
                                                                      .stream()
                                                                      .filter(
                                                                          e -> e.getKey() instanceof String)
                                                                      .collect(Collectors.toMap(
                                                                          e -> (String) e.getKey(),
                                                                          Map.Entry::getValue
                                                                      ));

                              return convertToItem(key, entries);
                            })
                            .onErrorResume(throwable -> {
                              if (throwable instanceof ItemNotFoundException) {
                                return Mono.empty();
                              }
                              return Mono.error(throwable);
                            });
               })
               .sequential();
  }

  private Item convertToItem(String key, Map<String, Object> entries) {
    String idStr = key.substring(key.indexOf(":") + 1);

    return Item.builder()
               .id(Long.parseLong(idStr))
               .title((String) entries.get(REDIS_ITEM_FIELD_TITLE))
               .imgPath((String) entries.get(REDIS_ITEM_FIELD_IMG_PATH))
               .description((String) entries.get(REDIS_ITEM_FIELD_DESCRIPTION))
               .price((Double) entries.get(REDIS_ITEM_FIELD_PRICE))
               .count((Integer) entries.get(REDIS_ITEM_FIELD_COUNT))
               .build();
  }
}