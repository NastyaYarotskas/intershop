package ru.yandex.practicum.feature.payment;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

import static org.springframework.security.test.web.reactive.server.SecurityMockServerConfigurers.mockJwt;

@SpringBootTest
@AutoConfigureWebTestClient
public class PaymentControllerTest {

    @Autowired
    WebTestClient webTestClient;

    @MockitoBean
    BalanceRepository balanceRepository;

    @Test
    void getBalance_allParamsAreSet_shouldReturnBalance() {
        Balance testBalance = new Balance(2000);
        UUID userId = UUID.randomUUID();

        Mockito.when(balanceRepository.getCurrentBalance(userId))
                .thenReturn(Mono.just(new Balance(2000)));

        webTestClient.mutateWith(mockJwt())
                .get()
                .uri("/payments/users/" + userId + "/balance")
                .exchange()
                .expectStatus().isOk()
                .expectBody(Balance.class)
                .isEqualTo(testBalance);
    }

    @Test
    void makePayment_sufficientFunds_shouldProcessPayment() {
        UUID userId = UUID.randomUUID();
        int paymentAmount = 500;
        Balance updatedBalance = new Balance(1500);

        Balance testBalance = new Balance(2000);

        Mockito.when(balanceRepository.getCurrentBalance(userId))
                .thenReturn(Mono.just(testBalance));

        Mockito.when(balanceRepository.updateBalance(userId, testBalance.getAmount() - paymentAmount))
                .thenReturn(Mono.just(updatedBalance));

        webTestClient.mutateWith(mockJwt())
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/payments/users/" + userId + "/pay")
                        .queryParam("amount", paymentAmount)
                        .build())
                .exchange()
                .expectStatus().isOk()
                .expectBody(Balance.class)
                .isEqualTo(updatedBalance);
    }

    @Test
    void makePayment_insufficientFunds_shouldReturnBadRequest() {
        UUID userId = UUID.randomUUID();
        Balance testBalance = new Balance(2000);
        int paymentAmount = 3000;

        Mockito.when(balanceRepository.getCurrentBalance(userId))
                .thenReturn(Mono.just(testBalance));

        webTestClient.mutateWith(mockJwt())
                .post()
                .uri(uriBuilder -> uriBuilder
                        .path("/payments/users/" + userId + "/pay")
                        .queryParam("amount", paymentAmount)
                        .build())
                .exchange()
                .expectStatus()
                .isBadRequest();
    }

    @Test
    void makePayment_withoutJwt_shouldReturnForbidden() {
        UUID userId = UUID.randomUUID();
        int paymentAmount = 3000;

        webTestClient.post()
                .uri(uriBuilder -> uriBuilder
                        .path("/payments/users/" + userId + "/pay")
                        .queryParam("amount", paymentAmount)
                        .build())
                .exchange()
                .expectStatus()
                .isUnauthorized();
    }
}
