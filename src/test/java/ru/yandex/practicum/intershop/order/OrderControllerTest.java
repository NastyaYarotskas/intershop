package ru.yandex.practicum.intershop.order;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(OrderController.class)
public class OrderControllerTest {

    @MockitoBean
    OrderService orderService;
    @Autowired
    MockMvc mockMvc;

    @Test
    void findAll_validRequest_shouldAddOrdersToModelAttributes() throws Exception {
        mockMvc.perform(get("/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("orders"))
                .andExpect(model().attributeExists("orders"));
    }

    @Test
    void findOrderById_orderIsPresent_shouldAddFoundOrderToModelAttributes() throws Exception {
        UUID orderId = UUID.fromString("550e8400-e29b-41d4-a716-446655440006");

        Order order = new Order(orderId, true, List.of());

        Mockito.when(orderService.findById(orderId)).thenReturn(Optional.of(order));

        mockMvc.perform(get("/orders/" + orderId))
                .andExpect(status().isOk())
                .andExpect(view().name("order"))
                .andExpect(model().attributeExists("order"));
    }

    @Test
    void findOrderById_orderIsNotPresent_shouldRedirectToErrorPage() throws Exception {
        UUID orderId = UUID.fromString("550e8400-e29b-41d4-a716-446655440006");

        mockMvc.perform(get("/orders/" + orderId))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attributeExists("errorMessage"));
    }
}
