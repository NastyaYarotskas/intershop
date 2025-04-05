package ru.yandex.practicum.intershop.item;

import lombok.Data;
import org.springframework.http.codec.multipart.FilePart;

@Data
public class CreateItemRequest {
    private String title;
    private String description;
    private FilePart img;
    private int price;
}
