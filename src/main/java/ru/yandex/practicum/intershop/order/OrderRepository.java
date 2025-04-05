package ru.yandex.practicum.intershop.order;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface OrderRepository extends ReactiveCrudRepository<OrderEntity, UUID> {

    @Query("SELECT * FROM orders WHERE is_new = true LIMIT 1")
    Mono<OrderEntity> findFirstByIsNewTrue();

    @Query("SELECT * FROM orders WHERE is_new = true")
    Mono<OrderEntity> findByIsNewTrue();

    @Query("SELECT * FROM orders WHERE is_new = false")
    Flux<OrderEntity> findByIsNewFalse();
}
