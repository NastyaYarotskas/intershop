package ru.yandex.practicum.intershop.orderitem;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.intershop.item.Item;
import ru.yandex.practicum.intershop.order.Order;

import java.util.UUID;

@Service
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;

    public OrderItemService(OrderItemRepository orderItemRepository) {
        this.orderItemRepository = orderItemRepository;
    }

    private Mono<OrderItem> findOrderItemOrCreateNew(Order order, Item item) {
        return orderItemRepository.findByOrderIdAndItemId(order.getId(), item.getId())
                .switchIfEmpty(Mono.defer(() -> {
                    OrderItem newOrderItem = new OrderItem();
                    newOrderItem.setOrderId(order.getId());
                    newOrderItem.setItemId(item.getId());
                    newOrderItem.setCount(0);
                    return orderItemRepository.save(newOrderItem);
                }));
    }

    public Flux<OrderItem> findOrderItems(UUID orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }

    public Mono<Integer> findOrderItemCount(UUID orderID, UUID itemId) {
        return orderItemRepository.findByOrderIdAndItemId(orderID, itemId)
                .map(OrderItem::getCount)
                .defaultIfEmpty(0);
    }

    public Mono<Void> addItemToOrder(Order order, Item item) {
        return findOrderItemOrCreateNew(order, item)
                .flatMap(orderItem -> {
                    orderItem.setCount(orderItem.getCount() + 1);
                    return orderItemRepository.updateCount(orderItem.getOrderId(), orderItem.getItemId(), orderItem.getCount());
                });
    }

    public Mono<Void> minusItemFromOrder(Order order, Item item) {
        return findOrderItemOrCreateNew(order, item)
                .flatMap(orderItem -> {
                    int newCount = Math.max(orderItem.getCount() - 1, 0);
                    if (newCount == 0) {
                        return orderItemRepository.delete(orderItem.getOrderId(), orderItem.getItemId());
                    } else {
                        return orderItemRepository.updateCount(orderItem.getOrderId(), orderItem.getItemId(), newCount);
                    }
                });
    }

    public Mono<Void> deleteItemFromOrder(Order order, Item item) {
        return findOrderItemOrCreateNew(order, item)
                .flatMap(orderItem -> orderItemRepository.delete(orderItem.getOrderId(), orderItem.getItemId()));
    }
}
