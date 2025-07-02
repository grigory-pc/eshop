package ru.yandex.practicum.eshop.core.mappers;

import java.util.List;
import java.util.stream.StreamSupport;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import ru.yandex.practicum.eshop.core.dto.ItemDto;
import ru.yandex.practicum.eshop.core.entity.Item;

/**
 * Маппер между объектами DAO Item и DTO ItemDto.
 */
@Mapper(componentModel = "spring")
public interface ItemMapper {
  ItemDto toPreviewDto(Item item);

  ItemDto toDto(Item item);

  default Page<ItemDto> toDtoPage(Page<Item> items) {
    List<ItemDto> dtos = items.stream()
                              .map(this::toPreviewDto)
                              .toList();
    return new PageImpl<>(dtos, pageable(items), items.getTotalElements());
  }

  default Pageable pageable(Page<Item> items) {
    return items.getPageable();
  }

  default List<ItemDto> toListDto(Iterable<Item> items) {
    return StreamSupport.stream(items.spliterator(), false)
                        .map(this::toDto)
                        .toList();
  }
}