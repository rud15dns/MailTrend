package com.example.mailtrend.Content.repository;

import com.example.mailtrend.Content.entity.MailContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MailContentRepository extends JpaRepository<MailContent, Long> {
}