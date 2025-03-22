package ru.yandex.practicum.intershop.orderitem;

import jakarta.persistence.*;
import lombok.Data;
import ru.yandex.practicum.intershop.item.Item;
import ru.yandex.practicum.intershop.order.Order;

@Entity
@Table(name = "orders_items")
@Data
public class OrderItem {

    @EmbeddedId
    private OrderItemId id;

    @ManyToOne
    @MapsId("orderId")
    @JoinColumn(name = "order_id", referencedColumnName = "id")
    private Order order;

    @ManyToOne
    @MapsId("itemId")
    @JoinColumn(name = "item_id", referencedColumnName = "id")
    private Item item;

    private int count;
}
