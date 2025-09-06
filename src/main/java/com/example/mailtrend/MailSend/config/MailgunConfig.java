package com.example.mailtrend.MailSend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.web.reactive.function.client.WebClient;


import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Configuration
public class MailgunConfig {

    @Bean
    public WebClient mailgunWebClient(
            @Value("${mailgun.api-base}") String apiBase,
            @Value("${mailgun.api-key}") String apiKey
    ) {
        return WebClient.builder()
                .baseUrl(apiBase)
                .defaultHeaders(h -> {
                    // Mailgun은 Basic Auth: username 고정 "api", password = apiKey
                    String basic = "Basic " + Base64.getEncoder()
                            .encodeToString(("api:" + apiKey).getBytes(StandardCharsets.UTF_8));
                    h.set(HttpHeaders.AUTHORIZATION, basic);
                })
                .build();
    }
}