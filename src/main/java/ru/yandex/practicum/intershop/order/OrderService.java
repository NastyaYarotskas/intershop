package ru.yandex.practicum.intershop.order;

import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class OrderService {

    private final OrderRepository orderRepository;

    public OrderService(OrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional
    public List<Order> findCompletedOrders() {
        return orderRepository.findByIsNewFalse();
    }

    @Transactional
    public Order getById(UUID id) {
        return orderRepository.findById(id).get();
    }

    public Optional<Order> findActiveOrder() {
        return orderRepository.findFirstByIsNewTrue();
    }

    public Order findActiveOrderOrCreateNew() {
        return orderRepository.findByIsNewTrue()
                .orElseGet(() -> {
                    Order newOrder = new Order();
                    newOrder.setNew(true);
                    return orderRepository.save(newOrder);
                });
    }

    public void completeOrder() {
        Optional<Order> newOrder = orderRepository.findByIsNewTrue();
        if (newOrder.isPresent()) {
            Order order = newOrder.get();
            order.setNew(false);
            orderRepository.save(order);
        }
    }
}
