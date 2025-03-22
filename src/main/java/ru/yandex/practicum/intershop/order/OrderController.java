package ru.yandex.practicum.intershop.order;

import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;

@Controller
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/orders")
    @Transactional(readOnly = true)
    public String findAll(Model model) {
        List<Order> orders = orderService.findCompletedOrders();
        List<OrderDto> orderDtos = OrderMapper.mapTo(orders);
        model.addAttribute("orders", orderDtos);
        return "orders";
    }

    @GetMapping("/orders/{id}")
    @Transactional(readOnly = true)
    public String getById(@PathVariable("id") UUID id, Model model) {
        Order order = orderService.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
        OrderDto orderDto = OrderMapper.mapTo(order);
        model.addAttribute("order", orderDto);
        return "order";
    }
}
