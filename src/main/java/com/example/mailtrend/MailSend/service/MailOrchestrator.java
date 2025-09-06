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

    private final S3Service s3Service;
    private final MailTemplateService templateService;
    private final MailgunService mailgunService;

    private final MailContentRepository mailContentRepository;

    @Value("${mailgun.from}")
    private String from;

    /** MailContent 1건 기반으로 바로 전송 (이미지 URL/CTA 옵션) */
    public Mono<String> sendFromMailContent(Long mailContentId, String to,
                                            @Nullable String heroImageUrl,
                                            @Nullable String ctaUrl) {
        MailContent mc = mailContentRepository.findByIdWithSummaryAndSource(mailContentId)
                .orElseThrow(() -> new IllegalArgumentException("MailContent not found: " + mailContentId));

        Source src = mc.getSource(); // MailContent#getSource() 제공

        // 템플릿 렌더링 (sources는 단일 리스트로 넘김)
        String html = templateService.renderSummaryEmail(
                // MailMessage 없이 바로 렌더링하려면 오버로드 하나 만들어도 됨
                new MailMessage(to, src.getTitle(), mc.getAiSummary().getText(), null,
                        "mc:"+mailContentId+"|to:"+to, heroImageUrl, ctaUrl),
                List.of(src),
                heroImageUrl,
                ctaUrl);

        String text = "[HTML 메일] 브라우저에서 열어 보세요.";
        return mailgunService.sendSimpleEmail(from, to, src.getTitle(), text, html);
    }

    /** 이미지 파일 받아 업로드 후 MailContent 기반 전송 */
    public Mono<String> sendFromMailContentWithFile(Long mailContentId, String to,
                                                    MultipartFile heroFile,
                                                    @Nullable String ctaUrl) throws IOException {
        String heroUrl = s3Service.upload(heroFile);
        return sendFromMailContent(mailContentId, to, heroUrl, ctaUrl);
    }

    /** 기존 MailMessage 경로(큐 메시지 기반)도 계속 지원 */
    public Mono<String> sendWithUrl(MailMessage msg, @Nullable String heroImageUrl, @Nullable String ctaUrl) {
        // MailMessage 안에 heroImageUrl/ctaUrl이 이미 들어왔다면 그대로 사용
        String hero = heroImageUrl != null ? heroImageUrl : msg.getHeroImageUrl();
        String cta  = ctaUrl != null ? ctaUrl : msg.getCtaUrl();

        // MailMessage 기반 템플릿 (sources는 필요 시 단일 Source 없이도 렌더되게 템플릿/서비스를 허용)
        String html = templateService.renderSummaryEmail(msg, /*sources*/ List.of(), hero, cta);
        String text = "[HTML 메일] 브라우저에서 열어 보세요.";
        return mailgunService.sendSimpleEmail(nullSafeFrom(), msg.getTo(), msg.getSubject(), text, html);
    }

    private String nullSafeFrom() {
        return (from == null || from.isBlank()) ? "no-reply@example.com" : from;
    }
}