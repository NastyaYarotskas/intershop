package ru.yandex.practicum.intershop.orderitem;

import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class OrderItem {
    private UUID id;
    private String title;
    private String description;
    private String img;
    private int price;
    private int count;
}
