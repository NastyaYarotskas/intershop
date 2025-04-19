package ru.yandex.practicum.intershop.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.intershop.error.EntityNotFoundException;
import ru.yandex.practicum.intershop.order.OrderService;
import ru.yandex.practicum.intershop.orderitem.OrderItemService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

@Slf4j
@Service
public class ItemService {

    private final ItemRepository itemRepository;
    private final OrderService orderService;
    private final OrderItemService orderItemService;
    private final ItemEntityService itemEntityService;

    public ItemService(ItemRepository itemRepository,
                       OrderService orderService,
                       OrderItemService orderItemService, ItemEntityService itemEntityService) {
        this.itemRepository = itemRepository;
        this.orderService = orderService;
        this.orderItemService = orderItemService;
        this.itemEntityService = itemEntityService;
    }

    public Mono<PageCacheDto> findAll(GetItemsRequest request) {
        return itemEntityService.findAllCache(request);
    }

    public Mono<Page<ItemEntity>> findAllInternal(GetItemsRequest request) {
        Sort sort = switch (request.getSort()) {
            case NO -> new Sort(DESC.name(), "title");
            case ALPHA -> new Sort(ASC.name(), "title");
            case PRICE -> new Sort(ASC.name(), "price");
        };

        int page = request.getPageNumber() == 0 ? 1 : request.getPageNumber();
        int size = request.getPageSize() == 0 ? 10 : request.getPageSize();

        Pageable pageRequest = PageRequest.of(page - 1, size);

        if (request.getSearch() == null || request.getSearch().isEmpty()) {
            return itemRepository.findAllBy(pageRequest.getPageSize(), pageRequest.getOffset(), sort.property(), sort.direction())
                    .collectList()
                    .zipWith(itemRepository.count())
                    .map(tuple -> new PageImpl<>(tuple.getT1(), pageRequest, tuple.getT2()));
        }

        return itemRepository.findByTitleContainingIgnoreCase(request.getSearch(), pageRequest.getPageSize(), pageRequest.getOffset())
                .collectList()
                .zipWith(itemRepository.countByTitleContainingIgnoreCase(request.getSearch()))
                .map(tuple -> new PageImpl<>(tuple.getT1(), pageRequest, tuple.getT2()));
    }

    public Mono<ItemEntity> findById(UUID id) {
        return itemRepository.findById(id);
    }

    public Mono<Item> getItemWithCount(UUID itemId) {
        Mono<Item> itemMono = itemEntityService.findById(itemId)
                .switchIfEmpty(Mono.error(new EntityNotFoundException(itemId)))
                .map(ItemMapper::mapTo);

        Mono<Integer> countMono = orderService.findActiveOrderOrCreateNew()
                .flatMap(order -> orderItemService.findOrderItemCount(order.getId(), itemId));

        return Mono.zip(countMono, itemMono)
                .map(tuple -> {
                    Item item = tuple.getT2();
                    item.setCount(tuple.getT1());
                    return item;
                });
    }

    public Mono<ItemsPageResult> getPagedItemsWithOrderCounts(GetItemsRequest request) {
        return findAll(request)
                .flatMap(page -> {
                    List<ItemEntity> content = page.getContent();
                    List<Item> items = ItemMapper.mapTo(content);
                    List<Item> orderedItems = new ArrayList<>(items);

                    return orderService.findActiveOrderId()
                            .flatMap(orderId -> updateItemsWithCounts(orderId, items, orderedItems))
                            .map(updatedItems -> createPageResult(request, page.toPage(), updatedItems));
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

    private ItemsPageResult createPageResult(GetItemsRequest request, Page<ItemEntity> page, List<Item> items) {
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

        return new ItemsPageResult(itemTable, paging);
    }

    public record ItemsPageResult(List<List<Item>> itemTable, Paging paging) {
    }

    public Mono<Void> save(ItemEntity item) {
        return itemRepository.save(item);
    }

    record Sort(String direction, String property) {
    }
}
