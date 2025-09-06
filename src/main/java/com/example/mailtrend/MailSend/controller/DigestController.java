package com.example.mailtrend.MailSend.controller;
import com.example.mailtrend.oauth.entity.Category;
import com.example.mailtrend.MailSend.service.DigestEnqueueService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/digest")
public class DigestController {

    private final DigestEnqueueService digestEnqueueService;

    /** 예: POST /api/digest/AI?subject=[MailTrend]%20오늘의%20AI%20소식%205&ctaUrl=https://... */
    @PostMapping("/{category}")
    public ResponseEntity<?> enqueue(@PathVariable("category") String categoryName,
                                     @RequestParam(defaultValue = "[MailTrend] 오늘의 소식 5") String subject,
                                     @RequestParam(required = false) String heroImageUrl,
                                     @RequestParam(required = false) String ctaUrl) {
        Category category = Category.valueOf(categoryName.toUpperCase());
        int enqueued = digestEnqueueService.enqueueDigestByCategory(category, subject, heroImageUrl, ctaUrl);
        return ResponseEntity.ok(Map.of("category", category, "enqueued", enqueued));
    }
}
