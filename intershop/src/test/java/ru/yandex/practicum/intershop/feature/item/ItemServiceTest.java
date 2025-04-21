package ru.yandex.practicum.intershop.feature.item;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.intershop.BaseTest;
import ru.yandex.practicum.intershop.feature.item.GetItemsRequest;
import ru.yandex.practicum.intershop.feature.item.ItemEntity;
import ru.yandex.practicum.intershop.feature.item.ItemService;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

public class ItemServiceTest extends BaseTest {

    @Autowired
    ItemService itemService;

    @Test
    void findAll_withPageAndSize_shouldReturnPagedItems() {
        GetItemsRequest request = new GetItemsRequest();
        request.setPageNumber(1);
        request.setPageSize(2);

        itemService.findAll(request)
                .doOnNext(page -> {
                    assertThat(page.getSize()).isEqualTo(2);
                    assertThat(page.getContent())
                            .usingRecursiveComparison()
                            .ignoringFields("img")
                            .isEqualTo(List.of(
                                    new ItemEntity(UUID.fromString("550e8400-e29b-41d4-a716-446655440006"),
                                            "Электронная книга PocketBook 740",
                                            "7.8\", 32 ГБ, сенсорный экран, Wi-Fi", "", 19990),
                                    new ItemEntity(UUID.fromString("550e8400-e29b-41d4-a716-446655440008"),
                                            "Фотоаппарат Canon EOS R6",
                                            "20 Мп, беззеркальный, 4K видео, Wi-Fi", "", 179990)
                            ));
                })
                .block();
    }

    @Test
    void findAll_withAlphaSort_shouldReturnSortedItems() {
        GetItemsRequest request = new GetItemsRequest();
        request.setPageNumber(1);
        request.setPageSize(2);
        request.setSort(GetItemsRequest.Sort.ALPHA);

        itemService.findAll(request)
                .doOnNext(page -> {
                    assertThat(page.getSize()).isEqualTo(2);
                    assertThat(page.getContent())
                            .usingRecursiveComparison()
                            .ignoringFields("img")
                            .isEqualTo(List.of(
                                    new ItemEntity(UUID.fromString("550e8400-e29b-41d4-a716-446655440007"),
                                            "Игровая консоль PlayStation 5",
                                            "825 ГБ SSD, 4K, Blu-ray, беспроводной геймпад", "", 79990),
                                    new ItemEntity(UUID.fromString("550e8400-e29b-41d4-a716-446655440011"),
                                            "Клавиатура Logitech G Pro X",
                                            "Механическая, RGB, программируемые клавиши", "", 12990)
                            ));
                })
                .block();
    }

    @Test
    void findAll_withPriceSort_shouldReturnSortedItems() {
        GetItemsRequest request = new GetItemsRequest();
        request.setPageNumber(1);
        request.setPageSize(2);
        request.setSort(GetItemsRequest.Sort.PRICE);

        itemService.findAll(request)
                .doOnNext(page -> {
                    assertThat(page.getSize()).isEqualTo(2);
                    assertThat(page.getContent())
                            .usingRecursiveComparison()
                            .ignoringFields("img")
                            .isEqualTo(List.of(
                                    new ItemEntity(UUID.fromString("550e8400-e29b-41d4-a716-446655440005"),
                                            "Фитнес-браслет Xiaomi Mi Band 6",
                                            "1.56\" AMOLED, мониторинг сна, пульса, SpO2", "", 3990),
                                    new ItemEntity(UUID.fromString("550e8400-e29b-41d4-a716-446655440011"),
                                            "Клавиатура Logitech G Pro X",
                                            "Механическая, RGB, программируемые клавиши", "", 12990)
                            ));
                })
                .block();
    }

    @Test
    void findAll_withSearch_shouldReturnItemsContainingSearchStringInTitle() {
        GetItemsRequest request = new GetItemsRequest();
        request.setPageNumber(1);
        request.setPageSize(2);
        request.setSearch("Фитнес");

        itemService.findAll(request)
                .doOnNext(page -> {
                    assertThat(page.getTotalElements()).isEqualTo(1);
                    assertThat(page.getContent())
                            .usingRecursiveComparison()
                            .ignoringFields("img")
                            .isEqualTo(List.of(
                                    new ItemEntity(UUID.fromString("550e8400-e29b-41d4-a716-446655440005"),
                                            "Фитнес-браслет Xiaomi Mi Band 6",
                                            "1.56\" AMOLED, мониторинг сна, пульса, SpO2", "", 3990)
                            ));
                })
                .block();
    }
}