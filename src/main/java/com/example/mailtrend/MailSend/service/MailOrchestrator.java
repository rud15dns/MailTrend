package com.example.mailtrend.MailSend.service;

import com.example.mailtrend.Content.entity.MailContent;
import com.example.mailtrend.Content.entity.Source;
import com.example.mailtrend.Content.repository.MailContentRepository;
import com.example.mailtrend.Content.service.SourceService;
import com.example.mailtrend.MailSend.dto.MailMessage;
import io.micrometer.common.lang.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.util.List;
@Service
@RequiredArgsConstructor
public class MailOrchestrator {

    private final MailTemplateService templateService;
    private final MailgunService mailgunService;
    private final S3Service s3Service;
    private final HtmlSnapshotService htmlSnapshotService;
    @Value("${mailgun.from}")
    private String from;
    public Mono<String> sendMessage(MailMessage msg) {
        String html = templateService.renderSummaryEmail(msg);
        String text = "[HTML 메일] 브라우저에서 열어 보세요.";



        // 1) HTML -> PNG
        byte[] png = htmlSnapshotService.htmlToPng(html, /*targetWidthPx*/ 600);

        // 2) S3 업로드 (파일명은 멱등키 기준)
        String snapshotUrl = s3Service.upload(
                png,
                msg.getIdempotencyKey() + ".png",
                "image/png"
        );

        // (선택) 스냅샷 URL을 메일 본문 상단에 삽입하고 싶다면 다음처럼 가공:
        // html = "<div style='text-align:center'><img src='" + snapshotUrl + "' style='max-width:100%'/></div>" + html;

        // 3) 메일 발송 (HTML은 그대로)

        return mailgunService.sendSimpleEmail(
                (from == null || from.isBlank()) ? "no-reply@example.com" : from,
                msg.getTo(), msg.getSubject(), text, html
        ).doOnSuccess(r -> {
            // 필요 시 로그/DB에 snapshotUrl 저장
            // e.g., log.info("snapshot uploaded: {}", snapshotUrl);
        });


    }

    public Mono<String> sendDigest(String to, String subject,
                                   List<MailContent> contents,
                                   @Nullable String heroImageUrl,
                                   @Nullable String ctaUrl) {

        List<MailMessage.Card> cards = contents.stream()
                .map(mc -> MailMessage.Card.builder()
                        .title(mc.getSource().getTitle())
                        .desc(mc.getAiSummary().getText())
                        .link(mc.getSource().getLink())
                        .build())
                .toList();

        MailMessage msg = MailMessage.builder()
                .to(to)
                .subject(subject)
                .idempotencyKey("digest:" + to + ":" + System.currentTimeMillis())
                .heroImageUrl(heroImageUrl)
                .ctaUrl(ctaUrl)
                .cards(cards)
                .build();

        String html = templateService.renderSummaryEmail(msg);
        String text = "[HTML 메일] 브라우저에서 열어 보세요.";
        return mailgunService.sendSimpleEmail(
                (from == null || from.isBlank()) ? "no-reply@example.com" : from,
                to, subject, text, html
        );
    }
}
