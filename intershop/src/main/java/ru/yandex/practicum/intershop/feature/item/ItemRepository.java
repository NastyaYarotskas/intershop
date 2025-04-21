package ru.yandex.practicum.intershop.feature.item;

import org.springframework.data.r2dbc.repository.Modifying;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Repository
public interface ItemRepository extends ReactiveCrudRepository<ItemEntity, UUID> {

    @Modifying
    @Query("""
            INSERT INTO items (price, title, description, img)
            VALUES (:#{#item.price}, :#{#item.title}, :#{#item.description}, :#{#item.img})
            """)
    Mono<Void> save(@Param("item") ItemEntity item);

    @Query("SELECT id, price, description, title, img FROM items i WHERE id = :id")
    Mono<ItemEntity> findById(@Param("id") UUID id);

    @Query("SELECT id, price, description, title, img FROM items WHERE LOWER(title) LIKE LOWER(CONCAT('%', :title, '%')) " +
           "ORDER BY title LIMIT :limit OFFSET :offset")
    Flux<ItemEntity> findByTitleContainingIgnoreCase(@Param("title") String title, @Param("limit") int limit, @Param("offset") long offset);

    @Query("SELECT COUNT(*) FROM items WHERE LOWER(title) LIKE LOWER(CONCAT('%', :title, '%'))")
    Mono<Long> countByTitleContainingIgnoreCase(@Param("title") String title);

    @Query("""
            SELECT id, price, description, title, img
            FROM items
            ORDER BY
                CASE WHEN :sortDirection = 'ASC' AND :sortBy = 'price' THEN price ELSE NULL END ASC,
                CASE WHEN :sortDirection = 'ASC' AND :sortBy = 'title' THEN title ELSE NULL END ASC,
                CASE WHEN :sortDirection = 'DESC' AND :sortBy = 'title' THEN title ELSE NULL END DESC
            LIMIT :limit
            OFFSET :offset
            """)
    Flux<ItemEntity> findAllBy(
            @Param("limit") int limit,
            @Param("offset") long offset,
            @Param("sortBy") String sortBy,
            @Param("sortDirection") String sortDirection);
}
