package ru.yandex.practicum.intershop.feature.orderitem;

import lombok.Getter;
import lombok.Setter;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Getter
@Setter
@Table(name = "orders_items")
public class OrderItemEntity {
    private UUID orderId;
    private UUID itemId;
    private int count;
}
