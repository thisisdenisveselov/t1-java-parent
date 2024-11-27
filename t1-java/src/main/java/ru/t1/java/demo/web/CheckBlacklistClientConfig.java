package ru.t1.java.demo.web;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@RequiredArgsConstructor
public class CheckBlacklistClientConfig {
    @Value("${web.base-url}")
    private String baseURL;

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder.baseUrl(baseURL).build();
    }
}
