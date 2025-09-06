package com.example.mailtrend.Content.repository;

import com.example.mailtrend.Content.entity.MailContent;
import com.example.mailtrend.oauth.entity.Category;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MailContentRepository extends JpaRepository<MailContent, Long> {
    List<MailContent> findByAiSummary_Source_CategoryIn(List<Category> categoryList);
    boolean existsByAiSummaryId(Long aiSummaryId);

    @Query("""
      select mc
      from MailContent mc
      join fetch mc.aiSummary s
      join fetch s.source src
      where mc.id = :id
    """)
    Optional<MailContent> findByIdWithSummaryAndSource(@Param("id") Long id);

    @EntityGraph(attributePaths = {"aiSummary", "aiSummary.source"})
    List<MailContent> findAllByIdIn(List<Long> ids);
    @EntityGraph(attributePaths = {"aiSummary", "aiSummary.source"})
    List<MailContent> findTop5ByAiSummary_Source_CategoryOrderByIdDesc(Category category);
}