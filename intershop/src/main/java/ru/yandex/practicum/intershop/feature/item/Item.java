package ru.yandex.practicum.intershop.feature.item;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Item {
    private UUID id;
    private String title;
    private String description;
    private String img;
    private int price;
    private int count;
}
