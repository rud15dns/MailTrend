package com.example.mailtrend.MailSend.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.smartcardio.Card;
import java.io.Serializable;
import java.util.List;
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MailMessage implements Serializable {
    @NotBlank private String to;
    @NotBlank private String subject;
    private String summary;
    private String original;
    @NotBlank private String idempotencyKey;

    public MailMessage(String to, String subject, String summary, String original, String idempotencyKey) {
        this(to, subject, summary, original, idempotencyKey, null, null,null);
    }
    // 선택 (추가)
    private String heroImageUrl;
    private String ctaUrl;
    private List<Card> cards;
    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class Card implements Serializable {
        private String title;   // 카드 제목 (필수 권장)
        private String desc;    // 카드 요약(짧게)
        private String link;    // 원문 링크
        private List<String> bullets; // 선택: 불릿 포인트들(있으면 템플릿에서 출력)
    }
}