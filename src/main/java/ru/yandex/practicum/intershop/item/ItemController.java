package ru.yandex.practicum.intershop.item;

import jakarta.websocket.server.PathParam;
import org.aspectj.weaver.ast.Or;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import ru.yandex.practicum.intershop.order.Order;
import ru.yandex.practicum.intershop.order.OrderRepository;
import ru.yandex.practicum.intershop.order.OrderService;
import ru.yandex.practicum.intershop.orderitem.OrderItem;
import ru.yandex.practicum.intershop.orderitem.OrderItemId;
import ru.yandex.practicum.intershop.orderitem.OrderItemRepository;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Controller
public class ItemController {

    private final ItemService itemService;
    private final ItemMapper itemMapper;
    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final OrderItemRepository orderItemRepository;

    public ItemController(ItemService itemService, ItemMapper itemMapper, OrderService orderService, OrderRepository orderRepository, ItemRepository itemRepository, OrderItemRepository orderItemRepository) {
        this.itemService = itemService;
        this.itemMapper = itemMapper;
        this.orderService = orderService;
        this.orderRepository = orderRepository;
        this.itemRepository = itemRepository;
        this.orderItemRepository = orderItemRepository;
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

    @PostMapping("/main/items/{id}")
    public String modifyItemInCart(@PathVariable("id") UUID itemId, @PathParam("action") String action, Model model) {
        // 1. Найти или создать новый заказ
        Order activeOrder = orderRepository.findByIsNewTrue()
                .orElseGet(() -> {
                    Order newOrder = new Order();
                    newOrder.setNew(true);
                    return orderRepository.save(newOrder);
                });

        // 2. Найти товар по его id
        Item item = itemRepository.findById(itemId)
                .orElseThrow(() -> new RuntimeException("Item not found"));

        // 3. Найти или создать OrderItem для данного заказа и товара
        OrderItemId orderItemId = new OrderItemId(activeOrder.getId(), itemId);
        OrderItem orderItem = orderItemRepository.findById(orderItemId)
                .orElseGet(() -> {
                    OrderItem newOrderItem = new OrderItem();
                    newOrderItem.setId(orderItemId);
                    newOrderItem.setOrder(activeOrder);
                    newOrderItem.setItem(item);
                    newOrderItem.setCount(0); // Начальное количество
                    return orderItemRepository.save(newOrderItem);
                });

        // 4. Увеличить счетчик (count) в OrderItem
        if ("plus".equals(action)) {
            // Увеличить счетчик
            orderItem.setCount(orderItem.getCount() + 1);
            orderItemRepository.save(orderItem);
        } else if ("minus".equals(action)) {
            // Уменьшить счетчик (но не меньше 0)
            orderItem.setCount(Math.max(orderItem.getCount() - 1, 0));
            if (orderItem.getCount() == 0) {
                // Если счетчик стал 0, удаляем OrderItem
                orderItemRepository.delete(orderItem);
            } else {
                orderItemRepository.save(orderItem);
            }
        } else {
            throw new IllegalArgumentException("Invalid action: " + action);
        }

        return "redirect:/";
    }
}
