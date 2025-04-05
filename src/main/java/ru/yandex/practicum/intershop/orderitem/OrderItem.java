package ru.yandex.practicum.intershop.orderitem;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderItem {
    private UUID id;
    private String title;
    private String description;
    private String img;
    private int price;
    private int count;
}
