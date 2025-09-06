package com.example.mailtrend.Content.service;

import com.example.mailtrend.Content.entity.AiSummary;
import com.example.mailtrend.Content.entity.MailContent;
import com.example.mailtrend.Content.entity.Source;
import com.example.mailtrend.Content.event.SourceCreatedEvent;
import com.example.mailtrend.Content.repository.AiSummaryRepository;
import com.example.mailtrend.Content.repository.MailContentRepository;
import com.example.mailtrend.Content.repository.SourceRepository;
import com.example.mailtrend.MailSend.dto.MailMessage;
import com.example.mailtrend.MailSend.service.MailEnqueueService;
import com.example.mailtrend.MailSend.service.SummaryService;
import com.example.mailtrend.oauth.repository.MemberRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;



@Service
@Slf4j
@RequiredArgsConstructor
public class SourceSummaryListener {

    private final AiSummaryRepository aiSummaryRepository;
    private final SourceRepository sourceRepository;
    private final SummaryService summaryService;
    private final MailContentRepository mailContentRepository;


    @Value("${summary.max-words:120}")
    private int defaultMaxWords;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSourceCreated(SourceCreatedEvent event) throws JsonProcessingException {
        Long sourceId = event.sourceId();

        // 1) 이미 요약 있으면 스킵
        if (aiSummaryRepository.existsBySourceId(sourceId)) return;

        // 2) Source 가져오기
        Source source = sourceRepository.findById(sourceId)
                .orElseThrow(() -> new IllegalStateException("Source not found: " + sourceId));

        System.out.println("Source 가져옴");

        // 3) 요약 실행 (title말고 description)
        String body;
        try {
            body = summaryService.summarize(source.getDescription(), defaultMaxWords);
        } catch (Exception e) {
            // 실패 시
            body = "(요약 생성 실패: " + e.getMessage() + ")";
        }

        // 4) 저장
        AiSummary savedAiSummary = aiSummaryRepository.save(new AiSummary(source, body));

        // 5) MailContent 생성 (나중에 불러올 때 한 번에 불러오기 편하도록)
        if (!mailContentRepository.existsByAiSummaryId(savedAiSummary.getId())) {
            MailContent savedMailContent =
                    mailContentRepository.save(new MailContent(savedAiSummary, java.time.LocalDateTime.now()));

            log.info("[savedMailContent] 제목 " + savedMailContent.getSource().getTitle() + "\n내용 : " + savedMailContent.getAiSummary().getText());
        }
    }
}