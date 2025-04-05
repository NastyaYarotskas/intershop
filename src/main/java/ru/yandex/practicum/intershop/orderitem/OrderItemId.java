package ru.yandex.practicum.intershop.orderitem;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Immutable;

import java.io.Serializable;
import java.util.UUID;

@Immutable
@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderItemId implements Serializable {
    private UUID orderId;
    private UUID itemId;
}
