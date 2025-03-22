package ru.yandex.practicum.intershop.order;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.intershop.BaseTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@Transactional
public class OrderServiceTest extends BaseTest {

    @Autowired
    OrderService orderService;

    @Test
    void findActiveOrderOrCreateNew_activeOrderNotExists_shouldCreateNewOrder() {
        Optional<Order> activeOrder = orderService.findActiveOrder();
        assertTrue(activeOrder.isEmpty());

        Order order = orderService.findActiveOrderOrCreateNew();
        assertTrue(order.isNew());

        activeOrder = orderService.findActiveOrder();
        assertTrue(activeOrder.isPresent());
        assertEquals(activeOrder.get().getId(), order.getId());
    }

    @Test
    void findActiveOrderOrCreateNew_activeOrderExists_shouldUseExistentOrder() {
        orderService.findActiveOrderOrCreateNew();
        Optional<Order> activeOrder = orderService.findActiveOrder();
        assertTrue(activeOrder.isPresent());

        Order order = orderService.findActiveOrderOrCreateNew();
        assertTrue(order.isNew());

        assertEquals(activeOrder.get().getId(), order.getId());
    }

    @Test
    void completeOrder_orderExists_shouldCompleteOrder() {
        Order order = orderService.findActiveOrderOrCreateNew();
        orderService.completeOrder();
        Optional<Order> completedOrder = orderService.findById(order.getId());
        assertTrue(completedOrder.isPresent());
        assertFalse(completedOrder.get().isNew());
        Optional<Order> activeOrder = orderService.findActiveOrder();
        assertTrue(activeOrder.isEmpty());
    }
}
