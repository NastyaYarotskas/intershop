package ru.yandex.practicum.intershop.item;

import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class ItemMapper {

    public static Item mapTo(ItemEntity item) {
        return new Item(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getImg(),
                item.getPrice(),
                0
        );
    }

    public static List<Item> mapTo(List<ItemEntity> items) {
        return items.stream().map(ItemMapper::mapTo).toList();
    }
}
