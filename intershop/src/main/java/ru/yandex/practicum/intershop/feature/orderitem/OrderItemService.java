package ru.yandex.practicum.intershop.feature.orderitem;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class OrderItemService {

    private final OrderItemRepository orderItemRepository;

    public OrderItemService(OrderItemRepository orderItemRepository) {
        this.orderItemRepository = orderItemRepository;
    }

    private Mono<OrderItemEntity> findOrderItemOrCreateNew(UUID orderId, UUID itemId) {
        return orderItemRepository.findByOrderIdAndItemId(orderId, itemId)
                .switchIfEmpty(Mono.defer(() -> {
                    OrderItemEntity newOrderItem = new OrderItemEntity();
                    newOrderItem.setOrderId(orderId);
                    newOrderItem.setItemId(itemId);
                    newOrderItem.setCount(0);
                    return orderItemRepository.save(newOrderItem);
                }));
    }

    public Flux<OrderItemEntity> findOrderItems(UUID orderId) {
        return orderItemRepository.findByOrderId(orderId);
    }

    public Mono<Integer> findOrderItemCount(UUID orderID, UUID itemId) {
        return orderItemRepository.findByOrderIdAndItemId(orderID, itemId)
                .map(OrderItemEntity::getCount)
                .defaultIfEmpty(0);
    }

    public Mono<Void> addItemToOrder(UUID orderId, UUID itemId) {
        return findOrderItemOrCreateNew(orderId, itemId)
                .flatMap(orderItem -> orderItemRepository
                        .updateCount(orderItem.getOrderId(), orderItem.getItemId(), orderItem.getCount() + 1));
    }

    public Mono<Void> minusItemFromOrder(UUID orderId, UUID itemId) {
        return findOrderItemOrCreateNew(orderId, itemId)
                .flatMap(orderItem -> {
                    int newCount = Math.max(orderItem.getCount() - 1, 0);
                    if (newCount == 0) {
                        return orderItemRepository.delete(orderItem.getOrderId(), orderItem.getItemId());
                    } else {
                        return orderItemRepository.updateCount(orderItem.getOrderId(), orderItem.getItemId(), newCount);
                    }
                });
    }

    public Mono<Void> deleteItemFromOrder(UUID orderId, UUID itemId) {
        return findOrderItemOrCreateNew(orderId, itemId)
                .flatMap(orderItem -> orderItemRepository.delete(orderItem.getOrderId(), orderItem.getItemId()));
    }
}
