package ru.yandex.practicum.intershop.feature.order;

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
        orderService.findActiveOrder(testUserId)
                .defaultIfEmpty(new OrderEntity())
                .flatMap(existing -> {
                    if (existing.getId() != null) {
                        return Mono.error(new AssertionError("Active order should not exist"));
                    }
                    return orderService.findActiveOrderOrCreateNew(testUserId);
                })
                .flatMap(newOrder -> orderService.findActiveOrder(testUserId)
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
        orderService.findActiveOrderOrCreateNew(testUserId)
                .flatMap(firstOrder -> orderService.findActiveOrderOrCreateNew(testUserId)
                        .map(secondOrder -> {
                            assertEquals(firstOrder.getId(), secondOrder.getId());
                            return secondOrder;
                        }))
                .block();
    }

    @Test
    void completeOrder_orderExists_shouldCompleteOrder() {
        orderService.findActiveOrderOrCreateNew(testUserId)
                .flatMap(order -> {
                    UUID orderId = order.getId();
                    return orderService.completeOrder(testUserId)
                            .then(orderService.findById(orderId));
                })
                .doOnNext(completedOrder -> {
                    assertNotNull(completedOrder);
                    assertFalse(completedOrder.isNew());
                })
                .block();
    }
}
