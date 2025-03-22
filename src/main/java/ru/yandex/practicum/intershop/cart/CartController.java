package ru.yandex.practicum.intershop.cart;

import jakarta.websocket.server.PathParam;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import ru.yandex.practicum.intershop.order.Order;
import ru.yandex.practicum.intershop.order.OrderDto;
import ru.yandex.practicum.intershop.order.OrderMapper;
import ru.yandex.practicum.intershop.order.OrderService;

import java.util.Optional;
import java.util.UUID;

@Controller
public class CartController {

    private final OrderService orderService;
    private final OrderMapper orderMapper;
    private final CartService cartService;

    public CartController(OrderService orderService,
                          OrderMapper orderMapper,
                          CartService cartService) {
        this.orderService = orderService;
        this.orderMapper = orderMapper;
        this.cartService = cartService;
    }

    @GetMapping("/cart/items")
    @Transactional(readOnly = true)
    public String getCart(Model model) {
        Optional<Order> activeOrder = orderService.findActiveOrder();
        OrderDto orderDto = activeOrder
                .map(orderMapper::mapTo)
                .orElse(new OrderDto());
        model.addAttribute("order", orderDto);
        return "cart";
    }

    @PostMapping("/buy")
    public String buy() {
        orderService.completeOrder();
        return "redirect:/";
    }

    @PostMapping("/cart/items/{id}")
    public String modifyItemInCartFromCart(@PathVariable("id") UUID itemId, @PathParam("action") String action) {
        cartService.modifyItemInCart(itemId, action);
        return "redirect:/cart/items";
    }

    @PostMapping("/items/{id}")
    public String modifyItemInCartFromItem(@PathVariable("id") UUID itemId, @PathParam("action") String action) {
        cartService.modifyItemInCart(itemId, action);
        return "redirect:/items/" + itemId;
    }

    @PostMapping("/main/items/{id}")
    public String modifyItemInCartFromMain(@PathVariable("id") UUID itemId, @PathParam("action") String action) {
        cartService.modifyItemInCart(itemId, action);
        return "redirect:/";
    }
}
