package ru.yandex.practicum.intershop.item;

import org.springframework.stereotype.Component;

@Component
public class ItemMapper {

    public ItemDto mapTo(Item item) {
        return new ItemDto(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getImg(),
                item.getPrice(),
                0
        );
    }
}
