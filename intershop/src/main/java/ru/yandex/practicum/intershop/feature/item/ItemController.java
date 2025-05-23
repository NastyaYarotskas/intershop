package ru.yandex.practicum.intershop.feature.item;

import org.springframework.data.domain.Page;
import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.intershop.feature.error.EntityNotFoundException;
import ru.yandex.practicum.intershop.feature.order.OrderService;
import ru.yandex.practicum.intershop.feature.orderitem.OrderItemService;
import ru.yandex.practicum.intershop.feature.user.CustomUserDetails;

import java.util.*;
import java.util.stream.IntStream;

@Controller
public class ItemController {

    private final ItemService itemService;
    private final OrderService orderService;
    private final OrderItemService orderItemService;

    public ItemController(ItemService itemService,
                          OrderService orderService,
                          OrderItemService orderItemService) {
        this.itemService = itemService;
        this.orderService = orderService;
        this.orderItemService = orderItemService;
    }

    @GetMapping(value = {"/main/items", "/"})
    public Mono<String> findItems(@AuthenticationPrincipal CustomUserDetails userDetails,
                                  @ModelAttribute GetItemsRequest request, Model model) {
        Optional<UUID> userId = userDetails == null ? Optional.empty() : Optional.of(userDetails.getUserId());
        return getPagedItemsWithOrderCounts(request, userId)
                .doOnNext(result -> {
                    model.addAttribute("items", result.itemTable());
                    model.addAttribute("paging", result.paging());
                })
                .thenReturn("main");
    }

    @GetMapping("/items/{id}")
    public Mono<String> findItemById(@AuthenticationPrincipal CustomUserDetails userDetails,
                                     @PathVariable("id") UUID itemId, Model model) {
        return getItemWithCount(itemId, userDetails.getUserId())
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

    public Mono<ItemService.ItemsPageResult> getPagedItemsWithOrderCounts(GetItemsRequest request, Optional<UUID> userId) {
        return itemService.findAll(request)
                .flatMap(page -> {
                    List<ItemEntity> content = page.getContent();
                    List<Item> items = ItemMapper.mapTo(content);
                    List<Item> orderedItems = new ArrayList<>(items);

                    return userId.map(id -> orderService.findActiveOrderId(id)
                            .flatMap(orderId -> updateItemsWithCounts(orderId, items, orderedItems))
                            .map(updatedItems -> createPageResult(request, page.toPage(), updatedItems))).orElseGet(() -> Mono.just(createPageResult(request, page.toPage(), items)));

                });
    }

    private Mono<List<Item>> updateItemsWithCounts(UUID orderId, List<Item> items, List<Item> orderedItems) {
        return Flux.fromIterable(items)
                .index()
                .flatMap(tuple -> {
                    Item item = tuple.getT2();
                    int originalIndex = tuple.getT1().intValue();

                    return orderItemService.findOrderItemCount(orderId, item.getId())
                            .doOnNext(count -> {
                                item.setCount(count);
                                orderedItems.set(originalIndex, item);
                            })
                            .thenReturn(item);
                })
                .then(Mono.just(orderedItems));
    }

    private ItemService.ItemsPageResult createPageResult(GetItemsRequest request, Page<ItemEntity> page, List<Item> items) {
        List<List<Item>> itemTable = IntStream.range(0, (items.size() + 2) / 3)
                .mapToObj(i -> items.subList(i * 3, Math.min((i + 1) * 3, items.size())))
                .toList();

        int pageNumber = request.getPageNumber() == 0 ? 1 : request.getPageNumber();
        int pageSize = request.getPageSize() == 0 ? 10 : request.getPageSize();

        Paging paging = new Paging(
                pageNumber,
                pageSize,
                page.hasNext(),
                page.hasPrevious()
        );

        return new ItemService.ItemsPageResult(itemTable, paging);
    }

    public Mono<Item> getItemWithCount(UUID itemId, UUID userId) {
        Mono<Item> itemMono = itemService.findById(itemId)
                .switchIfEmpty(Mono.error(new EntityNotFoundException(itemId)))
                .map(ItemMapper::mapTo);

        Mono<Integer> countMono = orderService.findActiveOrderOrCreateNew(userId)
                .flatMap(order -> orderItemService.findOrderItemCount(order.getId(), itemId));

        return Mono.zip(countMono, itemMono)
                .map(tuple -> {
                    Item item = tuple.getT2();
                    item.setCount(tuple.getT1());
                    return item;
                });
    }
}
