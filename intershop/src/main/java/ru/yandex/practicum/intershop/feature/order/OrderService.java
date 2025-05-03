package ru.yandex.practicum.intershop.feature.order;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.intershop.feature.error.EntityNotFoundException;
import ru.yandex.practicum.intershop.feature.item.ItemService;
import ru.yandex.practicum.intershop.feature.orderitem.OrderItemMapper;
import ru.yandex.practicum.intershop.feature.orderitem.OrderItemService;

import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemService orderItemService;
    private final ItemService itemService;

    public OrderService(OrderRepository orderRepository,
                        OrderItemService orderItemService,
                        ItemService itemService) {
        this.orderRepository = orderRepository;
        this.orderItemService = orderItemService;
        this.itemService = itemService;
    }

    public Flux<Order> findCompletedOrders(UUID userId) {
        return orderRepository.findByIsNewFalseAndUserId(userId)
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

    public Mono<OrderEntity> findActiveOrder(UUID userId) {
        return orderRepository.findFirstByIsNewTrueAndUserId(userId);
    }

    public Mono<UUID> findActiveOrderId(UUID userId) {
        return orderRepository.findFirstByIsNewTrueAndUserId(userId)
                .defaultIfEmpty(new OrderEntity(UUID.randomUUID(), true, UUID.randomUUID()))
                .flatMap(orderEntity -> Mono.just(orderEntity.getId()));
    }

    public Mono<Order> findActiveOrderOrCreateNew(UUID userId) {
        return orderRepository.findByIsNewTrueAndUserId(userId)
                .switchIfEmpty(Mono.defer(() -> {
                    OrderEntity newOrder = new OrderEntity();
                    newOrder.setNew(true);
                    newOrder.setUserId(userId);
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

    public Mono<Void> completeOrder(UUID userId) {
        return orderRepository.findByIsNewTrueAndUserId(userId)
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
