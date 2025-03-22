package ru.yandex.practicum.intershop.item;

import jakarta.persistence.*;
import lombok.Data;

import java.util.UUID;

@Data
@Entity
@Table(name = "items")
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
}
