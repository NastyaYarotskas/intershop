package ru.yandex.practicum.intershop.item;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.io.Serializable;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PageDto implements Serializable {
    private List<ItemEntity> content;
    private int number;
    private int size;
    private long totalElements;

    public static PageDto fromPage(Page<ItemEntity> page) {
        return new PageDto(
                page.getContent(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements()
        );
    }

    public Page<ItemEntity> toPage() {
        return new PageImpl<>(
                content,
                PageRequest.of(number, size),
                totalElements
        );
    }
}
