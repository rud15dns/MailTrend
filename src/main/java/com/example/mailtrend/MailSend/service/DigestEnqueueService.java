package com.example.mailtrend.MailSend.service;

import com.example.mailtrend.Content.entity.MailContent;
import com.example.mailtrend.Content.repository.MailContentRepository;
import com.example.mailtrend.MailSend.dto.MailMessage;
import com.example.mailtrend.MailSend.entity.IdempoUtil;
import com.example.mailtrend.oauth.entity.Category;
import com.example.mailtrend.oauth.repository.MemberRepository;
import io.micrometer.common.lang.Nullable;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
@Slf4j
@Service
@RequiredArgsConstructor
public class DigestEnqueueService {
    private final MailOrchestrator mailOrchestrator;
    private final MailContentRepository mailContentRepository;
    private final MemberRepository memberRepository;
    private final MailEnqueueService mailEnqueueService;
    private final S3Service s3Service;
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

    /** 해커톤용: 최신 5개(AI) 다이제스트를 즉시(동기)로 해당 사용자에게 발송 */
    @Transactional(readOnly = true)
    public void sendLatest5NowTo(String to,
                                 Category category,
                                 String subject,          // null이면 기본값
                                 String heroImageUrl,     // 옵션
                                 String ctaUrl) {         // 옵션
log.info("content 생성 잘되려함");
        List<MailContent> contents =
                mailContentRepository.findTop5ByAiSummary_Source_CategoryOrderByIdDesc(category);
        log.info("content 생성 잘되는중");
        if (contents.size() < 5) {
            log.info("5개미만이라 죽음");
            throw new IllegalStateException("다이제스트용 콘텐츠가 5개 미만입니다 (category=" + category + ")");
        }
        log.info("content 생성 잘됨");
        // 카드 변환
        log.info("content 카드 잘되려함");
        List<MailMessage.Card> cards = contents.stream()
                .map(mc -> MailMessage.Card.builder()
                        .title(mc.getSource().getTitle())
                        .desc(mc.getAiSummary().getText())
                        .link(mc.getSource().getLink())
                        .build())
                .toList();
        log.info("content 카드 생성 ");
        String finalSubject = (subject == null || subject.isBlank())
                ? "[MailTrend] 오늘의 " + category.name() + " 소식 5 - "
                : subject;
        log.info("content  finalsubject");
        // 멱등키: 수신자 + 제목 + mailContent id들
        String idsSeed = contents.stream().map(mc -> String.valueOf(mc.getId()))
                .reduce((a,b) -> a + "," + b).orElse("none");
        String idemp = com.example.mailtrend.MailSend.entity.IdempoUtil.sha256(to, finalSubject, idsSeed);
        log.info("mailmessage 생성");
        MailMessage msg = MailMessage.builder()
                .to(to)
                .subject(finalSubject)
                .idempotencyKey(idemp)
                .heroImageUrl(heroImageUrl)
                .ctaUrl(ctaUrl)
                .cards(cards)
                .build();
        log.info("동기 발송 직전  ");
        // ✅ 인라인(동기) 발송: 여기서 끝까지 보내고 반환
        Mono<String> sent = mailOrchestrator.sendMessage(msg);

        sent.block(); // 해커톤 요구: subscribe 요청 내에서 끝까지 수행
        log.info("동기 발생됨");
    }
}
