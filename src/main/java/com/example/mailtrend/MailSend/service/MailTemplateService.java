package com.example.mailtrend.MailSend.service;

import com.example.mailtrend.Content.entity.Source;
import com.example.mailtrend.MailSend.dto.MailMessage;
import io.micrometer.common.lang.Nullable;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.List;
import java.util.Locale;

@Service
@Transactional
@RequiredArgsConstructor
public class MailTemplateService {

private final SpringTemplateEngine templateEngine;
    public String renderSummaryEmail(
            MailMessage msg,
            List<Source> sources,        // 반드시 5개
            @Nullable String heroImageUrl,
            @Nullable String ctaUrl
    ) {
        Context ctx = new Context(Locale.KOREA);
        ctx.setVariable("subject", msg.getSubject());
        ctx.setVariable("summary", msg.getSummary());
        ctx.setVariable("original", msg.getOriginal());   // 없으면 템플릿에서 자동 미표시
        ctx.setVariable("sources", sources);
        ctx.setVariable("heroImageUrl", heroImageUrl);
        ctx.setVariable("ctaUrl", ctaUrl != null ? ctaUrl : "#");
        return templateEngine.process("mail/summary", ctx);
    }
}
