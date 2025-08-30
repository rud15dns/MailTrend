package com.example.mailtrend.Content.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name="mail_content")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MailContent {
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="ai_summary_id")
    private AiSummary aiSummary;


    private LocalDateTime sentDate;

    public MailContent(AiSummary aiSummary, LocalDateTime sentDate) {
        this.aiSummary = aiSummary;
        this.sentDate = sentDate;
    }

    public Source getSource() {
        return this.aiSummary.getSource();
    }

}
