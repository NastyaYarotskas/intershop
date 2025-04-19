package ru.yandex.practicum.intershop.item;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import reactor.core.publisher.Mono;

import java.util.Base64;
import java.util.UUID;

@Controller
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping(value = {"/main/items", "/"})
    public Mono<String> findItems(@ModelAttribute GetItemsRequest request, Model model) {
        return itemService.getPagedItemsWithOrderCounts(request)
                .doOnNext(result -> {
                    model.addAttribute("items", result.itemTable());
                    model.addAttribute("paging", result.paging());
                })
                .thenReturn("main");
    }

    @GetMapping("/items/{id}")
    public Mono<String> findItemById(@PathVariable("id") UUID itemId, Model model) {
        return itemService.getItemWithCount(itemId)
                .doOnNext(item -> model.addAttribute("item", item))
                .thenReturn("item");
    }

    @PostMapping(value = "/items", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<String> save(@ModelAttribute Mono<CreateItemRequest> requestMono) {
        return requestMono
                .flatMap(request -> {
                    Mono<byte[]> fileBytesMono = request.getImgAsBytes();

                    return fileBytesMono.flatMap(bytes -> {
                        ItemEntity item = new ItemEntity(request.getTitle(), request.getDescription(),
                                Base64.getEncoder().encodeToString(bytes), request.getPrice());

                        return itemService.save(item)
                                .thenReturn("redirect:/");
                    });
                });
    }
}
