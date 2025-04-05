package ru.yandex.practicum.intershop.orderitem;

import lombok.Data;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Table(name = "orders_items")
@Data
public class OrderItem {
    private UUID orderId;
    private UUID itemId;
    private int count;
}
