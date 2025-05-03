package ru.yandex.practicum.intershop.feature.order;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.intershop.BaseTest;
import ru.yandex.practicum.intershop.feature.error.EntityNotFoundException;

import java.util.List;
import java.util.UUID;

@AutoConfigureWebTestClient
public class OrderControllerTest extends BaseTest {

    @MockitoBean
    OrderService orderService;
    @Autowired
    WebTestClient webTestClient;

    @Test
    @WithMockUser(username = "test")
    void findAll_validRequest_shouldAddOrdersToModelAttributes() {
        Mockito.when(orderService.findCompletedOrders()).thenReturn(Flux.just(new Order(UUID.randomUUID(), List.of())));

        webTestClient.get().uri("/orders")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML);
    }

    @Test
    void findAll_unauthorizedUser_shouldRedirectToLoginPage() {
        webTestClient.get().uri("/orders")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueMatches("Location", ".*/login");
    }

    @Test
    @WithMockUser(username = "test")
    void findOrderById_orderIsPresent_shouldAddFoundOrderToModelAttributes() {
        UUID orderId = UUID.fromString("550e8400-e29b-41d4-a716-446655440006");

        Mockito.when(orderService.findOrderById(orderId)).thenReturn(Mono.just(new Order(orderId, List.of())));

        webTestClient.get().uri("/orders/" + orderId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML);
    }

    @Test
    @WithMockUser(username = "test")
    void findOrderById_orderIsNotPresent_shouldRedirectToErrorPage() {
        UUID orderId = UUID.fromString("550e8400-e29b-41d4-a716-446655440006");

        Mockito.when(orderService.findOrderById(orderId)).thenReturn(Mono.error(new EntityNotFoundException(orderId)));

        webTestClient.get().uri("/orders/" + orderId)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML);
    }

    @Test
    void findOrderById_unauthorizedUser_shouldRedirectToLoginPage() {
        UUID orderId = UUID.fromString("550e8400-e29b-41d4-a716-446655440006");

        webTestClient.get().uri("/orders/" + orderId)
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueMatches("Location", ".*/login");
    }
}
