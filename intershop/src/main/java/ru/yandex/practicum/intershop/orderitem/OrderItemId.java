package ru.yandex.practicum.intershop.orderitem;

import org.springframework.data.annotation.Immutable;

import java.io.Serializable;
import java.util.UUID;

@Immutable
public class OrderItemId implements Serializable {
    private UUID orderId;
    private UUID itemId;
}
