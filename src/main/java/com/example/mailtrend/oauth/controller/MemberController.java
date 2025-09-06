package com.example.mailtrend.oauth.controller;

import com.example.mailtrend.Content.entity.AiSummary;
import com.example.mailtrend.Content.entity.MailContent;
import com.example.mailtrend.Content.entity.Source;
import com.example.mailtrend.Content.repository.AiSummaryRepository;
import com.example.mailtrend.Content.repository.MailContentRepository;
import com.example.mailtrend.Content.repository.SourceRepository;
import com.example.mailtrend.MailSend.controller.DigestController;
import com.example.mailtrend.MailSend.service.DigestEnqueueService;
import com.example.mailtrend.common.apiPayload.error.CoreException;
import com.example.mailtrend.common.apiPayload.error.GlobalErrorType;
import com.example.mailtrend.common.apiPayload.response.ApiResponse;
import com.example.mailtrend.oauth.dto.ArchiveRequest;
import com.example.mailtrend.oauth.dto.ArchiveResponse;
import com.example.mailtrend.oauth.dto.CreateContentRequest;
import com.example.mailtrend.oauth.dto.MemberRequest;
import com.example.mailtrend.oauth.entity.Category;
import com.example.mailtrend.oauth.repository.MemberRepository;
import com.example.mailtrend.oauth.service.MemberService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;
import java.util.Set;
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api")
public class MemberController {
    private final MemberService memberService;

//    private final SourceRepository sourceRepository;
//    private final MailContentRepository mailContentRepository;
//    private final AiSummaryRepository aiSummaryRepository;

//    public MemberController(MemberService memberService, SourceRepository sourceRepository, MailContentRepository mailContentRepository, AiSummaryRepository aiSummaryRepository) {
//        this.memberService = memberService;
//        this.sourceRepository = sourceRepository;
//        this.mailContentRepository = mailContentRepository;
//        this.aiSummaryRepository = aiSummaryRepository;
//    }
    private final DigestEnqueueService digestEnqueueService;
    @PostMapping("/subscribe")
    public ResponseEntity<ApiResponse<?>> subscribe(@RequestBody MemberRequest request){
        if (request.getEmail() == null)
            throw new CoreException(GlobalErrorType.INVALID_EMAIL);

        if (request.getCategories() == null || request.getCategories().isEmpty())
            throw new CoreException(GlobalErrorType.INVALID_CATEGORIES);


        // 1) 구독 저장
        memberService.subscribe(request.getEmail(), request.getCategories());

        // 2) 해커톤 요구: 사용자가 AI만 선택했다고 가정 → 즉시 다이제스트 발송
        //    (여러 카테고리 왔어도 AI 포함이면 보내도록 처리)
        Set<Category> cats = request.getCategories();
        log.info("AAAA");
        if (cats.contains(Category.AI)) {
            log.info("AI 들어옴");
            // subject/CTA/히어로이미지는 임시 기본값 사용 (원하면 request에 추가해도 됨)
            String subject = "[MailTrend] 오늘의 AI 소식 5";
            String ctaUrl = "https://mailtrend.example.com";
            String hero = null; // 혹은 업로드된 S3 URL

            // 최신 5개 AI 콘텐츠로 즉시 발송 (block)
            log.info("AI 콘텐츠 즉시 발송");
            // 여기서 에러가 났음
            digestEnqueueService.sendLatest5NowTo(

                    request.getEmail(),
                    Category.AI,
                    subject,
                    hero,
                    ctaUrl
            );
            log.info("AI 콘텐츠 즉시 발송");
        }




        log.info("잘 수행됨!");
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PostMapping("/unsubscribe")
    public ResponseEntity<ApiResponse<?>> unsubscribe(@RequestBody MemberRequest request){
        if (request.getEmail() == null)
            throw new CoreException(GlobalErrorType.INVALID_EMAIL);

        memberService.unsubscribe(request.getEmail());
        return ResponseEntity.ok(ApiResponse.success());
    }

    @PutMapping("/change-category")
    public ResponseEntity<ApiResponse<?>> changeCategory(@RequestBody MemberRequest request){
        if (request.getEmail() == null)
            throw new CoreException(GlobalErrorType.INVALID_EMAIL);

        memberService.changeCategory(request.getEmail(), request.getCategories());
        return ResponseEntity.ok(ApiResponse.success());
    }

    @GetMapping("/archive")
    public ResponseEntity<ApiResponse<?>> archive(
            @RequestParam(value = "date", required = false) @DateTimeFormat(pattern = "yyyy-MM") YearMonth date,
            @RequestParam(value = "categories", required = false) List<Category> categories
    ) {
        Map<String, String> payload = Map.of(
                "sent_at", "9월 1주차",
                "content", "https://mailtrend-bucket.s3.ap-northeast-2.amazonaws.com/2025/09/06/1.png"
        );
        return ResponseEntity.ok(ApiResponse.success(payload)); // ✅ 래퍼에 담아서 반환
    }
    
    // 임시로 DB에 데이터 저장할 때 사용할 함수
//    @PostMapping("/create")
//    public ResponseEntity<ApiResponse<?>> createContent(@RequestBody CreateContentRequest request) {
//        try {
//            // 1. Source 생성
//            Source source = new Source(
//                    request.getTitle(),
//                    request.getLink(),
//                    request.getCategory()
//            );
//            Source savedSource = sourceRepository.save(source);
//
//            // 2. AiSummary 생성
//            AiSummary aiSummary = new AiSummary(
//                    savedSource,
//                    request.getSummaryText()
//            );
//            AiSummary savedAiSummary = aiSummaryRepository.save(aiSummary);
//
//            // 3. MailContent 생성
//            LocalDateTime sentDateTime = request.getSentDate() != null
//                    ? LocalDateTime.parse(request.getSentDate())
//                    : LocalDateTime.now();
//
//            MailContent mailContent = new MailContent(
//                    savedAiSummary,
//                    sentDateTime
//            );
//            MailContent savedMailContent = mailContentRepository.save(mailContent);
//
//            return ResponseEntity.ok(ApiResponse.success(Map.of(
//                    "sourceId", savedSource.getId(),
//                    "aiSummaryId", savedAiSummary.getId(),
//                    "mailContentId", savedMailContent.getId(),
//                    "message", "컨텐츠 생성 완료"
//            )));
//
//        } catch (Exception e) {
//            throw new CoreException(GlobalErrorType.E500);
//        }
//    }

}
