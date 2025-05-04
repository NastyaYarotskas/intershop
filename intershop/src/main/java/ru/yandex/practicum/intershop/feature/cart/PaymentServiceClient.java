package ru.yandex.practicum.intershop.feature.cart;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.OAuth2AuthorizeRequest;
import org.springframework.security.oauth2.client.OAuth2AuthorizedClient;
import org.springframework.security.oauth2.client.ReactiveOAuth2AuthorizedClientManager;
import org.springframework.security.oauth2.core.OAuth2AccessToken;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.UUID;

@Slf4j
@Component
public class PaymentServiceClient {

    @Autowired
    private ReactiveOAuth2AuthorizedClientManager manager;

    @Value("${payment.service.url}")
    private String baseUrl;

    public Mono<Balance> getCurrentBalance(UUID userId) {
        return retrieveToken()
                .flatMap(accessToken -> WebClient.create(baseUrl)
                        .get()
                        .uri("/payments/users/" + userId + "/balance")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .retrieve()
                        .bodyToMono(Balance.class)
                );
    }

    public Mono<Balance> makePayment(UUID userId, int amount) {
        return retrieveToken()
                .flatMap(accessToken -> WebClient.create(baseUrl)
                        .post()
                        .uri("/payments/users/" + userId+ "/pay?amount=" + amount)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                        .retrieve()
                        .bodyToMono(Balance.class)
                );
    }

    private Mono<String> retrieveToken() {
        return manager.authorize(OAuth2AuthorizeRequest
                        .withClientRegistrationId("yandex")
                        .principal("system")
                        .build())
                .map(OAuth2AuthorizedClient::getAccessToken)
                .map(OAuth2AccessToken::getTokenValue);
    }
}
