package ru.yandex.practicum.intershop.feature.orderitem;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.intershop.BaseTest;
import ru.yandex.practicum.intershop.feature.item.ItemEntity;
import ru.yandex.practicum.intershop.feature.order.OrderService;

import java.util.UUID;

public class OrderItemServiceTest extends BaseTest {

    @Autowired
    OrderItemService orderItemService;
    @Autowired
    OrderService orderService;

    @Test
    void findOrderItemCount_orderItemExists_shouldReturnCorrectCountValue() {
        ItemEntity item = new ItemEntity(
                UUID.fromString("550e8400-e29b-41d4-a716-446655440006"),
                "Электронная книга PocketBook 740",
                "7.8\", 32 ГБ, сенсорный экран, Wi-Fi",
                "",
                19990
        );

        orderService.findActiveOrderOrCreateNew(testUserId)
                .flatMap(order -> orderItemService.addItemToOrder(order.getId(), item.getId())
                        .then(orderItemService.findOrderItemCount(order.getId(), item.getId())))
                .doOnNext(count -> {
                    Assertions.assertThat(count)
                            .isEqualTo(1);
                })
                .block();
    }

    @Test
    void findOrderItemCount_orderItemNotExists_shouldReturnZeroCountValue() {
        final UUID itemId = UUID.fromString("550e8400-e29b-41d4-a716-446655440006");

        orderService.findActiveOrderOrCreateNew(testUserId)
                .flatMap(order -> orderItemService.findOrderItemCount(order.getId(), itemId))
                .doOnNext(count -> {
                    Assertions.assertThat(count)
                            .isEqualTo(0);
                })
                .block();
    }

    @Test
    void addItemToOrder_orderItemExists_shouldIncreaseItemCount() {
        ItemEntity item = new ItemEntity(UUID.fromString("550e8400-e29b-41d4-a716-446655440006"), "Электронная книга PocketBook 740", "7.8\", 32 ГБ, сенсорный экран, Wi-Fi", "", 19990);
        orderService.findActiveOrderOrCreateNew(testUserId)
                .flatMap(order -> orderItemService.addItemToOrder(order.getId(), item.getId())
                        .then(orderItemService.addItemToOrder(order.getId(), item.getId()))
                        .then(orderItemService.findOrderItemCount(order.getId(), item.getId())))
                .doOnNext(count -> {
                    Assertions.assertThat(count)
                            .isEqualTo(2);
                })
                .block();
    }

    @Test
    void minusItemFromOrder_orderItemExists_shouldDecreaseItemCount() {
        ItemEntity item = new ItemEntity(
                UUID.fromString("550e8400-e29b-41d4-a716-446655440006"),
                "Электронная книга PocketBook 740",
                "7.8\", 32 ГБ, сенсорный экран, Wi-Fi",
                "",
                19990
        );

        orderService.findActiveOrderOrCreateNew(testUserId)
                .flatMap(order -> orderItemService.addItemToOrder(order.getId(), item.getId())
                        .then(orderItemService.addItemToOrder(order.getId(), item.getId()))
                        .then(orderItemService.minusItemFromOrder(order.getId(), item.getId()))
                        .then(orderItemService.findOrderItemCount(order.getId(), item.getId())))
                .doOnNext(count -> {
                    Assertions.assertThat(count)
                            .isEqualTo(1);
                })
                .block();
    }

    @Test
    void deleteItemFromOrder_orderItemExists_shouldDecreaseItemCount() {
        ItemEntity item = new ItemEntity(
                UUID.fromString("550e8400-e29b-41d4-a716-446655440006"),
                "Электронная книга PocketBook 740",
                "7.8\", 32 ГБ, сенсорный экран, Wi-Fi",
                "",
                19990
        );

        orderService.findActiveOrderOrCreateNew(testUserId)
                .flatMap(order -> orderItemService.addItemToOrder(order.getId(), item.getId())
                        .then(orderItemService.deleteItemFromOrder(order.getId(), item.getId()))
                        .then(orderItemService.findOrderItemCount(order.getId(), item.getId())))
                .doOnNext(count -> {
                    Assertions.assertThat(count)
                            .isEqualTo(0);
                })
                .block();
    }
}
