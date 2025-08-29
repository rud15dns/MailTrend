package com.example.mailtrend.MailSend.service;

import com.example.mailtrend.MailSend.dto.MailMessage;
import io.awspring.cloud.sqs.annotation.SqsListener;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
// (선택) SQS 메시지를 JSON -> 객체 매핑할 때 명시하고 싶으면
// import org.springframework.messaging.handler.annotation.Payload;
@Slf4j
@Component
@RequiredArgsConstructor
public class MailSendListener {

    private final MailgunService mailgunService;

    // 기본 발신자 (Run Configuration 또는 properties에 세팅)
    @Value("${mailgun.from}")
    private String defaultFrom;

    @SqsListener(queueNames = "mail-send")
    public void onMessage(MailMessage msg) {
        // 메시지 로그(민감정보는 넣지 말 것)
        log.info("SQS received: to={}, subject={}, idempotencyKey={}",
                msg.getTo(), msg.getSubject(), msg.getIdempotencyKey());

        // 본문 구성
        String text = composeBody(msg.getSummary(), msg.getOriginal());

        // 실패 시 예외를 던져서 메시지가 재시도되도록 block() 처리
        mailgunService
                .sendSimpleEmail(defaultFrom, msg.getTo(), msg.getSubject(), text, null)
                .doOnSuccess(r -> log.info("Mailgun sent: to={}, subject={}", msg.getTo(), msg.getSubject()))
                .doOnError(e -> log.warn("Mailgun send failed: to={}, subject={}, err={}",
                        msg.getTo(), msg.getSubject(), e.toString()))
                .block();
    }

    private String composeBody(String summary, String original) {
        return "[요약]\n" + (summary == null ? "" : summary) +
                "\n\n[원문]\n" + (original == null ? "" : original);
    }
}