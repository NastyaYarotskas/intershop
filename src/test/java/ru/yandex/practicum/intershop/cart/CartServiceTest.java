package ru.yandex.practicum.intershop.cart;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.intershop.BaseTest;
import ru.yandex.practicum.intershop.item.Item;
import ru.yandex.practicum.intershop.item.ItemService;
import ru.yandex.practicum.intershop.order.Order;
import ru.yandex.practicum.intershop.order.OrderService;
import ru.yandex.practicum.intershop.orderitem.OrderItem;
import ru.yandex.practicum.intershop.orderitem.OrderItemService;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Transactional
public class CartServiceTest extends BaseTest {

    private final static UUID ITEM_ID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    @Autowired
    CartService cartService;
    @Autowired
    ItemService itemService;
    @Autowired
    OrderItemService orderItemService;
    @Autowired
    OrderService orderService;

    @Test
    void modifyItemInCart_addItem_shouldCreateNewOrderAndAddOneItem() {
        cartService.modifyItemInCart(ITEM_ID, "PLUS");

        Optional<Order> activeOrder = orderService.findActiveOrder();
        assertTrue(activeOrder.isPresent());

        Optional<Item> item = itemService.findById(ITEM_ID);
        assertTrue(item.isPresent());

        Optional<OrderItem> orderItem = orderItemService.findOrderItem(activeOrder.get(), item.get());
        assertTrue(orderItem.isPresent());
        assertEquals(1, orderItem.get().getCount());
    }

    @Test
    void modifyItemInCart_minusItem_shouldMinusItemFromCart() {
        cartService.modifyItemInCart(ITEM_ID, "PLUS");
        cartService.modifyItemInCart(ITEM_ID, "PLUS");
        cartService.modifyItemInCart(ITEM_ID, "PLUS");
        cartService.modifyItemInCart(ITEM_ID, "MINUS");

        Optional<Order> activeOrder = orderService.findActiveOrder();
        assertTrue(activeOrder.isPresent());

        Optional<Item> item = itemService.findById(ITEM_ID);
        assertTrue(item.isPresent());

        Optional<OrderItem> orderItem = orderItemService.findOrderItem(activeOrder.get(), item.get());
        assertTrue(orderItem.isPresent());
        assertEquals(2, orderItem.get().getCount());
    }

    @Test
    void modifyItemInCart_minusItemWithOneCount_shouldDeleteItemFromCart() {
        cartService.modifyItemInCart(ITEM_ID, "PLUS");
        cartService.modifyItemInCart(ITEM_ID, "MINUS");

        Optional<Order> activeOrder = orderService.findActiveOrder();
        assertTrue(activeOrder.isPresent());

        Optional<Item> item = itemService.findById(ITEM_ID);
        assertTrue(item.isPresent());

        Optional<OrderItem> orderItem = orderItemService.findOrderItem(activeOrder.get(), item.get());
        assertTrue(orderItem.isEmpty());
    }

    @Test
    void modifyItemInCart_minusItemWithZeroCount_shouldDoNothing() {
        cartService.modifyItemInCart(ITEM_ID, "MINUS");

        Optional<Order> activeOrder = orderService.findActiveOrder();
        assertTrue(activeOrder.isPresent());

        Optional<Item> item = itemService.findById(ITEM_ID);
        assertTrue(item.isPresent());

        Optional<OrderItem> orderItem = orderItemService.findOrderItem(activeOrder.get(), item.get());
        assertTrue(orderItem.isEmpty());
    }

    @Test
    void modifyItemInCart_deleteItem_shouldRemoveItemFromCart() {
        cartService.modifyItemInCart(ITEM_ID, "PLUS");
        cartService.modifyItemInCart(ITEM_ID, "PLUS");
        cartService.modifyItemInCart(ITEM_ID, "PLUS");
        cartService.modifyItemInCart(ITEM_ID, "DELETE");

        Optional<Order> activeOrder = orderService.findActiveOrder();
        assertTrue(activeOrder.isPresent());

        Optional<Item> item = itemService.findById(ITEM_ID);
        assertTrue(item.isPresent());

        Optional<OrderItem> orderItem = orderItemService.findOrderItem(activeOrder.get(), item.get());
        assertTrue(orderItem.isEmpty());
    }
}
