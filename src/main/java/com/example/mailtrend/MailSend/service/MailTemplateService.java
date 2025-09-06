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

    public String renderSummaryEmail(MailMessage msg) {
        Context ctx = new Context(Locale.KOREA);
        // 기본 필드
        ctx.setVariable("subject", msg.getSubject());
        ctx.setVariable("summary", msg.getSummary());
        ctx.setVariable("original", msg.getOriginal());
        ctx.setVariable("heroImageUrl", msg.getHeroImageUrl());
        ctx.setVariable("ctaUrl", msg.getCtaUrl());

        // 카드 5개
        ctx.setVariable("cards", msg.getCards());

        // 선택 메타 (없으면 템플릿에서 기본값 사용)
        // toName/mainTitle/prevUrl/webUrl/introHtml/footerImageUrl/words 등은 필요 시 추가
        return templateEngine.process("mail/summary", ctx);
    }
}
