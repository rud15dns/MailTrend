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
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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
            String idemp = IdempoUtil.sha256(to, subject, idsSeed);

            MailMessage msg = MailMessage.builder()
                    .to(to)
                    .subject(subject)
                    .idempotencyKey(idemp)
                    .heroImageUrl(heroImageUrl)
                    .ctaUrl(ctaUrl)
                    .cards(cards)
                    .build();

            mailEnqueueService.enqueue(msg);
            count++;
        }
        return count;
    }

    @Transactional(readOnly = true)
    public void enqueueDigestTo(String to,
                                String subject,
                                List<Long> mailContentIds,
                                @Nullable String heroImageUrl,
                                @Nullable String ctaUrl) {

        if (mailContentIds == null || mailContentIds.size() != 5) {
            throw new IllegalArgumentException("mailContentIds must contain exactly 5 ids");
        }

        // ✅ 연관까지 로딩
        List<MailContent> fetched = mailContentRepository.findAllByIdIn(mailContentIds);
        if (fetched.size() < 5) {
            throw new IllegalArgumentException("Some MailContent ids are invalid");
        }

        // ✅ 입력 ID 순서로 재정렬
        Map<Long, MailContent> byId = fetched.stream()
                .collect(Collectors.toMap(MailContent::getId, Function.identity()));

        List<MailContent> contents = mailContentIds.stream()
                .map(id -> {
                    MailContent mc = byId.get(id);
                    if (mc == null) throw new IllegalArgumentException("Invalid MailContent id: " + id);
                    return mc;
                })
                .toList();

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
                .cards(cards)
                .build();

        mailEnqueueService.enqueue(msg);
    }
}
