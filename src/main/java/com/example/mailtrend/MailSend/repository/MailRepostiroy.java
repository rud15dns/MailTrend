package com.example.mailtrend.MailSend.repository;

import com.example.mailtrend.MailSend.dto.MailMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MailRepostiroy extends JpaRepository<MailMessage, Long> {
}
