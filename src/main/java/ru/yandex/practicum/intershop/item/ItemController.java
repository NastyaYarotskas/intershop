package ru.yandex.practicum.intershop.item;

import org.aspectj.weaver.ast.Or;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import ru.yandex.practicum.intershop.order.Order;
import ru.yandex.practicum.intershop.order.OrderService;
import ru.yandex.practicum.intershop.orderitem.OrderItem;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class ItemController {

    private final ItemService itemService;
    private final ItemMapper itemMapper;
    private final OrderService orderService;

    public ItemController(ItemService itemService, ItemMapper itemMapper, OrderService orderService) {
        this.itemService = itemService;
        this.itemMapper = itemMapper;
        this.orderService = orderService;
    }

    @GetMapping(value = {"/main/items", "/"})
    @Transactional(readOnly = true)
    public String findAll(@ModelAttribute GetItemsRequest request, Model model) {
        Page<Item> foundItems = itemService.findAll(request);
        List<Item> itemList = foundItems.getContent();

        List<ItemDto> itemDtos = itemList.stream().map(itemMapper::mapTo).toList();

        Order order = orderService.findActiveOrder().orElse(new Order());
        for (ItemDto item : itemDtos) {
            for (OrderItem orderItem : order.getItems()) {
                if (item.getId().equals(orderItem.getId().getItemId())) {
                    item.setCount(orderItem.getCount());
                }
            }
        }

        List<List<ItemDto>> items = IntStream.range(0, (itemDtos.size() + 2) / 3)
                .mapToObj(i -> itemDtos.subList(i * 3, Math.min((i + 1) * 3, itemDtos.size())))
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
