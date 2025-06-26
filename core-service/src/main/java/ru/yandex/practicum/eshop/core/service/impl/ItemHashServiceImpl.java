package ru.yandex.practicum.eshop.core.service.impl;

import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.eshop.core.entity.Item;
import ru.yandex.practicum.eshop.core.service.ItemHashService;

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
               .flatMap(key -> Flux.fromIterable(redisTemplate.opsForHash().entries(key).entrySet())
                                   .map(entry -> convertToItem(key, entry)))
               .skip(pageableRequest.getOffset())
               .take(pageableRequest.getPageSize());
  }

  @Override
  public Flux<Item> findByTitleContainingIgnoreCase(String search, Pageable pageableRequest) {
    return Flux.fromIterable(redisTemplate.keys(ITEM_KEY_PREFIX + "*"))
               .flatMap(key -> Flux.fromIterable(redisTemplate.opsForHash().entries(key).entrySet())
                                   .map(entry -> convertToItem(key, entry))
                                   .filter(item -> item.getTitle().toLowerCase()
                                                       .contains(search.toLowerCase())))
               .skip(pageableRequest.getOffset())
               .take(pageableRequest.getPageSize());
  }


  @Override
  public Mono<Void> incrementCount(Long id) {

  }

  @Override
  public Mono<Void> decrementCount(Long id) {

  }

  @Override
  public Mono<Item> findById(Long id) {
    return null;
  }

  @Override
  public Flux<Item> findAllByIds(Set<Long> ids) {
    return null;
  }

  private Item convertToItem(String key, Map.Entry<Object, Object> entry) {
    String idStr = key.substring(key.indexOf(":") + 1);

    Map<String, Object> hash = (Map<String, Object>) entry.getValue();
    return Item.builder()
               .id(Long.parseLong(idStr))
               .title((String) hash.get(REDIS_ITEM_FIELD_TITLE))
               .imgPath((String) hash.get(REDIS_ITEM_FIELD_IMG_PATH))
               .description((String) hash.get(REDIS_ITEM_FIELD_DESCRIPTION))
               .price((Double) hash.get(REDIS_ITEM_FIELD_PRICE))
               .count((Integer) hash.get(REDIS_ITEM_FIELD_COUNT))
               .build();
  }
}