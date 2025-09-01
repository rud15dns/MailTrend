package com.example.mailtrend.MailSend.controller;

import com.example.mailtrend.MailSend.dto.SummarizeSendReq;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Max;

import com.example.mailtrend.MailSend.dto.MailMessage;
import com.example.mailtrend.MailSend.entity.IdempoUtil;
import com.example.mailtrend.MailSend.service.MailgunService;
import com.example.mailtrend.MailSend.service.SummaryService;
import io.awspring.cloud.sqs.operations.SqsTemplate;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
@RestController
@RequestMapping("/mail")
@RequiredArgsConstructor
public class MailController {

    private final SummaryService summaryService;
    private final SqsTemplate sqsTemplate;
    private final com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    @Value("${app.sqs.mail-queue}")
    String queueName;

    @PostMapping("/summarize-send")
    public Mono<ResponseEntity<String>> summarizeAndEnqueue(@RequestBody @Valid SummarizeSendReq req) {
        return Mono.fromCallable(() -> {
                    String summary = summaryService.summarize(req.original(), req.maxWords());

                    String idempo = IdempoUtil.sha256(req.to(), req.subject(), req.original());
                    MailMessage msg = MailMessage.builder()
                            .to(req.to()).subject(req.subject())
                            .summary(summary).original(req.original())
                            .idempotencyKey(idempo).build();

                    // ✅ JSON으로 직렬화해서 SQS에 넣기
                    String body = objectMapper.writeValueAsString(msg);
                    sqsTemplate.send(queueName, body);

                    return "enqueued";
                }).map(ResponseEntity::ok)
                .onErrorResume(e -> Mono.just(ResponseEntity.status(500).body("enqueue failed: " + e.getMessage())));
    }

}