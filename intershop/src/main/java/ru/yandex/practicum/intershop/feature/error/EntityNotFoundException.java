package ru.yandex.practicum.intershop.feature.error;

import java.util.UUID;

public class EntityNotFoundException extends RuntimeException {
    public EntityNotFoundException(UUID id) {
        super("Сущность с ID " + id + " не найдена");
    }
}
