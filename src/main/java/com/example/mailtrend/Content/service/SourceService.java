package com.example.mailtrend.Content.service;

import com.example.mailtrend.Content.entity.Source;
import com.example.mailtrend.Content.event.SourceCreatedEvent;
import com.example.mailtrend.Content.repository.SourceRepository;
import com.example.mailtrend.oauth.entity.Category;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SourceService {

    private final SourceRepository sourceRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public Source create(String title, String link, Category category) {
        Source saved = sourceRepository.save(new Source(title, link, category));

        // 트랜잭션 안에서 이벤트만 발행
        eventPublisher.publishEvent(new SourceCreatedEvent(saved.getId(), saved.getTitle()));
        return saved;
    }
}
