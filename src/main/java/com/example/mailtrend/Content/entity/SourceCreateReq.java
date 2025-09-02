package com.example.mailtrend.Content.entity;

import com.example.mailtrend.oauth.entity.Category;
import jakarta.validation.constraints.NotBlank;

public record SourceCreateReq(
        @NotBlank String title,
        String link,
        Category category // null이면 서비스에서 기본값 주고 싶으면 처리
) {}
