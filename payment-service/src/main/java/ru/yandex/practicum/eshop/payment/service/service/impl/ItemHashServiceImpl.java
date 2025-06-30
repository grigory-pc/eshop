package ru.yandex.practicum.eshop.payment.service.service.impl;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.eshop.payment.service.entity.Item;
import ru.yandex.practicum.eshop.payment.service.exceptions.ItemNotFoundException;
import ru.yandex.practicum.eshop.payment.service.service.ItemHashService;

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
  public Mono<Void> updateAllCountToZero() {
    return Mono.fromRunnable(() -> {
      Set<String> keys = redisTemplate.keys(ITEM_KEY_PREFIX + ":*");
      if (keys != null) {
        keys.forEach(key -> redisTemplate.opsForHash().put(key, REDIS_ITEM_FIELD_COUNT, 0));
      }
    });
  }

  private Item convertToItem(String key, Map<String, Object> entries) {
    String idStr = key.substring(key.indexOf(":") + 1);

    log.info(String.format("Получение объекта товара для ключа: %s", key));

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