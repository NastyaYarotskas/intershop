package ru.yandex.practicum.intershop.orderitem;

import lombok.Data;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Data
@Table(name = "orders_items")
public class OrderItemEntity {
    private UUID orderId;
    private UUID itemId;
    private int count;
}
