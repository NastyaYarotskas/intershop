package ru.yandex.practicum.intershop.feature.orderitem;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

public interface OrderItemRepository extends ReactiveCrudRepository<OrderItemEntity, OrderItemId> {

    Mono<OrderItemEntity> findByOrderIdAndItemId(UUID orderId, UUID itemId);

    @Modifying
    @Query("UPDATE orders_items SET count = :count WHERE order_id = :orderId AND item_id = :itemId")
    Mono<Void> updateCount(@Param("orderId") UUID orderId, @Param("itemId") UUID itemId, @Param("count") int count);

    @Modifying
    @Query("DELETE FROM orders_items WHERE order_id = :orderId AND item_id = :itemId")
    Mono<Void> delete(@Param("orderId") UUID orderId, @Param("itemId") UUID itemId);

    Flux<OrderItemEntity> findByOrderId(UUID orderId);
}
