package com.example.mailtrend;

import com.example.mailtrend.Content.entity.*;
import com.example.mailtrend.Content.repository.*;
import com.example.mailtrend.oauth.entity.Category;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class ContentEntityTest {

    @Autowired SourceRepository sourceRepository;
    @Autowired AiSummaryRepository aiSummaryRepository;
    @Autowired MailContentRepository mailContentRepository;

    @Test
    @DisplayName("Source -> AiSummary -> MailContent 연관관계 확인 테스트")
    void testEntityRelationships() {
        // 1. Source
        Source source = new Source(
                "테스트 뉴스 제목",
                "https://test-news.com/article/123",
                Category.BACKEND
        );
        Source savedSource = sourceRepository.save(source);

        // 2. AiSummary
        AiSummary savedAiSummary = aiSummaryRepository.save(
                new AiSummary(savedSource, "AI 요약 텍스트...")
        );

        // 3. MailContent
        MailContent savedMailContent = mailContentRepository.save(
                new MailContent(savedAiSummary, LocalDateTime.of(2025, 8, 30, 0, 0))
        );

        assertThat(savedMailContent.getId()).isNotNull();
        assertThat(savedMailContent.getAiSummary()).isNotNull();
        assertThat(savedMailContent.getAiSummary().getSource()).isNotNull();

        assertThat(savedMailContent.getAiSummary().getSource().getTitle())
                .isEqualTo("테스트 뉴스 제목");

        assertThat(savedMailContent.getSource().getCategory())
                .isEqualTo(Category.BACKEND);

        assertThat(savedMailContent.getSentDate())
                .isEqualTo(LocalDateTime.of(2025, 8, 30, 0, 0));

    }
}
