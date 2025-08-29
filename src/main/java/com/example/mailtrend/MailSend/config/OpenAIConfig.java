package com.example.mailtrend.MailSend.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import okhttp3.OkHttpClient;
import okhttp3.HttpUrl;

@Configuration
public class OpenAIConfig {
    @Value("${openai.api.base}") String apiBase;
    @Value("${openai.api.key}") String apiKey;

    @Bean
    public OkHttpClient openAiHttp() {
        return new OkHttpClient.Builder().build();
    }

    @Bean
    public HttpUrl openAiResponsesUrl() {
        return HttpUrl.parse(apiBase + "/v1/responses");
    }

    @Bean
    public String openAiAuthHeader() {
        return "Bearer " + apiKey;
    }
}