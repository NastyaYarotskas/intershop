package ru.yandex.practicum.intershop.item;

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
import ru.yandex.practicum.intershop.orderitem.OrderItemService;

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
    @Transactional(readOnly = true)
    public String findItems(@ModelAttribute GetItemsRequest request, Model model) {
        Page<Item> foundItems = itemService.findAll(request);
        List<Item> content = foundItems.getContent();

        List<ItemDto> items = ItemMapper.mapTo(content);

        Order order = orderService.findActiveOrder().orElse(new Order());

        for (ItemDto item : items) {
            for (OrderItem orderItem : order.getItems()) {
                if (item.getId().equals(orderItem.getId().getItemId())) {
                    item.setCount(orderItem.getCount());
                }
            }
        }

        List<List<ItemDto>> itemTable = IntStream.range(0, (items.size() + 2) / 3)
                .mapToObj(i -> items.subList(i * 3, Math.min((i + 1) * 3, items.size())))
                .collect(Collectors.toList());

        model.addAttribute("items", itemTable);

        int pageNumber = request.getPageNumber() == 0 ? 1 : request.getPageNumber();
        int pageSize = request.getPageSize() == 0 ? 10 : request.getPageSize();

        model.addAttribute("paging", new Paging(pageNumber, pageSize,
                foundItems.hasNext(), foundItems.hasPrevious()));
        return "main";
    }

    @GetMapping("/items/{id}")
    public String findItemById(@PathVariable("id") UUID itemId, Model model) {
        Order activeOrder = orderService.findActiveOrderOrCreateNew();

        Item item = itemService.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        int orderItemCount = orderItemService.findOrderItem(activeOrder, item)
                .map(OrderItem::getCount)
                .orElse(0);

        ItemDto itemDto = ItemMapper.mapTo(item);
        itemDto.setCount(orderItemCount);

        model.addAttribute("item", itemDto);

        return "item";
    }
}
