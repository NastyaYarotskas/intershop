package ru.yandex.practicum.intershop.feature.item;

import lombok.Data;
import org.springframework.core.io.buffer.DataBufferUtils;
import org.springframework.http.codec.multipart.FilePart;
import reactor.core.publisher.Mono;

@Data
public class CreateItemRequest {
    private String title;
    private String description;
    private FilePart img;
    private int price;

    public Mono<byte[]> getImgAsBytes() {
        return DataBufferUtils.join(img.content())
                .map(dataBuffer -> {
                    byte[] bytes = new byte[dataBuffer.readableByteCount()];
                    dataBuffer.read(bytes);
                    DataBufferUtils.release(dataBuffer);
                    return bytes;
                });
    }
}
