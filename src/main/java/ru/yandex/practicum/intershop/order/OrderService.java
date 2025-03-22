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

//    @Transactional
    public Optional<Order> findActiveOrder() {
        return orderRepository.findFirstByIsNewTrue();
    }

//    @Transactional
    public Order createNewOrder() {
        Order order = new Order();
        order.setNew(true);
        return orderRepository.save(order);
    }
}
