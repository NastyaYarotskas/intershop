package ru.yandex.practicum.intershop.order;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.intershop.BaseTest;
import ru.yandex.practicum.intershop.item.ItemEntity;
import ru.yandex.practicum.intershop.item.ItemService;
import ru.yandex.practicum.intershop.orderitem.OrderItemEntity;
import ru.yandex.practicum.intershop.orderitem.OrderItemService;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;

@AutoConfigureWebTestClient
public class OrderControllerTest extends BaseTest {

    @MockitoBean
    OrderService orderService;
    @MockitoBean
    OrderItemService orderItemService;
    @MockitoBean
    ItemService itemService;
    @Autowired
    WebTestClient webTestClient;

    @Test
    void findAll_validRequest_shouldAddOrdersToModelAttributes() {
        Mockito.when(orderService.findCompletedOrders()).thenReturn(Flux.just(new OrderEntity(UUID.randomUUID(), false)));
        Mockito.when(orderItemService.findOrderItems(any())).thenReturn(Flux.just(new OrderItemEntity()));
        Mockito.when(itemService.findById(any())).thenReturn(Mono.just(new ItemEntity()));

        webTestClient.get().uri("/orders")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML);
    }

    @Test
    void findOrderById_orderIsPresent_shouldAddFoundOrderToModelAttributes() {
        UUID orderId = UUID.fromString("550e8400-e29b-41d4-a716-446655440006");
        OrderEntity order = new OrderEntity(orderId, true);

        Mockito.when(orderService.findById(orderId)).thenReturn(Mono.just(order));
        Mockito.when(orderItemService.findOrderItems(orderId)).thenReturn(Flux.just(new OrderItemEntity()));
        Mockito.when(itemService.findById(any())).thenReturn(Mono.just(new ItemEntity()));

        webTestClient.get().uri("/orders/" + orderId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML);
    }

    @Test
    void findOrderById_orderIsNotPresent_shouldRedirectToErrorPage() {
        UUID orderId = UUID.fromString("550e8400-e29b-41d4-a716-446655440006");

        Mockito.when(orderService.findById(orderId)).thenReturn(Mono.empty());

        webTestClient.get().uri("/orders/" + orderId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML);
    }
}
