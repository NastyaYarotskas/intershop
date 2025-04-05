package ru.yandex.practicum.intershop.cart;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.intershop.BaseTest;
import ru.yandex.practicum.intershop.order.OrderService;
import ru.yandex.practicum.intershop.orderitem.OrderItemService;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class CartServiceTest extends BaseTest {

    private final static UUID ITEM_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    @Autowired
    CartService cartService;
    @Autowired
    OrderItemService orderItemService;
    @Autowired
    OrderService orderService;

    @Test
    void modifyItemInCart_addItem_shouldCreateNewOrderAndAddOneItem() {
        cartService.modifyItemInCart(ITEM_ID, "PLUS")
                .doOnNext(ignored -> orderService.findActiveOrderId()
                        .doOnNext(orderId -> orderItemService.findOrderItemCount(orderId, ITEM_ID)
                                .map(count -> {
                                    assertEquals(1, count);
                                    return count;
                                })))
                .block();
    }

    @Test
    void modifyItemInCart_minusItem_shouldMinusItemFromCart() {
        orderService.findActiveOrderOrCreateNew()
                .flatMap(order -> cartService.modifyItemInCart(ITEM_ID, "PLUS")
                        .then(cartService.modifyItemInCart(ITEM_ID, "PLUS"))
                        .then(cartService.modifyItemInCart(ITEM_ID, "PLUS"))
                        .then(cartService.modifyItemInCart(ITEM_ID, "MINUS"))
                        .then(orderItemService.findOrderItemCount(order.getId(), ITEM_ID)))
                .doOnNext(count -> {
                    Assertions.assertThat(count).isEqualTo(2);
                })
                .block();
    }

    @Test
    void modifyItemInCart_minusItemWithOneCount_shouldDeleteItemFromCart() {
        orderService.findActiveOrderOrCreateNew()
                .flatMap(order -> cartService.modifyItemInCart(ITEM_ID, "PLUS")
                        .then(cartService.modifyItemInCart(ITEM_ID, "MINUS"))
                        .then(orderItemService.findOrderItemCount(order.getId(), ITEM_ID)))
                .doOnNext(count -> {
                    Assertions.assertThat(count).isEqualTo(0);
                })
                .block();
    }

    @Test
    void modifyItemInCart_minusItemWithZeroCount_shouldDoNothing() {
        orderService.findActiveOrderOrCreateNew()
                .flatMap(order -> cartService.modifyItemInCart(ITEM_ID, "MINUS")
                        .then(orderItemService.findOrderItemCount(order.getId(), ITEM_ID)))
                .doOnNext(count -> {
                    Assertions.assertThat(count).isEqualTo(0);
                })
                .block();
    }

    @Test
    void modifyItemInCart_deleteItem_shouldRemoveItemFromCart() {
        orderService.findActiveOrderOrCreateNew()
                .flatMap(order -> cartService.modifyItemInCart(ITEM_ID, "PLUS")
                        .then(cartService.modifyItemInCart(ITEM_ID, "PLUS"))
                        .then(cartService.modifyItemInCart(ITEM_ID, "PLUS"))
                        .then(cartService.modifyItemInCart(ITEM_ID, "DELETE"))
                        .then(orderItemService.findOrderItemCount(order.getId(), ITEM_ID)))
                .doOnNext(count -> {
                    Assertions.assertThat(count).isEqualTo(0);
                })
                .block();
    }
}
