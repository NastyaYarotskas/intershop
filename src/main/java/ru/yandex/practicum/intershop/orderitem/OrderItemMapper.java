package ru.yandex.practicum.intershop.orderitem;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.intershop.item.Item;

@UtilityClass
public class OrderItemMapper {

    public static OrderItemDto mapFrom(Item item, int count) {
        OrderItemDto dto = new OrderItemDto();
        dto.setId(item.getId());
        dto.setTitle(item.getTitle());
        dto.setDescription(item.getDescription());
        dto.setImg(item.getImg());
        dto.setPrice(item.getPrice());
        dto.setCount(count);
        return dto;
    }
}
