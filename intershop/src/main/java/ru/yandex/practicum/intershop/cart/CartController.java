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
    private final PaymentServiceClient paymentServiceClient;

    public CartController(OrderService orderService,
                          CartService cartService,
                          PaymentServiceClient paymentServiceClient) {
        this.orderService = orderService;
        this.cartService = cartService;
        this.paymentServiceClient = paymentServiceClient;
    }

    @GetMapping("/cart/items")
    public Mono<String> getCart(Model model) {
        Mono<Balance> balanceMono = paymentServiceClient.getCurrentBalance()
                .doOnSuccess(b -> model.addAttribute("paymentServiceAvailable", true))
                .onErrorResume(e -> {
                    model.addAttribute("paymentServiceAvailable", false);
                    return Mono.just(new Balance(0));
                });

        return balanceMono.zipWith(orderService.findActiveOrderOrCreateNew())
                .doOnSuccess(tuple -> {
                    model.addAttribute("balance", tuple.getT1().getAmount());
                    model.addAttribute("order", tuple.getT2());
                })
                .thenReturn("cart");
    }

    @PostMapping("/buy")
    public Mono<String> makePayment(Model model) {
        return orderService.findActiveOrderOrCreateNew()
                .flatMap(order -> paymentServiceClient.makePayment(order.getTotalSum()))
                .flatMap(balance -> orderService.completeOrder())
                .thenReturn("redirect:/")
                .onErrorResume(e -> {
                    model.addAttribute("errorMessage", "Оплата не прошла");
                    return Mono.just("error");
                });
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
