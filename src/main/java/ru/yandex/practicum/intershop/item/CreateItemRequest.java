package ru.yandex.practicum.intershop.item;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.Base64;

@Data
public class CreateItemRequest {
    private String title;
    private String description;
    private MultipartFile img;
    private int price;

    public String getBase64Img() {
        byte[] bytes = null;
        try {
            bytes = img.getBytes();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return Base64.getEncoder().encodeToString(bytes);
    }
}
