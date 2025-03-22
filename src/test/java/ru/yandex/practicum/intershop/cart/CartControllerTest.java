package ru.yandex.practicum.intershop.cart;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.intershop.order.Order;
import ru.yandex.practicum.intershop.order.OrderService;

import java.util.Optional;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(CartController.class)
public class CartControllerTest {

    @MockitoBean
    CartService cartService;
    @MockitoBean
    OrderService orderService;
    @Autowired
    MockMvc mockMvc;

    @Test
    void getCart_orderExists_shouldAddOrderAttributeToModel() throws Exception {
        Mockito.when(orderService.findActiveOrder()).thenReturn(Optional.of(new Order()));

        mockMvc.perform(get("/cart/items"))
                .andExpect(status().isOk())
                .andExpect(view().name("cart"))
                .andExpect(model().attributeExists("order"));
    }

    @Test
    void buy_orderExists_shouldCompleteOrderAndRedirect() throws Exception {
        mockMvc.perform(post("/buy"))
                .andExpect(status().is3xxRedirection());

        Mockito.verify(orderService, Mockito.times(1)).completeOrder();
    }

    @Test
    void modifyItemInCartFromCart_paramsArePresent_shouldModifyCartAndRedirect() throws Exception {
        UUID itemId = UUID.randomUUID();
        mockMvc.perform(post("/cart/items/" + itemId)
                        .queryParam("action", "PLUS")
                )
                .andExpect(status().is3xxRedirection());

        Mockito.verify(cartService, Mockito.times(1)).modifyItemInCart(itemId, "PLUS");
    }

    @Test
    void modifyItemInCartFromItem_paramsArePresent_shouldModifyCartAndRedirect() throws Exception {
        UUID itemId = UUID.randomUUID();
        mockMvc.perform(post("/items/" + itemId)
                        .queryParam("action", "PLUS")
                )
                .andExpect(status().is3xxRedirection());

        Mockito.verify(cartService, Mockito.times(1)).modifyItemInCart(itemId, "PLUS");
    }

    @Test
    void modifyItemInCartFromMain_paramsArePresent_shouldModifyCartAndRedirect() throws Exception {
        UUID itemId = UUID.randomUUID();
        mockMvc.perform(post("/main/items/" + itemId)
                        .queryParam("action", "PLUS")
                )
                .andExpect(status().is3xxRedirection());

        Mockito.verify(cartService, Mockito.times(1)).modifyItemInCart(itemId, "PLUS");
    }
}
