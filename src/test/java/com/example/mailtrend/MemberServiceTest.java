package com.example.mailtrend;

import com.example.mailtrend.Content.entity.AiSummary;
import com.example.mailtrend.Content.entity.MailContent;
import com.example.mailtrend.Content.entity.Source;
import com.example.mailtrend.Content.repository.MailContentRepository;
import com.example.mailtrend.oauth.dto.ArchiveResponse;
import com.example.mailtrend.oauth.entity.Category;
import com.example.mailtrend.oauth.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

class MemberServiceTest {

    @Mock
    private MailContentRepository mailContentRepository;

    @InjectMocks
    private MemberService memberService; // 실제 패키지/클래스명에 맞춰주세요

    private List<MailContent> backendList;
    private List<MailContent> frontendList;
    private List<MailContent> aiList;
    private List<MailContent> allList;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        backendList = makeCategoryList(Category.BACKEND, 3);
        frontendList = makeCategoryList(Category.FRONTEND, 3);
        aiList      = makeCategoryList(Category.AI, 3);

        allList = new ArrayList<>();
        allList.addAll(backendList);
        allList.addAll(frontendList);
        allList.addAll(aiList);
    }

    private List<MailContent> makeCategoryList(Category cat, int count) {
        List<MailContent> list = new ArrayList<>();
        for (int i = 1; i <= count; i++) {
            list.add(makeMailContent(cat, i));
        }
        return list;
    }

    private MailContent makeMailContent(Category cat, int idx) {
        Source s = new Source(
                "제목-" + cat + "-" + idx,
                "https://example.com/" + cat.name().toLowerCase() + "/" + idx,
                cat
        );
        AiSummary a = new AiSummary(s, "요약-" + cat + "-" + idx);
        LocalDateTime sent = LocalDateTime.of(2025, 8, 29, 10, 0).plusMinutes(idx);
        return new MailContent(a, sent);
    }


    private ArchiveResponse toDto(MailContent mc) {
        var s = mc.getAiSummary().getSource();
        return ArchiveResponse.builder()
                .contentId(mc.getId())
                .title(s.getTitle())
                .contentUrl(s.getLink())
                .sentAt(mc.getSentDate())
                .category(s.getCategory())
                .build();
    }

    @Nested
    @DisplayName("카테고리 필터 동작")
    class CategoryFilter {

        @Test
        @DisplayName("카테고리 null -> 전체 DTO 9건")
        void categoriesNull_returnsAll() {
            when(mailContentRepository.findAll()).thenReturn(allList);

            List<ArchiveResponse> result =
                    memberService.getArchive((YearMonth) null, null);

            assertThat(result).hasSize(9);
            assertThat(result.stream().map(ArchiveResponse::getCategory).toList())
                    .containsExactlyInAnyOrder(
                            Category.BACKEND, Category.BACKEND, Category.BACKEND,
                            Category.FRONTEND, Category.FRONTEND, Category.FRONTEND,
                            Category.AI, Category.AI, Category.AI
                    );

            assertThat(result.get(0).getTitle()).startsWith("제목-");

            verify(mailContentRepository, times(1)).findAll();
            verify(mailContentRepository, never()).findByAiSummary_Source_CategoryIn(anyList());
        }

        @Test
        @DisplayName("카테고리 빈 리스트 -> 전체 DTO 9건")
        void categoriesEmpty_returnsAll() {
            when(mailContentRepository.findAll()).thenReturn(allList);

            List<ArchiveResponse> result =
                    memberService.getArchive((YearMonth) null, List.of());

            assertThat(result).hasSize(9);
            verify(mailContentRepository, times(1)).findAll();
            verify(mailContentRepository, never()).findByAiSummary_Source_CategoryIn(anyList());
        }

        @Test
        @DisplayName("단일 카테고리(BACKEND) -> DTO 3건")
        void singleCategory_backend() {
            when(mailContentRepository.findByAiSummary_Source_CategoryIn(List.of(Category.BACKEND)))
                    .thenReturn(backendList);

            List<ArchiveResponse> result =
                    memberService.getArchive((YearMonth) null, List.of(Category.BACKEND));

            assertThat(result).hasSize(3);
            assertThat(result).allMatch(r -> r.getCategory() == Category.BACKEND);

            verify(mailContentRepository, times(1))
                    .findByAiSummary_Source_CategoryIn(List.of(Category.BACKEND));
            verify(mailContentRepository, never()).findAll();
        }

        @Test
        @DisplayName("복수 카테고리(BACKEND, FRONTEND) -> DTO 6건")
        void multiCategory_backendFrontend() {
            when(mailContentRepository.findByAiSummary_Source_CategoryIn(List.of(Category.BACKEND, Category.FRONTEND)))
                    .thenReturn(new ArrayList<>() {{
                        addAll(backendList);
                        addAll(frontendList);
                    }});

            List<ArchiveResponse> result =
                    memberService.getArchive((YearMonth) null, List.of(Category.BACKEND, Category.FRONTEND));

            assertThat(result).hasSize(6);
            assertThat(result.stream().map(ArchiveResponse::getCategory).distinct().toList())
                    .containsExactlyInAnyOrder(Category.BACKEND, Category.FRONTEND);

            verify(mailContentRepository, times(1))
                    .findByAiSummary_Source_CategoryIn(List.of(Category.BACKEND, Category.FRONTEND));
            verify(mailContentRepository, never()).findAll();
        }
    }

}
