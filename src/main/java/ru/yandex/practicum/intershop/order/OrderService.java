package ru.yandex.practicum.intershop.order;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.intershop.error.EntityNotFoundException;
import ru.yandex.practicum.intershop.item.ItemService;
import ru.yandex.practicum.intershop.orderitem.OrderItemMapper;
import ru.yandex.practicum.intershop.orderitem.OrderItemService;

import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemService orderItemService;
    private final ItemService itemService;

    public OrderService(OrderRepository orderRepository, OrderItemService orderItemService, ItemService itemService) {
        this.orderRepository = orderRepository;
        this.orderItemService = orderItemService;
        this.itemService = itemService;
    }

    public Flux<Order> findCompletedOrders() {
        return orderRepository.findByIsNewFalse()
                .flatMap(order ->
                        orderItemService.findOrderItems(order.getId())
                                .flatMap(orderItem ->
                                        itemService.findById(orderItem.getItemId())
                                                .map(item -> OrderItemMapper.mapFrom(item, orderItem.getCount()))
                                )
                                .collectList()
                                .map(orderItems -> new Order(order.getId(), orderItems))
                );
    }

    public Mono<OrderEntity> findById(UUID id) {
        return orderRepository.findById(id);
    }

    public Mono<OrderEntity> findActiveOrder() {
        return orderRepository.findFirstByIsNewTrue();
    }

    public Mono<UUID> findActiveOrderId() {
        return orderRepository.findFirstByIsNewTrue()
                .defaultIfEmpty(new OrderEntity(UUID.randomUUID(), true))
                .flatMap(orderEntity -> Mono.just(orderEntity.getId()));
    }

    public Mono<Order> findActiveOrderOrCreateNew() {
        return orderRepository.findByIsNewTrue()
                .switchIfEmpty(Mono.defer(() -> {
                    OrderEntity newOrder = new OrderEntity();
                    newOrder.setNew(true);
                    return orderRepository.save(newOrder);
                }))
                .flatMap(order -> orderItemService.findOrderItems(order.getId())
                        .flatMap(orderItem ->
                                itemService.findById(orderItem.getItemId())
                                        .map(item -> OrderItemMapper.mapFrom(item, orderItem.getCount()))
                        )
                        .collectList()
                        .map(items -> new Order(order.getId(), items))
                );
    }

    public Mono<Void> completeOrder() {
        return orderRepository.findByIsNewTrue()
                .flatMap(order -> {
                    order.setNew(false);
                    return orderRepository.save(order);
                }).then();
    }

    public Mono<Order> findOrderById(UUID id) {
        return orderRepository.findById(id)
                .switchIfEmpty(Mono.error(new EntityNotFoundException(id)))
                .flatMap(order -> orderItemService.findOrderItems(order.getId())
                        .flatMap(orderItem ->
                                itemService.findById(orderItem.getItemId())
                                        .map(item -> OrderItemMapper.mapFrom(item, orderItem.getCount()))
                        )
                        .collectList()
                        .map(items -> new Order(order.getId(), items))
                );
    }
}
