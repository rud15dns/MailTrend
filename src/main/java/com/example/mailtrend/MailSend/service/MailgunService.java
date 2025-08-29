package com.example.mailtrend.MailSend.service;


import io.micrometer.common.lang.Nullable;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.MultipartBodyBuilder;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Service
public class MailgunService {

    private final WebClient mailgun;

    @Value("${mailgun.domain}")
    private String domain;

    public MailgunService(WebClient mailgunWebClient) {
        this.mailgun = mailgunWebClient;
    }

    public Mono<String> sendSimpleEmail(String from, String to, String subject, String text, @Nullable String html) {
        String path = String.format("/v3/%s/messages", domain);

        MultipartBodyBuilder body = new MultipartBodyBuilder();
        body.part("from", from);                 // "손현빈 <no-reply@mg.yourdomain.com>"
        body.part("to", to);                     // 콤마로 여러명 가능
        body.part("subject", subject);
        if (html != null && !html.isBlank()) {
            body.part("html", html);
        } else {
            body.part("text", text);
        }

        // 필요시 태그/캠페인/예약발송 옵션(선택)
        // body.part("o:tag", "welcome");
        // body.part("o:deliverytime", "Fri, 29 Aug 2025 09:00:00 +0900");

        return mailgun.post()
                .uri(path)
                .contentType(MediaType.MULTIPART_FORM_DATA)
                .body(BodyInserters.fromMultipartData(body.build()))
                .retrieve()
                .onStatus(HttpStatusCode::isError, resp -> resp.bodyToMono(String.class)
                        .flatMap(msg -> Mono.error(new IllegalStateException("Mailgun error: " + msg))))
                .bodyToMono(String.class);
    }
}

