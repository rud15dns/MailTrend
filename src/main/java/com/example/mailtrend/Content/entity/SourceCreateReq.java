package com.example.mailtrend.Content.entity;

import com.example.mailtrend.oauth.entity.Category;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;


public record SourceCreateReq(
        @NotBlank String title,
        @NotBlank String description,
        String link,
        Category category // null이면 서비스에서 기본값 주고 싶으면 처리
) {
    public String getDescription() {
        return description;
    }
    public String getTitle() {
        return title;
    }
    public String getLink() {
        return link;
    }

    public Category getCategory() {
        return category;
    }

}
