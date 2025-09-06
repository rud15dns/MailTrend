package com.example.mailtrend.MailSend.service;


import com.example.mailtrend.Content.entity.MailContent;
import com.example.mailtrend.Content.repository.MailContentRepository;
import com.example.mailtrend.MailSend.dto.MailMessage;
import com.example.mailtrend.MailSend.entity.IdempoUtil;
import com.example.mailtrend.oauth.entity.Category;
import com.example.mailtrend.oauth.repository.MemberRepository;
import io.micrometer.common.lang.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
@Service
@RequiredArgsConstructor
public class DigestEnqueueService {

    private final MailContentRepository mailContentRepository;
    private final MemberRepository memberRepository;
    private final MailEnqueueService mailEnqueueService;

    @Transactional(readOnly = true)
    public int enqueueDigestByCategory(Category category,
                                       String subject,
                                       @Nullable String heroImageUrl,
                                       @Nullable String ctaUrl) {

        List<MailContent> contents =
                mailContentRepository.findTop5ByAiSummary_Source_CategoryOrderByIdDesc(category);
        if (contents.isEmpty()) return 0;

        // 카드 변환(5개)
        List<MailMessage.Card> cards = contents.stream()
                .map(mc -> MailMessage.Card.builder()
                        .title(mc.getSource().getTitle())
                        .desc(mc.getAiSummary().getText())
                        .link(mc.getSource().getLink())
                        .build())
                .toList();

        List<String> recipients = memberRepository.findEmailsByCategory(category);
        if (recipients.isEmpty()) return 0;

        String idsSeed = contents.stream().map(mc -> String.valueOf(mc.getId()))
                .reduce((a,b) -> a + "," + b).orElse("none");

        int count = 0;
        for (String to : recipients) {
            String idemp = IdempoUtil.sha256(to, subject, idsSeed); // 멱등키

            MailMessage msg = MailMessage.builder()
                    .to(to)
                    .subject(subject)
                    .idempotencyKey(idemp)
                    .heroImageUrl(heroImageUrl)
                    .ctaUrl(ctaUrl)
                    .cards(cards) // ✅ 다이제스트(5칸)
                    .build();

            mailEnqueueService.enqueue(msg); // ✅ 큐로
            count++;
        }
        return count;
    }

    // ===== 아래 2개가 새로 추가되는 "단일 수신자"용 큐잉 메서드 =====

    /** 특정 수신자에게 '지정된 5개 MailContent'로 다이제스트 큐잉 */
    @Transactional(readOnly = true)
    public void enqueueDigestTo(String to,
                                String subject,
                                List<Long> mailContentIds,
                                @Nullable String heroImageUrl,
                                @Nullable String ctaUrl) {

        if (mailContentIds == null || mailContentIds.size() != 5) {
            throw new IllegalArgumentException("mailContentIds must contain exactly 5 ids");
        }

        // 요약/소스까지 로딩
        List<MailContent> contents =
                mailContentRepository.findAllByIdFetchSummaryAndSource(mailContentIds);
        if (contents.size() != 5) {
            throw new IllegalArgumentException("Some MailContent ids are invalid");
        }

        List<MailMessage.Card> cards = contents.stream()
                .map(mc -> MailMessage.Card.builder()
                        .title(mc.getSource().getTitle())
                        .desc(mc.getAiSummary().getText())
                        .link(mc.getSource().getLink())
                        .build())
                .toList();

        String idsSeed = mailContentIds.stream().map(String::valueOf)
                .reduce((a,b) -> a + "," + b).orElse("none");

        String idemp = IdempoUtil.sha256(to, subject, idsSeed);

        MailMessage msg = MailMessage.builder()
                .to(to)
                .subject(subject)
                .idempotencyKey(idemp)
                .heroImageUrl(heroImageUrl)
                .ctaUrl(ctaUrl)
                .cards(cards) // ✅ 5칸
                .build();

        mailEnqueueService.enqueue(msg); // ✅ 큐에 싣기 (리스너가 발송)
    }
}