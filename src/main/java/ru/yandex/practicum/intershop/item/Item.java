package ru.yandex.practicum.intershop.item;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Entity
@Table(name = "items")
@NoArgsConstructor
@AllArgsConstructor
public class Item {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String title;
    private String description;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    private String img;

    private int price;

    public Item(String title, String description, String img, int price) {
        this.title = title;
        this.description = description;
        this.img = img;
        this.price = price;
    }
}
