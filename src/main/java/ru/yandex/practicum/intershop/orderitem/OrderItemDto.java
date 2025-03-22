package ru.yandex.practicum.intershop.orderitem;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.UUID;

@Data
@AllArgsConstructor
public class OrderItemDto {
    private UUID id;
    private String title;
    private String description;
    private String img;
    private int price;
    private int count;
}
