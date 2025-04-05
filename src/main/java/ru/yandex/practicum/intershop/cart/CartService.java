package ru.yandex.practicum.intershop.cart;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.intershop.item.ItemEntity;
import ru.yandex.practicum.intershop.item.ItemService;
import ru.yandex.practicum.intershop.order.Order;
import ru.yandex.practicum.intershop.order.OrderService;
import ru.yandex.practicum.intershop.orderitem.OrderItemService;

import java.util.UUID;

@Service
public class CartService {

    private final OrderService orderService;
    private final ItemService itemService;
    private final OrderItemService orderItemService;

    public CartService(OrderService orderService,
                       ItemService itemService,
                       OrderItemService orderItemService) {
        this.orderService = orderService;
        this.itemService = itemService;
        this.orderItemService = orderItemService;
    }

    public Mono<Void> modifyItemInCart(UUID itemId, String action) {
        return orderService.findActiveOrderOrCreateNew()
                .zipWith(itemService.findById(itemId)
                        .switchIfEmpty(Mono.error(new RuntimeException("Item not found"))))
                .flatMap(tuple -> {
                    Order order = tuple.getT1();
                    ItemEntity item = tuple.getT2();

                    return switch (action) {
                        case "PLUS" -> orderItemService.addItemToOrder(order.getId(), item.getId());
                        case "MINUS" -> orderItemService.minusItemFromOrder(order.getId(), item.getId());
                        case "DELETE" -> orderItemService.deleteItemFromOrder(order.getId(), item.getId());
                        default -> Mono.error(new IllegalArgumentException("Invalid action: " + action));
                    };
                });
    }
}
