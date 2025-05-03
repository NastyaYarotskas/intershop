package ru.yandex.practicum.intershop;

import com.redis.testcontainers.RedisContainer;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.utility.DockerImageName;
import ru.yandex.practicum.intershop.config.TestSecurityConfig;
import ru.yandex.practicum.intershop.feature.user.CustomUserDetails;
import ru.yandex.practicum.intershop.feature.user.UserEntity;
import ru.yandex.practicum.intershop.feature.user.UserRepository;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

@Slf4j
@SpringBootTest
@Import(TestSecurityConfig.class)
public class BaseTest {

    protected UUID testUserId;
    protected CustomUserDetails mockUser;

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

    @Autowired
    UserRepository userRepository;

    @BeforeEach
    void setUp() {
        UserEntity userEntity = UserEntity.builder()
                .username("test")
                .password("test")
                .active(true)
                .roles("USER")
                .build();
        this.testUserId = Objects.requireNonNull(userRepository.save(userEntity).block()).getId();
        this.mockUser = CustomUserDetails.customUserDetailsBuilder()
                .userId(this.testUserId)
                .username("test")
                .password("test")
                .authorities(List.of(new SimpleGrantedAuthority("ROLE_USER")))
                .build();
    }

    @AfterEach
    void cleanDb() {
        databaseClient.sql("DELETE FROM orders_items; DELETE FROM orders").then().block();
        userRepository.deleteById(testUserId).then().block();
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
