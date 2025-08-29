package com.example.mailtrend.MailSend.service;

import com.example.mailtrend.MailSend.dto.MailMessage;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailEnqueueService {
    private final SqsTemplate sqsTemplate; // spring-cloud-aws 제공
    @Value("${app.sqs.queue-url}") String queueUrl;

    public void enqueue(MailMessage msg) {
        // FIFO면 groupId/deduplicationId를 헤더로 세팅
        sqsTemplate.send(sqsSendOptions -> sqsSendOptions
                .queue(queueUrl)
                .messageGroupId(msg.getTo())
                .messageDeduplicationId(msg.getIdempotencyKey())
                .payload(msg));
    }
}