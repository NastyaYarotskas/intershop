package ru.yandex.practicum.intershop.item;

import lombok.Data;

@Data
public class GetItemsRequest {
    private String search;
    private Sort sort = Sort.NO;
    private int pageSize;
    private int pageNumber;

    public enum Sort {
        NO, ALPHA, PRICE
    }
}
