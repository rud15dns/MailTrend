package com.example.mailtrend.oauth.dto;

import com.example.mailtrend.Content.entity.AiSummary;
import com.example.mailtrend.Content.entity.MailContent;
import com.example.mailtrend.Content.entity.Source;
import com.example.mailtrend.oauth.entity.Category;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonAppend;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@AllArgsConstructor
public class ArchiveResponse {
    @JsonProperty("content_id")
    private Long contentId;

    private String title;

    @JsonProperty("content_url")
    private String contentUrl;

    @JsonProperty("sent_at")
    private LocalDateTime sentAt;

    private Category category;

    public static ArchiveResponse from(MailContent mailContent){
        return ArchiveResponse.builder()
                .contentId(mailContent.getId())
                .title(mailContent.getSource().getTitle())
                .sentAt(mailContent.getSentDate())
                .contentUrl(mailContent.getSource().getLink())
                .category(mailContent.getSource().getCategory())
                .build();
    }
}
