package ru.yandex.practicum.intershop.orderitem;

import org.springframework.stereotype.Service;
import ru.yandex.practicum.intershop.item.Item;
import ru.yandex.practicum.intershop.order.Order;

import java.util.Optional;

@Service
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;

    public OrderItemService(OrderItemRepository orderItemRepository) {
        this.orderItemRepository = orderItemRepository;
    }

    private OrderItem findOrderItemOrCreateNew(Order order, Item item) {
        OrderItemId orderItemId = new OrderItemId(order.getId(), item.getId());
        return orderItemRepository.findById(orderItemId)
                .orElseGet(() -> {
                    OrderItem newOrderItem = new OrderItem();
                    newOrderItem.setId(orderItemId);
                    newOrderItem.setOrder(order);
                    newOrderItem.setItem(item);
                    newOrderItem.setCount(0);
                    return orderItemRepository.save(newOrderItem);
                });
    }

    public Optional<OrderItem> findOrderItem(Order order, Item item) {
        OrderItemId orderItemId = new OrderItemId(order.getId(), item.getId());
        return orderItemRepository.findById(orderItemId);
    }

    public void addItemToOrder(Order order, Item item) {
        OrderItem orderItem = findOrderItemOrCreateNew(order, item);
        orderItem.setCount(orderItem.getCount() + 1);
        orderItemRepository.save(orderItem);
    }

    public void minusItemFromOrder(Order order, Item item) {
        OrderItem orderItem = findOrderItemOrCreateNew(order, item);
        orderItem.setCount(Math.max(orderItem.getCount() - 1, 0));
        if (orderItem.getCount() == 0) {
            orderItemRepository.delete(orderItem);
        } else {
            orderItemRepository.save(orderItem);
        }
    }

    public void deleteItemFromOrder(Order order, Item item) {
        OrderItem orderItem = findOrderItemOrCreateNew(order, item);
        orderItemRepository.delete(orderItem);
    }
}
