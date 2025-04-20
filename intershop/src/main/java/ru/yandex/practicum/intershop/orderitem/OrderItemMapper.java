package ru.yandex.practicum.intershop.orderitem;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.intershop.item.ItemEntity;

@UtilityClass
public class OrderItemMapper {

    public static OrderItem mapFrom(ItemEntity item, int count) {
        OrderItem dto = new OrderItem();
        dto.setId(item.getId());
        dto.setTitle(item.getTitle());
        dto.setDescription(item.getDescription());
        dto.setImg(item.getImg());
        dto.setPrice(item.getPrice());
        dto.setCount(count);
        return dto;
    }
}
