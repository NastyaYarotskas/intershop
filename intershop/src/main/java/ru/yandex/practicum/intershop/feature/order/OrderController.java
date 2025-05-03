package ru.yandex.practicum.intershop.feature.order;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.intershop.feature.user.CustomUserDetails;

import java.util.UUID;

@Controller
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/orders")
    public Mono<String> findAll(@AuthenticationPrincipal CustomUserDetails userDetails, Model model) {
        return orderService.findCompletedOrders(userDetails.getUserId())
                .collectList()
                .doOnSuccess(orders -> model.addAttribute("orders", orders))
                .thenReturn("orders");
    }

    @GetMapping("/orders/{id}")
    public Mono<String> findOrderById(@PathVariable("id") UUID id, Model model) {
        return orderService.findOrderById(id)
                .doOnSuccess(order -> model.addAttribute("order", order))
                .thenReturn("order");
    }
}
