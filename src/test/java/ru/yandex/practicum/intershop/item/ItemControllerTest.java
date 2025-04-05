package ru.yandex.practicum.intershop.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.reactive.function.BodyInserters;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.intershop.BaseTest;
import ru.yandex.practicum.intershop.order.OrderEntity;
import ru.yandex.practicum.intershop.order.OrderService;
import ru.yandex.practicum.intershop.orderitem.OrderItemService;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;

@AutoConfigureWebTestClient
public class ItemControllerTest extends BaseTest {

    @MockitoBean
    OrderService orderService;
    @MockitoBean
    OrderItemService orderItemService;
    @MockitoBean
    ItemService itemService;
    @Autowired
    WebTestClient webTestClient;

    @ParameterizedTest
    @ValueSource(strings = {"/main/items", "/"})
    void findItems_requestIsValid_shouldAddItemsAndPagingToModelAttributes(String url) {
        ItemEntity firstItem = new ItemEntity(UUID.fromString("550e8400-e29b-41d4-a716-446655440006"), "Электронная книга PocketBook 740", "7.8\", 32 ГБ, сенсорный экран, Wi-Fi", "", 19990);
        ItemEntity secondItem = new ItemEntity(UUID.fromString("550e8400-e29b-41d4-a716-446655440008"), "Фотоаппарат Canon EOS R6", "20 Мп, беззеркальный, 4K видео, Wi-Fi", "", 179990);

        UUID orderId = UUID.fromString("550e8400-e29b-41d4-a716-446655440007");

        Page<ItemEntity> items = new PageImpl<>(List.of(firstItem, secondItem));

        Mockito.when(itemService.findAll(any())).thenReturn(Mono.just(items));
        Mockito.when(orderService.findActiveOrder()).thenReturn(Mono.just(new OrderEntity(orderId, true)));
        Mockito.when(orderItemService.findOrderItemCount(orderId, UUID.fromString("550e8400-e29b-41d4-a716-446655440006"))).thenReturn(Mono.just(1));
        Mockito.when(orderItemService.findOrderItemCount(orderId, UUID.fromString("550e8400-e29b-41d4-a716-446655440008"))).thenReturn(Mono.just(1));

        webTestClient.get().uri(url)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(response -> {
                    String responseBody = new String(response.getResponseBody());
                    assertTrue(responseBody.contains("Электронная книга PocketBook 740"));
                    assertTrue(responseBody.contains("Фотоаппарат Canon EOS R6"));
                });
    }

    @Test
    void findItemById_itemIsPresent_shouldAddFoundItemToModelAttributes() throws Exception {
        UUID itemId = UUID.fromString("550e8400-e29b-41d4-a716-446655440006");

        ItemEntity item = new ItemEntity(itemId, "Электронная книга PocketBook 740", "7.8\", 32 ГБ, сенсорный экран, Wi-Fi", "", 19990);

        UUID orderId = UUID.fromString("550e8400-e29b-41d4-a716-446655440007");

        Mockito.when(itemService.findById(itemId)).thenReturn(Mono.just(item));
        Mockito.when(orderService.findActiveOrderOrCreateNew()).thenReturn(Mono.just(new OrderEntity(orderId, true)));
        Mockito.when(orderItemService.findOrderItemCount(orderId, itemId)).thenReturn(Mono.just(1));

        webTestClient.get().uri("/items/{id}", itemId)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .consumeWith(response -> {
                    String responseBody = new String(response.getResponseBody());
                    assertTrue(responseBody.contains("Электронная книга PocketBook 740"));
                    assertTrue(responseBody.contains("19990"));
                });
    }

    @Test
    void findItemById_itemIsNotPresent_shouldRedirectToErrorPage() throws Exception {
        UUID itemId = UUID.fromString("550e8400-e29b-41d4-a716-446655440006");

        UUID orderId = UUID.fromString("550e8400-e29b-41d4-a716-446655440007");

        Mockito.when(itemService.findById(itemId)).thenReturn(Mono.empty());
        Mockito.when(orderService.findActiveOrderOrCreateNew()).thenReturn(Mono.just(new OrderEntity(orderId, true)));
        Mockito.when(orderItemService.findOrderItemCount(orderId, itemId)).thenReturn(Mono.just(1));

        webTestClient.get().uri("/items/{id}", itemId)
                .exchange()
                .expectStatus().isOk()
                .expectBody(String.class)
                .value(body -> {
                    assertTrue(body.contains("Ошибка"));
                    assertTrue(body.contains("Сущность с ID 550e8400-e29b-41d4-a716-446655440006 не найдена"));
                });
    }

    @Test
    void save_allParamsAreSet_shouldCreateItemAndRedirectToTheMainPage() throws Exception {
        Mockito.when(itemService.save(any(ItemEntity.class)))
                .thenReturn(Mono.empty());

        MultipartBodyBuilder builder = new MultipartBodyBuilder();
        builder.part("img", "test image".getBytes())
                .filename("test.jpg")
                .contentType(MediaType.IMAGE_JPEG);
        builder.part("title", "title");
        builder.part("description", "description");
        builder.part("price", "123");

        webTestClient.post()
                .uri("/items")
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(builder.build()))
                .exchange()
                .expectStatus().is3xxRedirection()
                .expectHeader().valueEquals("Location", "/");
    }
}
