package ru.yandex.practicum.intershop.cart;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.intershop.item.ItemService;
import ru.yandex.practicum.intershop.order.OrderDto;
import ru.yandex.practicum.intershop.order.OrderService;
import ru.yandex.practicum.intershop.orderitem.OrderItemDto;
import ru.yandex.practicum.intershop.orderitem.OrderItemService;

import java.util.UUID;

@Controller
public class CartController {

    private final OrderService orderService;
    private final CartService cartService;
    private final OrderItemService orderItemService;
    private final ItemService itemService;

    public CartController(OrderService orderService,
                          CartService cartService, OrderItemService orderItemService, ItemService itemService) {
        this.orderService = orderService;
        this.cartService = cartService;
        this.orderItemService = orderItemService;
        this.itemService = itemService;
    }

    @GetMapping("/cart/items")
    public Mono<String> getCart(Model model) {
        return orderService.findActiveOrderOrCreateNew()
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
