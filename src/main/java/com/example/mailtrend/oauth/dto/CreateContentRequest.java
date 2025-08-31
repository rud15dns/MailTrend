package com.example.mailtrend.oauth.dto;

import com.example.mailtrend.oauth.entity.Category;
import lombok.Data;

@Data
public class CreateContentRequest {
    private String title;
    private String link;
    private Category category;
    private String summaryText;
    private String sentDate; // "2025-08-31T10:30:00" 형식 (선택사항)
}