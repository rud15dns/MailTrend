package com.example.mailtrend.Content.service;

import com.example.mailtrend.Content.entity.Source;
import com.example.mailtrend.Content.entity.SourceCreateReq;
import com.example.mailtrend.Content.event.SourceCreatedEvent;
import com.example.mailtrend.Content.repository.SourceRepository;
import com.example.mailtrend.oauth.entity.Category;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SourceService {

    private final SourceRepository sourceRepository;
    private final ApplicationEventPublisher eventPublisher;


    @Transactional
    public List<Source> createBatch(List<SourceCreateReq> reqs) {
        List<Source> result = new ArrayList<>();
        for (SourceCreateReq r : reqs) {
            result.add(create(r.title(), r.link(),r.description(), r.category())); // 기존 로직 재사용
        }
        return result;
    }


    @Transactional
    public Source create(String title, String description, String link, Category category) {
        Source saved = sourceRepository.save(new Source(title, description, link, category));

        // 트랜잭션 안에서 Source -> Ai Summary 만 실행
        eventPublisher.publishEvent(new SourceCreatedEvent(saved.getId(), saved.getTitle(), saved.getDescription()));
        return saved;
    }

}