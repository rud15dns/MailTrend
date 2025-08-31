package com.example.mailtrend.oauth.controller;

import com.example.mailtrend.Content.entity.AiSummary;
import com.example.mailtrend.Content.entity.MailContent;
import com.example.mailtrend.Content.entity.Source;
import com.example.mailtrend.Content.repository.AiSummaryRepository;
import com.example.mailtrend.Content.repository.MailContentRepository;
import com.example.mailtrend.Content.repository.SourceRepository;
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
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

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
    public MemberController(MemberService memberService) {
            this.memberService = memberService;
    }


    @PostMapping("/subscribe")
    public ResponseEntity<ApiResponse<?>> subscribe(@RequestBody MemberRequest request){
        if (request.getEmail() == null)
            throw new CoreException(GlobalErrorType.INVALID_EMAIL);

        if (request.getCategories() == null || request.getCategories().isEmpty())
            throw new CoreException(GlobalErrorType.INVALID_CATEGORIES);

        memberService.subscribe(request.getEmail(), request.getCategories());
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
        System.out.println("받은 categories: " + categories); // 디버깅용

        List<ArchiveResponse> response = memberService.getArchive(date, categories);
        return ResponseEntity.ok(ApiResponse.success(response));
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
