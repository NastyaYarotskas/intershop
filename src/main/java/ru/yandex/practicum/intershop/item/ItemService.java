package ru.yandex.practicum.intershop.item;

import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.UUID;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

@Slf4j
@Service
public class ItemService {

    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Cacheable(value = "items", key = "#itemId.toString()", unless = "#result == null")
    public Mono<ItemEntity> findById(@Param("id") UUID itemId) {
        log.info("Fetching item from database for id: {}", itemId);
        return itemRepository.findById(itemId);
    }

    @Cacheable(
            value = "itemsPage",
            key = "{#request.pageNumber, #request.pageSize, #request.sort, #request.search}",
            unless = "#result == null"
    )
    public Mono<PageDto> findAll(GetItemsRequest request) {
        return findAllInternal(request)
                .map(PageDto::fromPage);
    }

    private Mono<Page<ItemEntity>> findAllInternal(GetItemsRequest request) {
        ItemService.Sort sort = switch (request.getSort()) {
            case NO -> new ItemService.Sort(DESC.name(), "title");
            case ALPHA -> new ItemService.Sort(ASC.name(), "title");
            case PRICE -> new ItemService.Sort(ASC.name(), "price");
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

    public record ItemsPageResult(List<List<Item>> itemTable, Paging paging) {
    }

    @CacheEvict(value = "itemsPage", allEntries = true)
    public Mono<Void> save(ItemEntity item) {
        return itemRepository.save(item);
    }

    record Sort(String direction, String property) {
    }
}
