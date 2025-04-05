package ru.yandex.practicum.intershop.order;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.intershop.error.EntityNotFoundException;
import ru.yandex.practicum.intershop.item.ItemService;
import ru.yandex.practicum.intershop.orderitem.OrderItemDto;
import ru.yandex.practicum.intershop.orderitem.OrderItemService;

import java.util.UUID;

@Controller
public class OrderController {

    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final ItemService itemService;

    public OrderController(OrderService orderService, OrderItemService orderItemService, ItemService itemService) {
        this.orderService = orderService;
        this.orderItemService = orderItemService;
        this.itemService = itemService;
    }

    @GetMapping("/orders")
    public Mono<String> findAll(Model model) {
        return orderService.findCompletedOrders()
                .flatMap(order ->
                        orderItemService.findOrderItems(order.getId())
                                .flatMap(orderItem ->
                                        itemService.findById(orderItem.getItemId())
                                                .map(item -> {
                                                    OrderItemDto dto = new OrderItemDto();
                                                    dto.setId(item.getId());
                                                    dto.setTitle(item.getTitle());
                                                    dto.setDescription(item.getDescription());
                                                    dto.setImg(item.getImg());
                                                    dto.setPrice(item.getPrice());
                                                    dto.setCount(orderItem.getCount());
                                                    return dto;
                                                })
                                )
                                .collectList()
                                .map(orderItemDtos -> {
                                    // Создаем DTO заказа с собранными позициями
                                    OrderDto orderDto = new OrderDto();
                                    orderDto.setId(order.getId());
                                    // Другие поля заказа
                                    orderDto.setItems(orderItemDtos);
                                    return orderDto;
                                })
                )
                .collectList()
                .doOnSuccess(orderDtos -> model.addAttribute("orders", orderDtos))
                .thenReturn("orders");
    }

    @GetMapping("/orders/{id}")
    public Mono<String> findOrderById(@PathVariable("id") UUID id, Model model) {
        return orderService.findById(id)
                .switchIfEmpty(Mono.error(new EntityNotFoundException(id)))
                .flatMap(order ->
                        orderItemService.findOrderItems(order.getId())
                                .flatMap(orderItem ->
                                        itemService.findById(orderItem.getItemId())
                                                .map(item -> {
                                                    OrderItemDto dto = new OrderItemDto();
                                                    dto.setId(item.getId());
                                                    dto.setTitle(item.getTitle());
                                                    dto.setDescription(item.getDescription());
                                                    dto.setImg(item.getImg());
                                                    dto.setPrice(item.getPrice());
                                                    dto.setCount(orderItem.getCount());
                                                    return dto;
                                                })
                                )
                                .collectList()
                                .map(items -> {
                                    OrderDto dto = new OrderDto();
                                    dto.setId(order.getId());
                                    dto.setItems(items);
                                    model.addAttribute("order", dto);
                                    return dto;
                                })
                )
                .thenReturn("order");
    }
}
