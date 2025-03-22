package ru.yandex.practicum.intershop.item;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.intershop.BaseTest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@AutoConfigureMockMvc
public class ItemControllerTest extends BaseTest {

    @MockitoBean
    ItemService itemService;
    @Autowired
    MockMvc mockMvc;

    @ParameterizedTest
    @ValueSource(strings = {"/main/items", "/"})
    void findItems_requestIsValid_shouldAddItemsAndPagingToModelAttributes(String url) throws Exception {
        Item firstItem = new Item(UUID.fromString("550e8400-e29b-41d4-a716-446655440006"), "Электронная книга PocketBook 740", "7.8\", 32 ГБ, сенсорный экран, Wi-Fi", "", 19990);
        Item secondItem = new Item(UUID.fromString("550e8400-e29b-41d4-a716-446655440008"), "Фотоаппарат Canon EOS R6", "20 Мп, беззеркальный, 4K видео, Wi-Fi", "", 179990);

        Page<Item> items = new PageImpl<>(List.of(firstItem, secondItem));

        Mockito.when(itemService.findAll(Mockito.any())).thenReturn(items);

        mockMvc.perform(get(url))
                .andExpect(status().isOk())
                .andExpect(view().name("main"))
                .andExpect(model().attributeExists("items"))
                .andExpect(model().attributeExists("paging"));
    }

    @Test
    void findItemById_itemIsPresent_shouldAddFoundItemToModelAttributes() throws Exception {
        UUID itemId = UUID.fromString("550e8400-e29b-41d4-a716-446655440006");

        Item item = new Item(itemId, "Электронная книга PocketBook 740", "7.8\", 32 ГБ, сенсорный экран, Wi-Fi", "", 19990);

        Mockito.when(itemService.findById(itemId)).thenReturn(Optional.of(item));

        mockMvc.perform(get("/items/" + itemId))
                .andExpect(status().isOk())
                .andExpect(view().name("item"))
                .andExpect(model().attributeExists("item"));
    }

    @Test
    void findItemById_itemIsNotPresent_shouldRedirectToErrorPage() throws Exception {
        UUID itemId = UUID.fromString("550e8400-e29b-41d4-a716-446655440006");

        mockMvc.perform(get("/items/" + itemId))
                .andExpect(status().isOk())
                .andExpect(view().name("error"))
                .andExpect(model().attributeExists("errorMessage"));
    }
}
