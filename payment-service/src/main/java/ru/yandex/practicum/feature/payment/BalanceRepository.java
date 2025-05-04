package ru.yandex.practicum.feature.payment;

import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Repository
public class BalanceRepository {

    private static final Map<UUID, Balance> USER_TO_BALANCE = new HashMap<>();

    public Mono<Balance> getCurrentBalance(UUID userId) {
        Balance userBalance = USER_TO_BALANCE.computeIfAbsent(userId, id -> new Balance(10000));
        return Mono.just(userBalance);
    }

    public Mono<Balance> updateBalance(UUID userId, int newAmount) {
        Balance userBalance = USER_TO_BALANCE.get(userId);
        userBalance.setAmount(newAmount);
        return Mono.just(userBalance);
    }
}
