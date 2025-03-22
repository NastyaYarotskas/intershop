package ru.yandex.practicum.intershop.order;

import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
//@Transactional
public interface OrderRepository extends JpaRepository<Order, UUID> {

    Optional<Order> findFirstByIsNewTrue();

    Optional<Order> findByIsNewTrue();

    List<Order> findByIsNewFalse();
}
