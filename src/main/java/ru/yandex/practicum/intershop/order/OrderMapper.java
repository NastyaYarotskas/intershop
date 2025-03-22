package ru.yandex.practicum.intershop.order;

import lombok.Data;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.intershop.orderitem.OrderItemDto;

import java.util.List;

@Data
@Component
public class OrderMapper {

    public OrderDto mapTo(Order order) {
        List<OrderItemDto> itemDtos = order.getItems()
                .stream()
                .map(oi -> new OrderItemDto(
                                oi.getItem().getId(),
                                oi.getItem().getTitle(),
                                oi.getItem().getDescription(),
                                oi.getItem().getImg(),
                                oi.getItem().getPrice(),
                                oi.getCount()
                        )
                )
                .toList();

        return new OrderDto(order.getId(), itemDtos);
    }
}
