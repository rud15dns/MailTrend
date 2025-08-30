package com.example.mailtrend.Content.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name="ai_summary")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AiSummary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "source_id")
    private Source source;

    @Column(columnDefinition = "TEXT")
    private String text;

    private LocalDateTime createdAt;

    public AiSummary(Source source, String text) {
        this.source = source;
        this.text = text;
        this.createdAt = LocalDateTime.now();
    }
}
