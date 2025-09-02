package com.example.mailtrend.Content.repository;

import com.example.mailtrend.Content.entity.MailContent;
import com.example.mailtrend.oauth.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MailContentRepository extends JpaRepository<MailContent, Long> {
    List<MailContent> findByAiSummary_Source_CategoryIn(List<Category> categoryList);
    boolean existsByAiSummaryId(Long aiSummaryId);
}