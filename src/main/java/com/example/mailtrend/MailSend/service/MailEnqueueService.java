package com.example.mailtrend.MailSend.service;

import com.example.mailtrend.MailSend.dto.MailMessage;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailEnqueueService {
    private final SqsTemplate sqsTemplate;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Value("${app.sqs.mail-queue}")
    String queueName;

    public void enqueue(MailMessage msg) {
        try {
            String json = objectMapper.writeValueAsString(msg);
            sqsTemplate.send(opts -> opts
                    .queue(queueName)
                    .payload(json));
        } catch (Exception e) {
            throw new IllegalStateException("Failed to enqueue MailMessage", e);
        }
    }
}
