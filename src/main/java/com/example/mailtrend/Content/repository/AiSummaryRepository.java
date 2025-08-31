package com.example.mailtrend.Content.repository;

import com.example.mailtrend.Content.entity.AiSummary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AiSummaryRepository extends JpaRepository<AiSummary, Long> {
}