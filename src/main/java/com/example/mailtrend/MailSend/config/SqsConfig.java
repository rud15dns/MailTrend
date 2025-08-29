package com. example. mailtrend. MailSend. config;

import io.awspring.cloud.sqs.config.SqsMessageListenerContainerFactory;

import io.awspring.cloud.sqs.listener.QueueNotFoundStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.services.sqs.SqsAsyncClient;

@Configuration
public class SqsConfig {

    @Bean
    SqsMessageListenerContainerFactory<Object> defaultSqsListenerContainerFactory(SqsAsyncClient client) {
        return SqsMessageListenerContainerFactory
                .builder()
                .sqsAsyncClient(client) // ★ factory builder에 붙입니다.
                .configure(options -> options
                        .queueNotFoundStrategy(QueueNotFoundStrategy.FAIL) // ★ options에는 이거만
                )
                .build();
    }
}
