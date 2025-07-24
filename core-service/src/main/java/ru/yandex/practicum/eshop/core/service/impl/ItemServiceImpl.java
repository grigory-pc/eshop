package ru.yandex.practicum.eshop.core.service.impl;

import ru.yandex.practicum.eshop.core.exceptions.DataBaseRequestException;
import ru.yandex.practicum.eshop.core.exceptions.SortingException;
import ru.yandex.practicum.eshop.core.mappers.ItemMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.eshop.core.dto.ItemDto;
import ru.yandex.practicum.eshop.core.enums.Sorting;
import ru.yandex.practicum.eshop.core.exceptions.ItemNotFoundException;
import ru.yandex.practicum.eshop.core.pojo.GetItemsData;
import ru.yandex.practicum.eshop.core.repository.ItemHashService;
import ru.yandex.practicum.eshop.core.service.ItemService;

import static ru.yandex.practicum.eshop.core.enums.MessagesLog.MESSAGE_LOG_DB_GET_REQUEST;
import static ru.yandex.practicum.eshop.core.enums.MessagesLog.MESSAGE_LOG_DB_RESPONSE_ERROR;
import static ru.yandex.practicum.eshop.core.enums.MessagesLog.MESSAGE_LOG_FIND_ITEM;
import static ru.yandex.practicum.eshop.core.enums.MessagesLog.MESSAGE_LOG_FIND_ALL_ITEMS;

@Slf4j
@Service
@RequiredArgsConstructor
public class ItemServiceImpl implements ItemService {
  private final ItemMapper itemMapper;
  private final ItemHashService itemHashService;


  @Override
  public Mono<PageImpl<ItemDto>> getItems(GetItemsData getItemsData) {

    //    получить корзину Если её нет, то создать. Получить список карт итем на базе карт.
    //    Для полученного списка товаров сделать фильтр на базе товаров из карт итем.
    // проставить каунт для списка товаров из каунт итем.


    Pageable pageableItems = getPageableItemsRequest(getItemsData.sort(), getItemsData.pageNumber(),
                                                     getItemsData.pageSize());
    log.info(MESSAGE_LOG_DB_GET_REQUEST.getMessage());

    if (getItemsData.search().isEmpty()) {
      return itemHashService.findAll(pageableItems)
                            .collectList()
                            .flatMap(items -> {
                              long total = items.size();
                              return Mono.just(
                                  new PageImpl<>(itemMapper.toListDto(items),
                                                 pageableItems,
                                                 total));
                            })

                            .onErrorResume(e -> {
                              log.error(MESSAGE_LOG_FIND_ALL_ITEMS.getMessage(), e);
                              return Mono.error(new DataBaseRequestException(
                                  MESSAGE_LOG_DB_RESPONSE_ERROR.getMessage(), e));
                            });

    } else {
      return itemHashService.findByTitleContainingIgnoreCase(getItemsData.search(), pageableItems)
                            .skip((long) getItemsData.pageNumber() * getItemsData.pageSize())
                            .limitRate(getItemsData.pageSize())
                            .collectList()
                            .flatMap(items -> {
                              long total = items.size();
                              return Mono.just(
                                  new PageImpl<>(itemMapper.toListDto(items),
                                                 pageableItems,
                                                 total));
                            })
                            .onErrorResume(e -> {
                              log.error(MESSAGE_LOG_FIND_ALL_ITEMS.getMessage(), e);
                              return Mono.error(new DataBaseRequestException(
                                  MESSAGE_LOG_DB_RESPONSE_ERROR.getMessage(), e));
                            });
    }
  }

  @Override
  public Mono<ItemDto> getItem(Long id) {
    return Mono.defer(() -> {
      log.info(MESSAGE_LOG_DB_GET_REQUEST.getMessage());

      return itemHashService.findById(id)
                            .map(itemMapper::toDto)
                            .switchIfEmpty(Mono.error(new ItemNotFoundException("Товар не найден")))
                            .onErrorResume(e -> {
                              log.error(MESSAGE_LOG_FIND_ITEM.getMessage(), e);
                              return Mono.error(new DataBaseRequestException(
                                  MESSAGE_LOG_DB_RESPONSE_ERROR.getMessage(), e));
                            });
    });
  }

  private static Pageable getPageableItemsRequest(Sorting sort, int pageNumber, int pageSize) {
    return switch (sort) {
      case ALPHA -> PageRequest.of(pageNumber, pageSize, Sort.by("title").ascending());
      case PRICE -> PageRequest.of(pageNumber, pageSize, Sort.by("price").ascending());
      case NO -> PageRequest.of(pageNumber, pageSize);
      default -> throw new SortingException("Некорректный тип сортировки: " + sort);
    };
  }
}