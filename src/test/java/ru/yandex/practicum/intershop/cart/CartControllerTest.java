package ru.yandex.practicum.intershop.cart;

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
import ru.yandex.practicum.intershop.order.OrderEntity;
import ru.yandex.practicum.intershop.order.OrderService;
import ru.yandex.practicum.intershop.orderitem.OrderItemEntity;
import ru.yandex.practicum.intershop.orderitem.OrderItemService;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;

@AutoConfigureWebTestClient
public class CartControllerTest extends BaseTest {

    @MockitoBean
    OrderService orderService;
    @MockitoBean
    OrderItemService orderItemService;
    @MockitoBean
    ItemService itemService;
    @MockitoBean
    CartService cartService;
    @Autowired
    WebTestClient webTestClient;

    @Test
    void getCart_orderExists_shouldAddOrderAttributeToModel() {
        Mockito.when(orderService.findActiveOrderOrCreateNew()).thenReturn(Mono.just(new OrderEntity(UUID.randomUUID(), true)));
        Mockito.when(orderItemService.findOrderItems(any())).thenReturn(Flux.just(new OrderItemEntity()));
        Mockito.when(itemService.findById(any())).thenReturn(Mono.just(new ItemEntity()));

        webTestClient.get().uri("/cart/items")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.TEXT_HTML);
    }

    @Test
    void buy_orderExists_shouldCompleteOrderAndRedirect() {
        Mockito.when(orderService.completeOrder()).thenReturn(Mono.empty());

        webTestClient.post().uri("/buy")
                .exchange()
                .expectStatus().is3xxRedirection();

        Mockito.verify(orderService, Mockito.times(1)).completeOrder();
    }

    @Test
    void modifyItemInCartFromCart_paramsArePresent_shouldModifyCartAndRedirect() {
        UUID itemId = UUID.randomUUID();

        Mockito.when(cartService.modifyItemInCart(itemId, "PLUS")).thenReturn(Mono.empty());

        webTestClient.post().uri("/cart/items/" + itemId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ItemActionRequest("PLUS"))
                .exchange()
                .expectStatus().is3xxRedirection();
    }

    @Test
    void modifyItemInCartFromItem_paramsArePresent_shouldModifyCartAndRedirect() {
        UUID itemId = UUID.randomUUID();

        Mockito.when(cartService.modifyItemInCart(itemId, "PLUS")).thenReturn(Mono.empty());

        webTestClient.post().uri("/items/" + itemId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ItemActionRequest("PLUS"))
                .exchange()
                .expectStatus().is3xxRedirection();
    }

    @Test
    void modifyItemInCartFromMain_paramsArePresent_shouldModifyCartAndRedirect() {
        UUID itemId = UUID.randomUUID();

        Mockito.when(cartService.modifyItemInCart(itemId, "PLUS")).thenReturn(Mono.empty());

        webTestClient.post().uri("/main/items/" + itemId)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(new ItemActionRequest("PLUS"))
                .exchange()
                .expectStatus().is3xxRedirection();
    }
}
