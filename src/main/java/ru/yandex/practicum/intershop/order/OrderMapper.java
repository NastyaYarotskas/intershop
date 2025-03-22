package ru.yandex.practicum.intershop.order;

import lombok.experimental.UtilityClass;
import ru.yandex.practicum.intershop.orderitem.OrderItemDto;

import java.util.List;

@UtilityClass
public class OrderMapper {

    public static OrderDto mapTo(Order order) {
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

    public static List<OrderDto> mapTo(List<Order> orders) {
        return orders.stream().map(OrderMapper::mapTo).toList();
    }
}
