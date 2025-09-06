package com.example.mailtrend.MailSend.dto;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MailMessage implements Serializable {
    @NotBlank private String to;
    @NotBlank private String subject;
    private String summary;
    private String original;
    @NotBlank private String idempotencyKey;
    public MailMessage(String to, String subject, String summary, String original, String idempotencyKey) {
        this(to, subject, summary, original, idempotencyKey, null, null);
    }
    // 선택 (추가)
    private String heroImageUrl;
    private String ctaUrl;
}