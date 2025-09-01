package com.example.mailtrend.MailSend.service;

import com.example.mailtrend.MailSend.dto.MailMessage;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailEnqueueService {
    private final SqsTemplate sqsTemplate;
    @Value("${app.sqs.mail-queue}")
    String queueName;
    public void enqueue(MailMessage msg) {
        sqsTemplate.send(opts -> opts
                .queue(queueName)   // URL 대신 이름!
                .payload(msg));
    }
}
