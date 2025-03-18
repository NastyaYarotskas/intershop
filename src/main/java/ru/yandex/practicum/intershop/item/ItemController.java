package ru.yandex.practicum.intershop.item;

import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class ItemController {

    private final ItemService itemService;

    public ItemController(ItemService itemService) {
        this.itemService = itemService;
    }

    @GetMapping(value = {"/main/items", "/"})
    public String findAll(@ModelAttribute GetItemsRequest request, Model model) {
        Page<Item> foundItems = itemService.findAll(request);
        List<Item> itemList = foundItems.getContent();

        List<List<Item>> items = IntStream.range(0, (itemList.size() + 2) / 3)
                .mapToObj(i -> itemList.subList(i * 3, Math.min((i + 1) * 3, itemList.size())))
                .collect(Collectors.toList());

        model.addAttribute("items", items);

        int pageNumber = request.getPageNumber() == 0 ? 1 : request.getPageNumber();
        int pageSize = request.getPageSize() == 0 ? 10 : request.getPageSize();

        model.addAttribute("paging", new Paging(pageNumber, pageSize, foundItems.hasNext(), foundItems.hasPrevious()));
        return "main";
    }

    @GetMapping("/items/{id}")
    public String getById(@PathVariable("id") UUID id, Model model) {
        Item item = itemService.getById(id);

        model.addAttribute("item", item);

        return "item";
    }
}
