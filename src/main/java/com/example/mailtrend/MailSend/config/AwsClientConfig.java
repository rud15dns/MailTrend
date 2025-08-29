package com.example.mailtrend.MailSend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

import java.net.URI;

@Configuration
public class AwsClientConfig {

    @Bean
    public SqsAsyncClient sqsAsyncClient() {
        return SqsAsyncClient.builder()
                .region(Region.AP_NORTHEAST_2)                 // 리전 맞추기
                .credentialsProvider(DefaultCredentialsProvider.create()) // 기본 자격증명 체인
                 // .endpointOverride(URI.create("http://localhost:4566")) // (LocalStack 쓸 때만)
                .build();
    }
}

