package ru.yandex.practicum.eshop.core.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Pageable;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.eshop.core.entity.Item;
import ru.yandex.practicum.eshop.core.service.ItemHashService;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemHashServiceImpl implements ItemHashService {
  private static final String ITEM_KEY_PREFIX = "item";
  private final RedisTemplate<String,String> redisTemplate;

  @Override
  public List<Item> findByTitleContainingIgnoreCase(String search, Pageable pageableRequest) {
    List<Item> searchedItems = new ArrayList<>();

    Set<String> keys = redisTemplate.keys(ITEM_KEY_PREFIX + "*");

    for (String key : keys) {
      HashOperations<String, Object, Object> hashOps = redisTemplate.opsForHash();
      Map<Object, Object> itemMap = hashOps.entries(key);

      String title = (String) itemMap.get("title");
      if (title != null && title.toLowerCase().contains(search.toLowerCase())) {
        Item item = new Item();
        item.setTitle((String) itemMap.get("title"));
        item.setImgPath((String) itemMap.get("imgPath"));
        item.setDescription((String) itemMap.get("description"));
        item.setPrice((Double) itemMap.get("price"));
        item.setCount((Integer) itemMap.get("count"));

        String idStr = key.substring(ITEM_KEY_PREFIX.length());
        item.setId(Long.parseLong(idStr));

        searchedItems.add(item);
      }
    }

    int start = pageableRequest.getPageNumber() * pageableRequest.getPageSize();
    int end = Math.min(start + pageableRequest.getPageSize(), searchedItems.size());
    return searchedItems.subList(start, end);
  }

  @Override
  public void incrementCount(Long id) {

  }

  @Override
  public void decrementCount(Long id) {

  }

  @Override
  public List<Item> findAll() {
    List<Item> searchedItems = new ArrayList<>();

    Set<String> keys = redisTemplate.keys(ITEM_KEY_PREFIX + "*");

    for (String key : keys) {
      HashOperations<String, Object, Object> hashOps = redisTemplate.opsForHash();
      Map<Object, Object> itemMap = hashOps.entries(key);

      Item item = new Item();
      item.setTitle((String) itemMap.get("title"));
      item.setImgPath((String) itemMap.get("imgPath"));
      item.setDescription((String) itemMap.get("description"));
      item.setPrice((Double) itemMap.get("price"));
      item.setCount((Integer) itemMap.get("count"));

      String idStr = key.substring(ITEM_KEY_PREFIX.length());
      item.setId(Long.parseLong(idStr));

      searchedItems.add(item);
    }
    return searchedItems;
  }

  @Override
  public Item findById(Long id) {
    return null;
  }

  @Override
  public List<Item> findAllByIds(List<Long> ids) {
    return null;
  }

}
