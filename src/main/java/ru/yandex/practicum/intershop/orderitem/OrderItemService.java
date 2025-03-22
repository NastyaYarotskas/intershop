package ru.yandex.practicum.intershop.orderitem;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.intershop.item.Item;
import ru.yandex.practicum.intershop.order.Order;

@Service
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;

    public OrderItemService(OrderItemRepository orderItemRepository) {
        this.orderItemRepository = orderItemRepository;
    }

    @Transactional
    public OrderItem createNewOrderItem(Order order, Item item) {
        OrderItem orderItem = new OrderItem();
//        orderItem.setId(new OrderItemId(order.getId(), item.getId()));
        orderItem.setOrder(order);
        orderItem.setItem(item);
        return orderItemRepository.save(orderItem);
    }
}
