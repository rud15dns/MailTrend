package com.example.mailtrend.Content.entity;

import com.example.mailtrend.oauth.entity.Category;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Getter
@Table(name="source")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Source {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    @Lob
    private String description;

    @Column(length = 2000)
    private String link;

    @Enumerated(EnumType.STRING)
    private Category category;

    private LocalDateTime createdAt;

    public Source(String title, String description, String link, Category category) {
        this.title = title;
        this.description = description;
        this.link = link;
        this.category = category;
        this.createdAt = LocalDateTime.now();
    }
}
