package ru.yandex.practicum.intershop.order;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.intershop.BaseTest;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

public class OrderServiceTest extends BaseTest {

    @Autowired
    OrderService orderService;

    @Test
    void findActiveOrderOrCreateNew_activeOrderNotExists_shouldCreateNewOrder() {
        orderService.findActiveOrder()
                .defaultIfEmpty(new Order())
                .flatMap(existing -> {
                    if (existing.getId() != null) {
                        return Mono.error(new AssertionError("Active order should not exist"));
                    }
                    return orderService.findActiveOrderOrCreateNew();
                })
                .flatMap(newOrder -> orderService.findActiveOrder()
                        .map(active -> {
                            assertNotNull(active);
                            assertEquals(active.getId(), newOrder.getId());
                            assertTrue(active.isNew());
                            return active;
                        }))
                .block();
    }

    @Test
    void findActiveOrderOrCreateNew_activeOrderExists_shouldUseExistentOrder() {
        orderService.findActiveOrderOrCreateNew()
                .flatMap(firstOrder -> orderService.findActiveOrderOrCreateNew()
                        .map(secondOrder -> {
                            assertEquals(firstOrder.getId(), secondOrder.getId());
                            return secondOrder;
                        }))
                .block();
    }

    @Test
    void completeOrder_orderExists_shouldCompleteOrder() {
        orderService.findActiveOrderOrCreateNew()
                .flatMap(order -> {
                    UUID orderId = order.getId();
                    return orderService.completeOrder()
                            .then(orderService.findById(orderId));
                })
                .doOnNext(completedOrder -> {
                    assertNotNull(completedOrder);
                    assertFalse(completedOrder.isNew());
                })
                .block();
    }
}
