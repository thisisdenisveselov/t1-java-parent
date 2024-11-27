package ru.t1.java.demo.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import ru.t1.java.demo.model.dto.CheckBlacklistResponse;
import ru.t1.java.demo.util.JwtUtils;

import java.util.UUID;

@Slf4j
@Component
public class CheckBlacklistWebClient {

    private final WebClient webClient;
    private final JwtUtils jwtUtils;

    @Value("${web.resources.blacklist-check}")
    private String resource;

    public CheckBlacklistWebClient(WebClient webClient, JwtUtils jwtUtils) {
        this.webClient = webClient;
        this.jwtUtils = jwtUtils;
    }

    public CheckBlacklistResponse check(UUID clientId, UUID accountId) {

        log.debug("Старт запроса с clientId {} и accountId {}", clientId, accountId);
        CheckBlacklistResponse get;
        try {
            get = webClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path(resource)
                            .queryParam("clientId", clientId)
                            .queryParam("accountId", accountId)
                            .build())
                    .header(HttpHeaders.AUTHORIZATION, jwtUtils.generateJwtToken())
                    .retrieve()
                    .bodyToMono(CheckBlacklistResponse.class)
                    .block();


        } catch (Exception httpStatusException) {
            throw httpStatusException;
        }

        log.debug("Финиш запроса с clientId {} и accountId {}", clientId, accountId);
        return get;
    }
}
