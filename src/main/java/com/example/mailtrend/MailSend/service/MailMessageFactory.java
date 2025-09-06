package com.example.mailtrend.MailSend.service;


import com.example.mailtrend.Content.entity.MailContent;
import com.example.mailtrend.Content.entity.Source;
import com.example.mailtrend.Content.repository.MailContentRepository;
import com.example.mailtrend.MailSend.dto.MailMessage;
import io.micrometer.common.lang.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MailMessageFactory {

    private final MailContentRepository mailContentRepository;

    public MailMessage fromMailContent(Long mailContentId, String to,
                                       @Nullable String heroImageUrl,
                                       @Nullable String ctaUrl) {
        MailContent mc = mailContentRepository.findByIdWithSummaryAndSource(mailContentId)
                .orElseThrow(() -> new IllegalArgumentException("MailContent not found: " + mailContentId));

        Source src = mc.getSource();              // MailContent#getSource()에서 AiSummary를 통해 Source 반환
        String subject = src.getTitle();          // 제목 = Source.title
        String summary = mc.getAiSummary().getText(); // 요약 = AiSummary.text
        String original = null;                   // 필요시 원문 보관했다면 세팅

        String idempotencyKey = "mc:" + mailContentId + "|to:" + to; // 예시

        MailMessage msg = new MailMessage();
        msg.setTo(to);
        msg.setSubject(subject);
        msg.setSummary(summary);
        msg.setOriginal(original);
        msg.setIdempotencyKey(idempotencyKey);

        // 선택 필드(추가해도 역호환 OK)
        if (heroImageUrl != null) msg.setHeroImageUrl(heroImageUrl);
        if (ctaUrl != null) msg.setCtaUrl(ctaUrl);

        return msg;
    }
}
