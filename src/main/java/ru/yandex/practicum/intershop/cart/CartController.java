package ru.yandex.practicum.intershop.cart;

import jakarta.websocket.server.PathParam;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import ru.yandex.practicum.intershop.item.Item;
import ru.yandex.practicum.intershop.item.ItemRepository;
import ru.yandex.practicum.intershop.order.*;
import ru.yandex.practicum.intershop.orderitem.OrderItem;
import ru.yandex.practicum.intershop.orderitem.OrderItemId;
import ru.yandex.practicum.intershop.orderitem.OrderItemRepository;
import ru.yandex.practicum.intershop.orderitem.OrderItemService;

import java.util.Optional;
import java.util.UUID;

@Controller
public class CartController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;

    private final OrderItemRepository orderItemRepository;
    private final ItemRepository itemRepository;
    private final OrderItemService orderItemService;
    private final OrderRepository orderRepository;

    public CartController(OrderService orderService, OrderMapper orderMapper, OrderItemRepository orderItemRepository, ItemRepository itemRepository, OrderItemService orderItemService, OrderRepository orderRepository) {
        this.orderService = orderService;
        this.orderMapper = orderMapper;
        this.orderItemRepository = orderItemRepository;
        this.itemRepository = itemRepository;
        this.orderItemService = orderItemService;
        this.orderRepository = orderRepository;
    }

    @GetMapping("/cart/items")
    @Transactional(readOnly = true)
    public String findAll(Model model) {
        Optional<Order> activeOrder = orderService.findActiveOrder();
        OrderDto orderDto = activeOrder
                .map(orderMapper::mapTo)
                .orElse(new OrderDto());
        model.addAttribute("order", orderDto);
        return "cart";
    }

    @PostMapping("/buy")
    @Transactional
    public String buy(Model model) {
        Optional<Order> newOrder = orderRepository.findByIsNewTrue();
        if (newOrder.isPresent()) {
            Order order = newOrder.get();
            order.setNew(false);
            orderRepository.save(order);
        }
        return "redirect:/";
    }

    @PostMapping("/cart/items/{id}")
    @Transactional
    public String getById(@PathVariable("id") UUID itemId, @PathParam("action") String action, Model model) {
        // 1. Найти или создать новый заказ
        Order activeOrder = orderRepository.findByIsNewTrue()
                .orElseGet(() -> {
                    Order newOrder = new Order();
                    newOrder.setNew(true);
                    return orderRepository.save(newOrder);
                });

        // 2. Найти товар по его id
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        // 3. Найти или создать OrderItem для данного заказа и товара
        OrderItemId orderItemId = new OrderItemId(activeOrder.getId(), itemId);
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseGet(() -> {
                    OrderItem newOrderItem = new OrderItem();
                    newOrderItem.setId(orderItemId);
                    newOrderItem.setOrder(activeOrder);
                    newOrderItem.setItem(item);
                    newOrderItem.setCount(0); // Начальное количество
                    return orderItemRepository.save(newOrderItem);
                });

        // 4. Увеличить счетчик (count) в OrderItem
        if ("PLUS".equals(action)) {
            // Увеличить счетчик
            orderItem.setCount(orderItem.getCount() + 1);
            orderItemRepository.save(orderItem);
        } else if ("MINUS".equals(action)) {
            // Уменьшить счетчик (но не меньше 0)
            orderItem.setCount(Math.max(orderItem.getCount() - 1, 0));
            if (orderItem.getCount() == 0) {
                // Если счетчик стал 0, удаляем OrderItem
                orderItemRepository.delete(orderItem);
            } else {
                orderItemRepository.save(orderItem);
            }
        } else if ("DELETE".equals(action)) {
            // Удалить OrderItem
            orderItemRepository.delete(orderItem);
        } else {
            throw new IllegalArgumentException("Invalid action: " + action);
        }

        // 5. Перенаправить на страницу корзины
        return "redirect:/cart/items";
    }
}
