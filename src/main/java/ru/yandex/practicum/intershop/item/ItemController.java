package ru.yandex.practicum.intershop.item;

import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.intershop.error.EntityNotFoundException;
import ru.yandex.practicum.intershop.order.Order;
import ru.yandex.practicum.intershop.order.OrderService;
import ru.yandex.practicum.intershop.orderitem.OrderItemService;

import java.util.Base64;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
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
    public Mono<String> findItems(@ModelAttribute GetItemsRequest request, Model model) {
        return itemService.findAll(request)
                .flatMap(page -> {
                    List<Item> content = page.getContent();
                    List<ItemDto> items = ItemMapper.mapTo(content);

                    return orderService.findActiveOrder()
                            .defaultIfEmpty(new Order())
                            .flatMap(order -> {
                                // Update item counts from order
                                return Flux.fromIterable(items)
                                        .flatMap(itemDto -> orderItemService.findOrderItemCount(order.getId(), itemDto.getId())
                                                .doOnNext(itemDto::setCount)
                                                .thenReturn(itemDto))
                                        .collectList()
                                        .map(updatedItems -> {
                                            // Split into rows of 3 items
                                            List<List<ItemDto>> itemTable = IntStream.range(0, (updatedItems.size() + 2) / 3)
                                                    .mapToObj(i -> updatedItems.subList(i * 3, Math.min((i + 1) * 3, updatedItems.size())))
                                                    .collect(Collectors.toList());

                                            model.addAttribute("items", itemTable);

                                            int pageNumber = request.getPageNumber() == 0 ? 1 : request.getPageNumber();
                                            int pageSize = request.getPageSize() == 0 ? 10 : request.getPageSize();

                                            model.addAttribute("paging", new Paging(
                                                    pageNumber,
                                                    pageSize,
                                                    page.hasNext(),
                                                    page.hasPrevious()
                                            ));

                                            return "main";
                                        });
                            });
                });
    }


    @GetMapping("/items/{id}")
    public Mono<String> findItemById(@PathVariable("id") UUID itemId, Model model) {
        Mono<ItemDto> findItem = itemService.findById(itemId)
                .switchIfEmpty(Mono.error(new EntityNotFoundException(itemId)))
                .map(ItemMapper::mapTo);

        Mono<Integer> findItemsCount = orderService.findActiveOrderOrCreateNew()
                .flatMap(order -> orderItemService.findOrderItemCount(order.getId(), itemId));

        return Mono.zip(findItemsCount, findItem)
                .doOnNext(tuple -> {
                    ItemDto itemDto = tuple.getT2();
                    Integer count = tuple.getT1();
                    itemDto.setCount(count);
                    model.addAttribute("item", itemDto);
                })
                .thenReturn("item");
    }

    // todo
    @PostMapping(value = "/items", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Mono<String> save(@ModelAttribute Mono<CreateItemRequest> requestMono) {
        return requestMono
                .flatMap(request -> {
                    // Конвертируем файл в массив байтов реактивно
                    Mono<byte[]> fileBytesMono = DataBufferUtils.join(request.getImg().content())
                            .map(dataBuffer -> {
                                byte[] bytes = new byte[dataBuffer.readableByteCount()];
                                dataBuffer.read(bytes);
                                DataBufferUtils.release(dataBuffer);
                                return bytes;
                            });

                    return fileBytesMono.flatMap(bytes -> {
                        // Создаем сущность Item с байтами файла
                        Item item = new Item();
                        item.setTitle(request.getTitle());
                        item.setDescription(request.getDescription());
                        item.setPrice(request.getPrice());
                        String base64Image = Base64.getEncoder().encodeToString(bytes);
                        item.setImg(base64Image); // Предполагается, что у Item есть поле byte[] imageBytes

                        return itemService.save(item)
                                .thenReturn("redirect:/");
                    });
                });
    }
}
