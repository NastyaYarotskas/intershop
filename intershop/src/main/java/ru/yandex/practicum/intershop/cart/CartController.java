package ru.yandex.practicum.intershop.cart;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.intershop.order.OrderService;

import java.util.UUID;

@Controller
public class CartController {

    private final OrderService orderService;
    private final CartService cartService;

    public CartController(OrderService orderService,
                          CartService cartService) {
        this.orderService = orderService;
        this.cartService = cartService;
    }

    @GetMapping("/cart/items")
    public Mono<String> getCart(Model model) {
        return orderService.findActiveOrderOrCreateNew()
                .doOnSuccess(order -> model.addAttribute("order", order))
                .thenReturn("cart");
    }

    @PostMapping("/buy")
    public Mono<String> buy() {
        return orderService.completeOrder()
                .thenReturn("redirect:/");
    }

    @PostMapping("/cart/items/{id}")
    public Mono<String> modifyItemInCartFromCart(@PathVariable("id") UUID itemId, @RequestBody ItemActionRequest request) {
        return cartService.modifyItemInCart(itemId, request.getAction())
                .thenReturn("redirect:/cart/items");
    }

    @PostMapping("/items/{id}")
    public Mono<String> modifyItemInCartFromItem(@PathVariable("id") UUID itemId, @RequestBody ItemActionRequest request) {
        return cartService.modifyItemInCart(itemId, request.getAction())
                .thenReturn("redirect:/items/" + itemId);
    }

    @PostMapping(value = "/main/items/{id}")
    public Mono<String> modifyItemInCartFromMain(@PathVariable("id") UUID itemId, @RequestBody ItemActionRequest request) {
        return cartService.modifyItemInCart(itemId, request.getAction())
                .thenReturn("redirect:/");
    }
}
