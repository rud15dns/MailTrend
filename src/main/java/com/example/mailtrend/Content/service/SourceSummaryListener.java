package com.example.mailtrend.Content.service;

import com.example.mailtrend.Content.entity.AiSummary;
import com.example.mailtrend.Content.entity.Source;
import com.example.mailtrend.Content.event.SourceCreatedEvent;
import com.example.mailtrend.Content.repository.AiSummaryRepository;
import com.example.mailtrend.Content.repository.SourceRepository;
import com.example.mailtrend.MailSend.service.SummaryService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;


@Service
@RequiredArgsConstructor
public class SourceSummaryListener {

    private final AiSummaryRepository aiSummaryRepository;
    private final SourceRepository sourceRepository;
    private final SummaryService summaryService;

    @Value("${summary.max-words:120}")
    private int defaultMaxWords;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onSourceCreated(SourceCreatedEvent event) {
        Long sourceId = event.sourceId();

        // 1) 이미 요약 있으면 스킵
        if (aiSummaryRepository.existsBySourceId(sourceId)) return;

        // 2) Source 가져오기
        Source source = sourceRepository.findById(sourceId)
                .orElseThrow(() -> new IllegalStateException("Source not found: " + sourceId));

//        System.out.println("Source 가져옴");
        // 3) 요약 실행 (title만)
        String body;
        try {
            body = summaryService.summarize(source.getTitle(), defaultMaxWords);
        } catch (Exception e) {
            // 실패 시
            body = "(요약 생성 실패: " + e.getMessage() + ")";
        }

//        System.out.println("body" + body);

        // 4) 저장
        AiSummary summary = new AiSummary(source, body);
        aiSummaryRepository.save(summary);

        //
//        System.out.println("저장됨");
    }
}