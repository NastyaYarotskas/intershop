package ru.yandex.practicum.intershop.item;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.springframework.data.domain.Sort.Direction.ASC;
import static org.springframework.data.domain.Sort.Direction.DESC;

@Service
public class ItemService {

    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    public Mono<Page<Item>> findAll(GetItemsRequest request) {
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

    public Mono<Item> findById(UUID id) {
        return itemRepository.findById(id);
    }

    public Mono<Void> save(Item item) {
        return itemRepository.save(item);
    }

    record Sort(String direction, String property) {
    }
}
