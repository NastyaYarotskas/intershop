package ru.yandex.practicum.intershop.feature.order;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface OrderRepository extends ReactiveCrudRepository<OrderEntity, UUID> {

    Mono<OrderEntity> findFirstByIsNewTrueAndUserId(UUID userId);

    Mono<OrderEntity> findByIsNewTrueAndUserId(UUID userId);

    Flux<OrderEntity> findByIsNewFalseAndUserId(UUID userId);
}
