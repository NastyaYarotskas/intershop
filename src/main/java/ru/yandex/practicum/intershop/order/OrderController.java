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
    private final OrderMapper orderMapper;

    public OrderController(OrderService orderService, OrderMapper orderMapper) {
        this.orderService = orderService;
        this.orderMapper = orderMapper;
    }

    @GetMapping("/orders")
    @Transactional(readOnly = true)
    public String findAll(Model model) {
        List<Order> orders = orderService.findCompletedOrders();
        List<OrderDto> orderDtos = orders.stream().map(orderMapper::mapTo).toList();
        model.addAttribute("orders", orderDtos);
        return "orders";
    }

    @GetMapping("/orders/{id}")
    @Transactional(readOnly = true)
    public String getById(@PathVariable("id") UUID id, Model model) {
        Order order = orderService.getById(id);
        OrderDto orderDto = orderMapper.mapTo(order);

        model.addAttribute("order", orderDto);

        return "order";
    }
}
