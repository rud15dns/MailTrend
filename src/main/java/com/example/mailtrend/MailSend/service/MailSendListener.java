package com.example.mailtrend.MailSend.service;

import com.example.mailtrend.MailSend.dto.MailMessage;
import com.example.mailtrend.MailSend.service.MailgunService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import io.awspring.cloud.sqs.annotation.SqsListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class MailSendListener {

    private final ObjectMapper objectMapper;
    private final MailOrchestrator mailOrchestrator;
    @Value("${mailgun.from}")
    private String defaultFrom;

    // 표준 큐: 이름만 사용. 프로퍼티로 빼면 환경 차이 대응 쉬움

    @SqsListener(queueNames = "${app.sqs.mail-queue}")
    public void onMessage(@Payload String body) throws Exception {
        MailMessage msg = objectMapper.readValue(body, MailMessage.class);
        log.info("SQS received: to={}, subject={}, idempotencyKey={}",
                msg.getTo(), msg.getSubject(), msg.getIdempotencyKey());

        mailOrchestrator
                .sendMessage(msg)
                .doOnSuccess(r -> log.info("Mail sent: to={}, subject={}", msg.getTo(), msg.getSubject()))
                .doOnError(e -> log.warn("Mail send failed: to={}, subject={}, err={}",
                        msg.getTo(), msg.getSubject(), e.toString()))
                .block(); // 재시도 제어 필요 시 블로킹 유지
    }



}
