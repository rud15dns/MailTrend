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

    private final MailgunService mailgunService;
    private final ObjectMapper objectMapper; // JSON 파싱

    @Value("${mailgun.from}")
    private String defaultFrom;

    // 표준 큐: 이름만 사용. 프로퍼티로 빼면 환경 차이 대응 쉬움
    @SqsListener(queueNames = "${app.sqs.mail-queue}")
    public void onMessage(@Payload String body) throws Exception {
        // ✅ SQS 바디(JSON) → MailMessage 역직렬화
        MailMessage msg = objectMapper.readValue(body, MailMessage.class);

        log.info("SQS received: to={}, subject={}, idempotencyKey={}",
                msg.getTo(), msg.getSubject(), msg.getIdempotencyKey());

        String text = composeText(msg.getSummary(), msg.getOriginal());
        String html = composeHtml(msg.getSummary(), msg.getOriginal());

        // 실패 시 예외 던져 재시도 (at-least-once 특성상 중복 주의)
        mailgunService
                .sendSimpleEmail(defaultFrom, msg.getTo(), msg.getSubject(), text, html)
                .doOnSuccess(r -> log.info("Mailgun sent: to={}, subject={}", msg.getTo(), msg.getSubject()))
                .doOnError(e -> log.warn("Mailgun send failed: to={}, subject={}, err={}",
                        msg.getTo(), msg.getSubject(), e.toString()))
                .block();
    }

    // ↓↓↓ 여기 둘이 같은 클래스 “안”에 있어야 합니다.
    private String composeText(String summary, String original) {
        return "[요약]\n" + (summary == null ? "" : summary) +
                "\n\n[원문]\n" + (original == null ? "" : original);
    }

    private String composeHtml(String summary, String original) {
        String safeSummary = summary == null ? "" : summary.replace("\n", "<br/>");
        String safeOriginal = original == null ? "" : original.replace("\n", "<br/>");
        return """
               <div style="font-family:system-ui,Segoe UI,Apple SD Gothic Neo,sans-serif">
                 <h3>요약</h3>
                 <div>%s</div>
                 <hr/>
                 <h3>원문</h3>
                 <div style="white-space:pre-wrap">%s</div>
               </div>
               """.formatted(safeSummary, safeOriginal);
    }
}
