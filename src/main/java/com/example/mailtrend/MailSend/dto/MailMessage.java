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
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MailMessage implements Serializable {
    @NotBlank @Id
    private String to;
    @NotBlank private String subject;

    // SummaryService가 만든 요약
    private String summary;

    // 원문(필요하면 일부만 저장해도 됨)
    private String original;

    // FIFO 중복 제거용 키
    @NotBlank private String idempotencyKey;
}