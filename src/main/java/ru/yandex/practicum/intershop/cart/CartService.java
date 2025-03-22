package ru.yandex.practicum.intershop.cart;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.PathVariable;
import ru.yandex.practicum.intershop.item.Item;
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

    @Transactional
    public void modifyItemInCart(@PathVariable("id") UUID itemId, String action) {
        Order order = orderService.findActiveOrderOrCreateNew();

        Item item = itemService.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        switch (action) {
            case "PLUS" -> orderItemService.addItemToOrder(order, item);
            case "MINUS" -> orderItemService.minusItemFromOrder(order, item);
            case "DELETE" -> orderItemService.deleteItemFromOrder(order, item);
            case null, default -> throw new IllegalArgumentException("Invalid action: " + action);
        }
    }
}
