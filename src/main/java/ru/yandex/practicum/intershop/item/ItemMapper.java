package ru.yandex.practicum.intershop.item;

import lombok.experimental.UtilityClass;

import java.util.List;

@UtilityClass
public class ItemMapper {

    public static Item mapFrom(CreateItemRequest request) {
        return new Item(
                request.getTitle(),
                request.getDescription(),
                request.getBase64Img(),
                request.getPrice()
        );
    }

    public static ItemDto mapTo(Item item) {
        return new ItemDto(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getImg(),
                item.getPrice(),
                0
        );
    }

    public static List<ItemDto> mapTo(List<Item> items) {
        return items.stream().map(ItemMapper::mapTo).toList();
    }
}
