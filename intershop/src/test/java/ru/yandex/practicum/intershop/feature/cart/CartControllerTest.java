package ru.yandex.practicum.intershop.feature.cart;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.intershop.BaseTest;
import ru.yandex.practicum.intershop.feature.order.Order;
import ru.yandex.practicum.intershop.feature.order.OrderService;
import ru.yandex.practicum.intershop.feature.user.CustomReactiveUserDetailsService;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;

@AutoConfigureWebTestClient
public class CartControllerTest extends BaseTest {

    @MockitoBean
    OrderService orderService;
    @MockitoBean
    CartService cartService;
    @MockitoBean
    PaymentServiceClient paymentServiceClient;
    @MockitoBean
    CustomReactiveUserDetailsService customReactiveUserDetailsService;
    @Autowired
    WebTestClient webTestClient;

    @Test
    void getCart_orderExists_shouldAddOrderAttributeToModel() {
        Mockito.when(paymentServiceClient.getCurrentBalance(any())).thenReturn(Mono.just(new Balance(200)));
        Mockito.when(orderService.findActiveOrderOrCreateNew(any())).thenReturn(Mono.just(new Order(UUID.randomUUID(), List.of())));

        Mockito.when(customReactiveUserDetailsService.findByUsername(any())).thenReturn(Mono.just(mockUser));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser(mockUser))
                .get().uri("/cart/items")
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
    void makePayment_orderExists_shouldCompleteOrderAndRedirect() {
        Mockito.when(paymentServiceClient.makePayment(any(), anyInt())).thenReturn(Mono.just(new Balance(200)));
        Mockito.when(orderService.findActiveOrderOrCreateNew(any())).thenReturn(Mono.just(new Order()));
        Mockito.when(orderService.completeOrder(any())).thenReturn(Mono.empty());
        Mockito.when(customReactiveUserDetailsService.findByUsername(any())).thenReturn(Mono.just(mockUser));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser(mockUser))
                .post().uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueMatches("Location", "/");

        Mockito.verify(orderService, Mockito.times(1)).completeOrder(any());
    }

    @Test
    void makePayment_unauthorizedUser_shouldRedirectToLoginPage() {
        Mockito.when(paymentServiceClient.makePayment(any(), anyInt())).thenReturn(Mono.just(new Balance(200)));
        Mockito.when(orderService.findActiveOrderOrCreateNew(any())).thenReturn(Mono.just(new Order()));
        Mockito.when(orderService.completeOrder(any())).thenReturn(Mono.empty());

        webTestClient.post().uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueMatches("Location", ".*/login");

        Mockito.verify(orderService, Mockito.times(0)).completeOrder(any());
    }

    @Test
    void modifyItemInCartFromCart_paramsArePresent_shouldModifyCartAndRedirect() {
        UUID itemId = UUID.randomUUID();

        Mockito.when(cartService.modifyItemInCart(itemId, "PLUS", testUserId)).thenReturn(Mono.empty());
        Mockito.when(customReactiveUserDetailsService.findByUsername(any())).thenReturn(Mono.just(mockUser));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser(mockUser))
                .post().uri("/cart/items/" + itemId)
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
    void modifyItemInCartFromItem_paramsArePresent_shouldModifyCartAndRedirect() {
        UUID itemId = UUID.randomUUID();

        Mockito.when(cartService.modifyItemInCart(itemId, "PLUS", testUserId)).thenReturn(Mono.empty());
        Mockito.when(customReactiveUserDetailsService.findByUsername(any())).thenReturn(Mono.just(mockUser));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser(mockUser))
                .post().uri("/items/" + itemId)
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
    void modifyItemInCartFromMain_paramsArePresent_shouldModifyCartAndRedirect() {
        UUID itemId = UUID.randomUUID();

        Mockito.when(cartService.modifyItemInCart(itemId, "PLUS", testUserId)).thenReturn(Mono.empty());
        Mockito.when(customReactiveUserDetailsService.findByUsername(any())).thenReturn(Mono.just(mockUser));

        webTestClient
                .mutateWith(SecurityMockServerConfigurers.mockUser(mockUser))
                .post().uri("/main/items/" + itemId)
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
