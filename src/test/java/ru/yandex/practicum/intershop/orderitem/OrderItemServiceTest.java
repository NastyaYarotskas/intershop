package ru.yandex.practicum.intershop.orderitem;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.intershop.BaseTest;
import ru.yandex.practicum.intershop.item.Item;
import ru.yandex.practicum.intershop.order.Order;
import ru.yandex.practicum.intershop.order.OrderService;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Transactional
public class OrderItemServiceTest extends BaseTest {

    @Autowired
    OrderItemService orderItemService;
    @Autowired
    OrderService orderService;

    @Test
    void findOrderItem_orderItemExists_shouldReturnOrderItem() {
        Item item = new Item(UUID.fromString("550e8400-e29b-41d4-a716-446655440006"), "Электронная книга PocketBook 740", "7.8\", 32 ГБ, сенсорный экран, Wi-Fi", "", 19990);
        Order order = orderService.findActiveOrderOrCreateNew();

        orderItemService.addItemToOrder(order, item);
        Optional<OrderItem> orderItem = orderItemService.findOrderItem(order, item);
        assertTrue(orderItem.isPresent());
        assertEquals(1, orderItem.get().getCount());
    }

    @Test
    void findOrderItem_orderItemNotExists_shouldReturnEmptyOptionalOrderItem() {
        Item item = new Item(UUID.fromString("550e8400-e29b-41d4-a716-446655440006"), "Электронная книга PocketBook 740", "7.8\", 32 ГБ, сенсорный экран, Wi-Fi", "", 19990);
        Order order = orderService.findActiveOrderOrCreateNew();

        Optional<OrderItem> orderItem = orderItemService.findOrderItem(order, item);
        assertTrue(orderItem.isEmpty());
    }

    @Test
    void addItemToOrder_orderItemExists_shouldIncreaseItemCount() {
        Item item = new Item(UUID.fromString("550e8400-e29b-41d4-a716-446655440006"), "Электронная книга PocketBook 740", "7.8\", 32 ГБ, сенсорный экран, Wi-Fi", "", 19990);
        Order order = orderService.findActiveOrderOrCreateNew();

        orderItemService.addItemToOrder(order, item);
        Optional<OrderItem> orderItem = orderItemService.findOrderItem(order, item);
        assertTrue(orderItem.isPresent());
        assertEquals(1, orderItem.get().getCount());

        orderItemService.addItemToOrder(order, item);
        orderItem = orderItemService.findOrderItem(order, item);
        assertTrue(orderItem.isPresent());
        assertEquals(2, orderItem.get().getCount());
    }

    @Test
    void minusItemFromOrder_orderItemExists_shouldDecreaseItemCount() {
        Item item = new Item(UUID.fromString("550e8400-e29b-41d4-a716-446655440006"), "Электронная книга PocketBook 740", "7.8\", 32 ГБ, сенсорный экран, Wi-Fi", "", 19990);
        Order order = orderService.findActiveOrderOrCreateNew();

        orderItemService.addItemToOrder(order, item);
        orderItemService.addItemToOrder(order, item);
        orderItemService.minusItemFromOrder(order, item);
        Optional<OrderItem> orderItem = orderItemService.findOrderItem(order, item);
        assertTrue(orderItem.isPresent());
        assertEquals(1, orderItem.get().getCount());

        orderItemService.minusItemFromOrder(order, item);
        orderItem = orderItemService.findOrderItem(order, item);
        assertTrue(orderItem.isEmpty());
    }

    @Test
    void deleteItemFromOrder_orderItemExists_shouldDecreaseItemCount() {
        Item item = new Item(UUID.fromString("550e8400-e29b-41d4-a716-446655440006"), "Электронная книга PocketBook 740", "7.8\", 32 ГБ, сенсорный экран, Wi-Fi", "", 19990);
        Order order = orderService.findActiveOrderOrCreateNew();

        orderItemService.addItemToOrder(order, item);
        orderItemService.addItemToOrder(order, item);
        orderItemService.deleteItemFromOrder(order, item);
        Optional<OrderItem> orderItem = orderItemService.findOrderItem(order, item);
        assertTrue(orderItem.isEmpty());
    }
}
