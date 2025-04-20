package ru.yandex.practicum.intershop.order;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface OrderRepository extends ReactiveCrudRepository<OrderEntity, UUID> {

    Mono<OrderEntity> findFirstByIsNewTrue();

    Mono<OrderEntity> findByIsNewTrue();

    Flux<OrderEntity> findByIsNewFalse();
}
