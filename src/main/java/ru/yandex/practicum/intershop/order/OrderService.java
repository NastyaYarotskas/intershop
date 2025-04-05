package ru.yandex.practicum.intershop.order;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    public Flux<OrderEntity> findCompletedOrders() {
        return orderRepository.findByIsNewFalse();
    }

    public Mono<OrderEntity> findById(UUID id) {
        return orderRepository.findById(id);
    }

    public Mono<OrderEntity> findActiveOrder() {
        return orderRepository.findFirstByIsNewTrue();
    }

    public Mono<OrderEntity> findActiveOrderOrCreateNew() {
        return orderRepository.findByIsNewTrue()
                .switchIfEmpty(Mono.defer(() -> {
                    OrderEntity newOrder = new OrderEntity();
                    newOrder.setNew(true);
                    return orderRepository.save(newOrder);
                }));
    }

    public Mono<Void> completeOrder() {
        return orderRepository.findByIsNewTrue()
                .flatMap(order -> {
                    order.setNew(false);
                    return orderRepository.save(order);
                })
                .then();
    }
}
