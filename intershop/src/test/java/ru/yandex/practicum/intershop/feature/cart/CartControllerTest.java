package ru.yandex.practicum.intershop.feature.cart;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.intershop.BaseTest;
import ru.yandex.practicum.intershop.feature.order.Order;
import ru.yandex.practicum.intershop.feature.order.OrderService;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.anyInt;

@AutoConfigureWebTestClient
public class CartControllerTest extends BaseTest {

    @MockitoBean
    OrderService orderService;
    @MockitoBean
    CartService cartService;
    @MockitoBean
    PaymentServiceClient paymentServiceClient;
    @Autowired
    WebTestClient webTestClient;

    @Test
    @WithMockUser(username = "test")
    void getCart_orderExists_shouldAddOrderAttributeToModel() {
        Mockito.when(paymentServiceClient.getCurrentBalance()).thenReturn(Mono.just(new Balance(200)));
        Mockito.when(orderService.findActiveOrderOrCreateNew()).thenReturn(Mono.just(new Order(UUID.randomUUID(), List.of())));

        webTestClient.get().uri("/cart/items")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML);
    }

    @Test
    void getCart_unauthorizedUser_shouldRedirectToLoginPage() {
        webTestClient.get().uri("/cart/items")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueMatches("Location", ".*/login");
    }

    @Test
    @WithMockUser(username = "test")
    void makePayment_orderExists_shouldCompleteOrderAndRedirect() {
        Mockito.when(paymentServiceClient.makePayment(anyInt())).thenReturn(Mono.just(new Balance(200)));
        Mockito.when(orderService.findActiveOrderOrCreateNew()).thenReturn(Mono.just(new Order()));
        Mockito.when(orderService.completeOrder()).thenReturn(Mono.empty());

        webTestClient.post().uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueMatches("Location", "/");

        Mockito.verify(orderService, Mockito.times(1)).completeOrder();
    }

    @Test
    void makePayment_unauthorizedUser_shouldRedirectToLoginPage() {
        Mockito.when(paymentServiceClient.makePayment(anyInt())).thenReturn(Mono.just(new Balance(200)));
        Mockito.when(orderService.findActiveOrderOrCreateNew()).thenReturn(Mono.just(new Order()));
        Mockito.when(orderService.completeOrder()).thenReturn(Mono.empty());

        webTestClient.post().uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueMatches("Location", ".*/login");

        Mockito.verify(orderService, Mockito.times(0)).completeOrder();
    }

    @Test
    @WithMockUser(username = "test")
    void modifyItemInCartFromCart_paramsArePresent_shouldModifyCartAndRedirect() {
        UUID itemId = UUID.randomUUID();

        Mockito.when(cartService.modifyItemInCart(itemId, "PLUS")).thenReturn(Mono.empty());

        webTestClient.post().uri("/cart/items/" + itemId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ItemActionRequest("PLUS"))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueMatches("Location", "/cart/items");
    }

    @Test
    void modifyItemInCartFromCart_unauthorizedUser_shouldRedirectToLoginPage() {
        UUID itemId = UUID.randomUUID();

        webTestClient.post().uri("/cart/items/" + itemId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ItemActionRequest("PLUS"))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueMatches("Location", ".*/login");
    }

    @Test
    @WithMockUser(username = "test")
    void modifyItemInCartFromItem_paramsArePresent_shouldModifyCartAndRedirect() {
        UUID itemId = UUID.randomUUID();

        Mockito.when(cartService.modifyItemInCart(itemId, "PLUS")).thenReturn(Mono.empty());

        webTestClient.post().uri("/items/" + itemId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ItemActionRequest("PLUS"))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueMatches("Location", "/items/" + itemId);
    }

    @Test
    void modifyItemInCartFromItem_unauthorizedUser_shouldRedirectToItemPage() {
        UUID itemId = UUID.randomUUID();

        webTestClient.post().uri("/items/" + itemId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ItemActionRequest("PLUS"))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueMatches("Location", ".*/login");
    }

    @Test
    @WithMockUser(username = "test")
    void modifyItemInCartFromMain_paramsArePresent_shouldModifyCartAndRedirect() {
        UUID itemId = UUID.randomUUID();

        Mockito.when(cartService.modifyItemInCart(itemId, "PLUS")).thenReturn(Mono.empty());

        webTestClient.post().uri("/main/items/" + itemId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ItemActionRequest("PLUS"))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueMatches("Location", "/");
    }

    @Test
    void modifyItemInCartFromMain_unauthorizedUser_shouldRedirectToItemPage() {
        UUID itemId = UUID.randomUUID();

        webTestClient.post().uri("/main/items/" + itemId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ItemActionRequest("PLUS"))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueMatches("Location", ".*/login");
    }
}
