package ru.yandex.practicum.feature.payment;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.yandex.practicum.feature.error.InsufficientFundsException;

import java.util.UUID;

@RestController
@RequestMapping("/payments/users/")
@RequiredArgsConstructor
@Tag(name = "Сервис Платежей", description = "Управление платежами и балансом")
public class PaymentController {

    private final BalanceRepository balanceRepository;

    @GetMapping("{userId}/balance")
    @Operation(summary = "Получить текущий баланс")
    @ApiResponse(responseCode = "200", description = "Успешное получение баланса")
    public Mono<Balance> getBalance(@Parameter(description = "ID пользователя") @PathVariable UUID userId) {
        return balanceRepository.getCurrentBalance(userId);
    }

    @PostMapping("{userId}/pay")
    @Operation(summary = "Осуществить платеж")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Платеж успешно обработан"),
            @ApiResponse(responseCode = "400", description = "Недостаточно средств")
    })
    public Mono<Balance> makePayment(@Parameter(description = "ID пользователя") @PathVariable UUID userId,
                                     @Parameter(description = "Сумма платежа") @RequestParam int amount) {
        return balanceRepository.getCurrentBalance(userId)
                .flatMap(currentBalance -> {
                    if (currentBalance.getAmount() < amount) {
                        return Mono.error(new InsufficientFundsException(currentBalance.getAmount(), amount));
                    }
                    return balanceRepository.updateBalance(userId, currentBalance.getAmount() - amount);
                });
    }
}
