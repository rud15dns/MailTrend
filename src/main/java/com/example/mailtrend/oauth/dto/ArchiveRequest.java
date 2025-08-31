package com.example.mailtrend.oauth.dto;

import com.example.mailtrend.oauth.entity.Category;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.YearMonth;
import java.util.List;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ArchiveRequest {
    @DateTimeFormat(pattern = "yyyy-MM")
    private YearMonth date;

    private List<Category> categories;
}
