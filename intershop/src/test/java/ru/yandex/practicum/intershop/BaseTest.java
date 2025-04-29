package ru.yandex.practicum.intershop;

import com.redis.testcontainers.RedisContainer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import ru.yandex.practicum.intershop.config.TestSecurityConfig;

@Slf4j
@SpringBootTest
@Import(TestSecurityConfig.class)
public class BaseTest {

    private static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:latest")
            .withDatabaseName("intershop_db")
            .withUsername("test")
            .withPassword("test");

    private static final RedisContainer redis = new RedisContainer(DockerImageName.parse("redis:latest"))
                    .withExposedPorts(6379);

    static {
        postgres.start();
        redis.start();
    }

    @Autowired
    DatabaseClient databaseClient;

    @AfterEach
    void cleanDb() {
        databaseClient.sql("DELETE FROM orders_items; DELETE FROM orders").then().block();
    }

    @DynamicPropertySource
    static void registerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.r2dbc.url", () ->
                "r2dbc:postgresql://" + postgres.getHost() + ":" + postgres.getFirstMappedPort() + "/" + postgres.getDatabaseName());
        registry.add("spring.r2dbc.username", postgres::getUsername);
        registry.add("spring.r2dbc.password", postgres::getPassword);

        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", redis::getFirstMappedPort);
    }
}
