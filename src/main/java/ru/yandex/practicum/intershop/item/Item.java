package ru.yandex.practicum.intershop.item;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.util.UUID;

@Data
@Table("items")
@NoArgsConstructor
@AllArgsConstructor
public class Item {
    @Id
    private UUID id;
    private String title;
    private String description;
    private String img;
    private int price;

    public Item(String title, String description, String img, int price) {
        this.title = title;
        this.description = description;
        this.img = img;
        this.price = price;
    }
}
