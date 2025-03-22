package ru.yandex.practicum.intershop.item;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

@Service
public class ItemService {

    private final ItemRepository itemRepository;

    public ItemService(ItemRepository itemRepository) {
        this.itemRepository = itemRepository;
    }

    @Transactional
    public Page<Item> findAll(GetItemsRequest request) {
        Sort sort = switch (request.getSort()) {
            case NO -> Sort.by(Sort.Direction.DESC,"title");
            case ALPHA -> Sort.by(Sort.Direction.ASC, "title");
            case PRICE -> Sort.by(Sort.Direction.ASC, "price");
        };

        int page = request.getPageNumber() == 0 ? 1 : request.getPageNumber();
        int size = request.getPageSize() == 0 ? 10 : request.getPageSize();

        PageRequest pageRequest = PageRequest.of(page - 1, size, sort);

        if (request.getSearch() == null || request.getSearch().isEmpty()) {
            return itemRepository.findAll(pageRequest);
        }

        return itemRepository.findByTitleContainingIgnoreCase(request.getSearch(), pageRequest);
    }

    public Optional<Item> findById(UUID id) {
        return itemRepository.findById(id);
    }
}
