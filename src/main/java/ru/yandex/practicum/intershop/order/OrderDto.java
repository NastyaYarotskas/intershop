package ru.yandex.practicum.intershop.order;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.yandex.practicum.intershop.orderitem.OrderItemDto;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OrderDto {
    private UUID id;
    private List<OrderItemDto> items = new ArrayList<>();

    public int getTotalSum() {
        return items.stream()
                .map(oi -> oi.getPrice() * oi.getCount())
                .reduce(0, Integer::sum);
    }
}
