package com.example.mailtrend.oauth.dto;

import com.example.mailtrend.oauth.entity.Category;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.Set;

@Getter
public class MemberRequest {
    private String email;
    private Set<Category> categories;
}
